import os
import logging
import traceback
import threading
import json
import time
import psycopg2
import paho.mqtt.client as mqtt
from datetime    import datetime, timezone
from flask       import Flask, request, jsonify
from flask_cors  import CORS
from models      import registry
from predictor   import predictor

logging.basicConfig(
    level  = logging.INFO,
    format = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
app = Flask(__name__)
CORS(app)

try:
    registry.load()
    logging.info("Model registry loaded successfully")
except Exception as e:
    logging.error(f"FATAL: Could not load model: {e}")
    raise

@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status"       : "ok",
        "active_model" : registry.active_name,
        "window_size"  : registry.window_size,
        "n_features"   : registry.n_features,
        "buffer_status": predictor.get_buffer_status()
    }), 200

@app.route("/predict", methods=["POST"])
def predict_live():
    try:
        raw = request.get_json()
        if not raw:
            return jsonify({"error": "No JSON body received"}), 400

        device_id = raw.get("device_id", "default")

        required = [
            "node_a_pressure", "velocity_a",
            "node_b_pressure", "velocity_b",
            "node_c_pressure", "velocity_c"
        ]
        missing = [f for f in required if f not in raw]
        if missing:
            return jsonify({
                "error"  : "Missing required fields",
                "missing": missing
            }), 400

        result = predictor.predict(device_id, raw)
        return jsonify(result), 200

    except Exception as e:
        logging.error(f"Prediction error: {traceback.format_exc()}")
        return jsonify({
            "error" : "Prediction failed",
            "detail": str(e)
        }), 500

@app.route("/reset/<device_id>", methods=["POST"])
def reset_buffer(device_id):
    predictor.reset_buffer(device_id)
    return jsonify({
        "status"   : "ok",
        "device_id": device_id,
        "message"  : f"Buffer reset for {device_id}"
    }), 200

@app.route("/status", methods=["GET"])
def status():
    return jsonify(predictor.get_buffer_status()), 200

