

import os
import json
import time
import logging
import statistics
import psycopg2
import paho.mqtt.client as mqtt

from collections import deque
from datetime    import datetime, timezone
from dotenv      import load_dotenv
from pathlib     import Path

load_dotenv(dotenv_path=Path(__file__).parent.parent / "..env")

logging.basicConfig(
    level  = logging.INFO,
    format = "%(asctime)s [REPLAY] %(levelname)s — %(message)s"
)

BROKER_URL  = os.environ.get("MQTT_BROKER_URL")
USERNAME    = os.environ.get("MQTT_USERNAME")
PASSWORD    = os.environ.get("MQTT_PASSWORD")
TOPIC       = os.environ.get("MQTT_TOPIC", "pipeline/sensors/ESP32_REPLAY_01/node")
DEVICE_ID   = os.environ.get("REPLAY_DEVICE_ID", "ESP32_REPLAY_01")
DB_URL      = os.environ.get("SEED_DB_URL") or os.environ.get("DB_URL")
INTERVAL    = float(os.environ.get("REPLAY_INTERVAL_MS", "2000")) / 1000.0
WINDOW      = 10   # rolling window — matches LSTM window_size

if not BROKER_URL:
    raise RuntimeError("MQTT_BROKER_URL not set in ..env")
if not DB_URL:
    raise RuntimeError("SEED_DB_URL not set in ..env")

logging.info(f"Broker : {BROKER_URL}")
logging.info(f"Topic  : {TOPIC}")
logging.info(f"DB     : {DB_URL.split('@')[-1]}")   # hide credentials
logging.info(f"Interval: {INTERVAL}s  Window: {WINDOW}")

logging.info("Connecting to PostgreSQL...")
conn = psycopg2.connect(DB_URL)
logging.info("PostgreSQL connected.")

client = mqtt.Client(
    client_id=f"replay-{int(time.time())}",
    protocol=mqtt.MQTTv5
)
client.username_pw_set(USERNAME, PASSWORD)

if BROKER_URL.startswith("ssl://"):
    host, port = BROKER_URL[6:].rsplit(":", 1)
    client.tls_set()
else:
    host, port = BROKER_URL.replace("tcp://", "").rsplit(":", 1)

logging.info(f"Connecting to MQTT broker {host}:{port}...")
client.connect(host, int(port), keepalive=60)
client.loop_start()
logging.info("MQTT connected.")
time.sleep(2)   # let connection settle


logging.info("Loading sensor reading IDs in round-robin order...")
with conn.cursor() as cur:
    cur.execute("""
        SELECT id
        FROM sensor_readings
        ORDER BY
            CAST(REGEXP_REPLACE(scenario, '.*run', '', 'g') AS INTEGER) ASC,
            CASE
                WHEN scenario ILIKE 'normal%'         THEN 1
                WHEN scenario ILIKE 'leak_incipient%' THEN 2
                WHEN scenario ILIKE 'leak_moderate%'  THEN 3
                WHEN scenario ILIKE 'leak_critical%'  THEN 4
                WHEN scenario ILIKE 'blockage_25%'    THEN 5
                WHEN scenario ILIKE 'blockage_50%'    THEN 6
                WHEN scenario ILIKE 'blockage_75%'    THEN 7
                ELSE 8
            END ASC,
            reading_time ASC
    """)
    ids = [row[0] for row in cur.fetchall()]

total = len(ids)
logging.info(f"{total:,} readings loaded. Starting replay at {INTERVAL}s interval...")
logging.info("Order: normal → leak_incipient → leak_moderate → leak_critical → blockage_25 → blockage_50 → blockage_75 (per run)")

prev_pa = prev_pb = prev_pc = None
prev_dp_dt_a = prev_dp_dt_b = prev_dp_dt_c = 0.0

roll_pa      = deque(maxlen=WINDOW)
roll_pb      = deque(maxlen=WINDOW)
roll_pc      = deque(maxlen=WINDOW)
roll_vb      = deque(maxlen=WINDOW)
roll_drop_bc = deque(maxlen=WINDOW)
roll_mid_p   = deque(maxlen=WINDOW)
roll_mid_v   = deque(maxlen=WINDOW)

def rmean(d): return sum(d) / len(d) if d else 0.0
def rstd(d):  return statistics.stdev(d) if len(d) > 1 else 0.0

def reset_state():
    global prev_pa, prev_pb, prev_pc, prev_dp_dt_a, prev_dp_dt_b, prev_dp_dt_c
    prev_pa = prev_pb = prev_pc = None
    prev_dp_dt_a = prev_dp_dt_b = prev_dp_dt_c = 0.0
    for d in [roll_pa, roll_pb, roll_pc, roll_vb, roll_drop_bc, roll_mid_p, roll_mid_v]:
        d.clear()