def start_replay_thread():
    broker_url = os.environ.get("MQTT_BROKER_URL")
    if not broker_url:
        logging.info("MQTT_BROKER_URL not set - replay simulator disabled")
        return

    db_url    = os.environ.get("SEED_DB_URL") or os.environ.get("DB_URL")
    username  = os.environ.get("MQTT_USERNAME")
    password  = os.environ.get("MQTT_PASSWORD")
    topic     = os.environ.get("MQTT_TOPIC", "pipeline/sensors/ESP32_REPLAY_01/node")
    interval  = float(os.environ.get("REPLAY_INTERVAL_MS", "2000")) / 1000.0
    device_id = os.environ.get("REPLAY_DEVICE_ID", "ESP32_REPLAY_01")

    if not db_url:
        logging.warning("No DB URL found - replay simulator skipping")
        return

    def replay():
        from collections import deque
        import statistics

        # ── Connect DB ────────────────────────────────────────────────────────
        try:
            conn = psycopg2.connect(db_url)
            logging.info("Replay: DB connected")
        except Exception as e:
            logging.error(f"Replay: DB connection failed: {e}")
            return

        # ── Connect MQTT ──────────────────────────────────────────────────────
        client = mqtt.Client(
            client_id=f"replay-{int(time.time())}",
            protocol=mqtt.MQTTv5
        )
        client.username_pw_set(username, password)

        url = broker_url
        if url.startswith("ssl://"):
            host, port = url[6:].rsplit(":", 1)
            client.tls_set()
        else:
            host, port = url.replace("tcp://", "").rsplit(":", 1)

        try:
            client.connect(host, int(port), keepalive=60)
            client.loop_start()
            logging.info(f"Replay: MQTT connected to {host}:{port}")
        except Exception as e:
            logging.error(f"Replay: MQTT connection failed: {e}")
            return

        time.sleep(3)

        # ── Preload IDs in round-robin order ──────────────────────────────────
        try:
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
            logging.info(f"Replay: {len(ids):,} readings loaded - starting")
        except Exception as e:
            logging.error(f"Replay: failed to load IDs: {e}")
            return

        # ── Stateful temporal feature state ───────────────────────────────────
        WINDOW = 10  # rolling window size — matches LSTM window_size

        prev_pa, prev_pb, prev_pc = None, None, None
        prev_va, prev_vb, prev_vc = None, None, None
        prev_dp_dt_a, prev_dp_dt_b, prev_dp_dt_c = 0.0, 0.0, 0.0

        roll_pa = deque(maxlen=WINDOW)
        roll_pb = deque(maxlen=WINDOW)
        roll_pc = deque(maxlen=WINDOW)
        roll_vb = deque(maxlen=WINDOW)
        roll_drop_bc = deque(maxlen=WINDOW)
        roll_mid_p_dev = deque(maxlen=WINDOW)
        roll_mid_v_dev = deque(maxlen=WINDOW)

        idx   = 0
        cycle = 0
        total = len(ids)

        # ── Main loop ─────────────────────────────────────────────────────────
        while True:
            try:
                if idx >= total:
                    idx    = 0
                    cycle += 1
                    # reset state on cycle restart
                    prev_pa = prev_pb = prev_pc = None
                    prev_va = prev_vb = prev_vc = None
                    prev_dp_dt_a = prev_dp_dt_b = prev_dp_dt_c = 0.0
                    for d in [roll_pa, roll_pb, roll_pc, roll_vb,
                               roll_drop_bc, roll_mid_p_dev, roll_mid_v_dev]:
                        d.clear()
                    logging.info(f"Replay: cycle {cycle} - restarting from row 1")

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

                    # ── dp/dt (first derivatives) ─────────────────────────
                    dt = interval  # seconds between readings
                    dp_dt_a = (pa - prev_pa) / dt if prev_pa is not None else 0.0
                    dp_dt_b = (pb - prev_pb) / dt if prev_pb is not None else 0.0
                    dp_dt_c = (pc - prev_pc) / dt if prev_pc is not None else 0.0

                    # ── d2p/dt2 (second derivatives) ──────────────────────
                    d2p_dt2_a = (dp_dt_a - prev_dp_dt_a) / dt
                    d2p_dt2_b = (dp_dt_b - prev_dp_dt_b) / dt
                    d2p_dt2_c = (dp_dt_c - prev_dp_dt_c) / dt

                    # ── rolling stats ─────────────────────────────────────
                    roll_pa.append(pa)
                    roll_pb.append(pb)
                    roll_pc.append(pc)
                    roll_vb.append(vb)

                    mid_p_dev = pb - (pa + pc) / 2
                    mid_v_dev = vb - (va + vc) / 2
                    drop_bc   = pb - pc

                    roll_drop_bc.append(drop_bc)
                    roll_mid_p_dev.append(mid_p_dev)
                    roll_mid_v_dev.append(mid_v_dev)

                    def rmean(d): return sum(d) / len(d) if d else 0.0
                    def rstd(d):  return statistics.stdev(d) if len(d) > 1 else 0.0

                    payload = json.dumps({
                        "device_id":        device_id,
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
                        "rolling_mean_midpoint_pressure_deviation": rmean(roll_mid_p_dev),
                        "rolling_std_midpoint_pressure_deviation":  rstd(roll_mid_p_dev),
                        "rolling_mean_midpoint_velocity_deviation": rmean(roll_mid_v_dev),
                        "rolling_std_midpoint_velocity_deviation":  rstd(roll_mid_v_dev),
                    })

                    client.publish(topic, payload, qos=1)

                    # update previous state
                    prev_pa, prev_pb, prev_pc = pa, pb, pc
                    prev_va, prev_vb, prev_vc = va, vb, vc
                    prev_dp_dt_a = dp_dt_a
                    prev_dp_dt_b = dp_dt_b
                    prev_dp_dt_c = dp_dt_c

                    if idx % 100 == 0:
                        logging.info(
                            f"Replay: [{idx+1}/{total}] cycle={cycle} "
                            f"scenario={scenario} "
                            f"pA={pa:.0f} dp_dt_a={dp_dt_a:.2f} "
                            f"roll_std_pA={rstd(roll_pa):.2f}"
                        )

                idx += 1
                time.sleep(interval)

            except Exception as e:
                logging.error(f"Replay error at idx {idx}: {e}")
                time.sleep(5)

    t = threading.Thread(target=replay, name="ReplaySimulator", daemon=True)
    t.start()
    logging.info("Replay simulator thread started")

if __name__ == "__main__":
    # Local: python app.py
    port = int(os.environ.get("PORT", 5000))
    start_replay_thread()
    app.run(host="0.0.0.0", port=port, debug=False, threaded=True)
else:
    # Render: gunicorn app:app
    # __name__ is "app" not "__main__" so we start the thread here
    start_replay_thread()