idx   = 0
cycle = 0

while True:
    try:
        if idx >= total:
            idx    = 0
            cycle += 1
            reset_state()
            logging.info(f"Cycle {cycle} — restarting from row 1")

        # Fetch row
        with conn.cursor() as cur:
            cur.execute("""
                SELECT node_a_pressure, velocity_a,
                       node_b_pressure, velocity_b,
                       node_c_pressure, velocity_c,
                       scenario
                FROM sensor_readings
                WHERE id = %s
            """, (ids[idx],))
            row = cur.fetchone()

        if row:
            pa, va, pb, vb, pc, vc, scenario = row

            # dp/dt
            dp_dt_a = (pa - prev_pa) / INTERVAL if prev_pa is not None else 0.0
            dp_dt_b = (pb - prev_pb) / INTERVAL if prev_pb is not None else 0.0
            dp_dt_c = (pc - prev_pc) / INTERVAL if prev_pc is not None else 0.0

            # d2p/dt2
            d2p_dt2_a = (dp_dt_a - prev_dp_dt_a) / INTERVAL
            d2p_dt2_b = (dp_dt_b - prev_dp_dt_b) / INTERVAL
            d2p_dt2_c = (dp_dt_c - prev_dp_dt_c) / INTERVAL

            # Rolling stats
            mid_p_dev = pb - (pa + pc) / 2
            mid_v_dev = vb - (va + vc) / 2
            drop_bc   = pb - pc

            roll_pa.append(pa);  roll_pb.append(pb);  roll_pc.append(pc)
            roll_vb.append(vb)
            roll_drop_bc.append(drop_bc)
            roll_mid_p.append(mid_p_dev)
            roll_mid_v.append(mid_v_dev)

            payload = json.dumps({
                "device_id":        DEVICE_ID,
                "ts":               datetime.now(timezone.utc).isoformat(),
                # raw sensor values
                "node_a_pressure":  pa,
                "velocity_a":       va,
                "node_b_pressure":  pb,
                "velocity_b":       vb,
                "node_c_pressure":  pc,
                "velocity_c":       vc,
                "sc":               scenario or "replay",
                # temporal derivatives
                "dp_dt_a":          dp_dt_a,
                "dp_dt_b":          dp_dt_b,
                "dp_dt_c":          dp_dt_c,
                "d2p_dt2_a":        d2p_dt2_a,
                "d2p_dt2_b":        d2p_dt2_b,
                "d2p_dt2_c":        d2p_dt2_c,
                # rolling stats
                "rolling_mean_node_a_pressure":             rmean(roll_pa),
                "rolling_std_node_a_pressure":              rstd(roll_pa),
                "rolling_mean_node_b_pressure":             rmean(roll_pb),
                "rolling_std_node_b_pressure":              rstd(roll_pb),
                "rolling_mean_node_c_pressure":             rmean(roll_pc),
                "rolling_std_node_c_pressure":              rstd(roll_pc),
                "rolling_mean_velocity_b":                  rmean(roll_vb),
                "rolling_std_velocity_b":                   rstd(roll_vb),
                "rolling_mean_pressure_drop_bc":            rmean(roll_drop_bc),
                "rolling_std_pressure_drop_bc":             rstd(roll_drop_bc),
                "rolling_mean_midpoint_pressure_deviation": rmean(roll_mid_p),
                "rolling_std_midpoint_pressure_deviation":  rstd(roll_mid_p),
                "rolling_mean_midpoint_velocity_deviation": rmean(roll_mid_v),
                "rolling_std_midpoint_velocity_deviation":  rstd(roll_mid_v),
            })

            result = client.publish(topic=TOPIC, payload=payload, qos=1)

            # Update state
            prev_pa, prev_pb, prev_pc = pa, pb, pc
            prev_dp_dt_a, prev_dp_dt_b, prev_dp_dt_c = dp_dt_a, dp_dt_b, dp_dt_c

            if idx % 50 == 0:
                logging.info(
                    f"[{idx+1}/{total}] cycle={cycle} scenario={scenario} "
                    f"pA={pa:.0f} dp_dt_a={dp_dt_a:.1f} "
                    f"roll_std_pA={rstd(roll_pa):.1f} "
                    f"mqtt_rc={result.rc}"
                )

        idx += 1
        time.sleep(INTERVAL)

    except KeyboardInterrupt:
        logging.info("Replay stopped by user.")
        break
    except Exception as e:
        logging.error(f"Replay error at idx {idx}: {e}", exc_info=True)
        time.sleep(5)

client.loop_stop()
conn.close()
logging.info("Replay shutdown complete.")
