import os
import pandas as pd
import psycopg2
from psycopg2.extras import execute_values
from datetime import datetime, timezone, timedelta
from dotenv import load_dotenv
from concurrent.futures import ThreadPoolExecutor, as_completed

load_dotenv()

DATABASE_URL = os.environ.get("SEED_DB_URL")
if not DATABASE_URL:
    raise RuntimeError("SEED_DB_URL not set in .env")

CSV_PATH    = "ml_service/data/processed/live_feature_dataset.csv"
LABEL_MAP   = {0: "Normal", 1: "Leak", 2: "Blockage"}
BATCH_SIZE  = 2000
NUM_WORKERS = 4
TOTAL_DAYS  = 52  # fixed: data ends today June 5


def derive_severity(label, confidence):
    if label == "Normal": return "NONE"
    if confidence < 0.50: return "LOW"
    if confidence < 0.75: return "MODERATE"
    return "CRITICAL"


def synthetic_confidence(scenario_id, label):
    if label == "Normal": return 0.97
    s = scenario_id.lower()
    if "75" in s: return 0.96
    if "50" in s: return 0.84
    return 0.72


def device_id(scenario_id):
    return "ESP32_REPLAY_01"


def insert_chunk(chunk_df: pd.DataFrame, base_time: datetime, start_index: int) -> list:
    TIMESTEP = timedelta(seconds=30)

    conn = psycopg2.connect(
        DATABASE_URL,
        keepalives=1,
        keepalives_idle=30,
        keepalives_interval=10,
        keepalives_count=5,
    )
    conn.autocommit = False
    cur = conn.cursor()

    # CHANGE 1: added reading_time to RETURNING so we can pass it to fault_alerts
    read_sql = """
        INSERT INTO sensor_readings
            (device_id, reading_time,
             node_a_pressure, velocity_a,
             node_b_pressure, velocity_b,
             node_c_pressure, velocity_c,
             dp_dt_a, dp_dt_b, dp_dt_c,
             scenario, prediction)
        VALUES %s
        RETURNING id, reading_time, device_id, scenario, prediction
    """

    alert_rows = []

    for batch_start in range(0, len(chunk_df), BATCH_SIZE):
        batch = chunk_df.iloc[batch_start: batch_start + BATCH_SIZE]
        rows  = []

        for local_i, (_, row) in enumerate(batch.iterrows()):
            global_i  = start_index + batch_start + local_i
            sid       = row["scenario_id"]
            label_str = LABEL_MAP[int(row["label"])]
            dev       = device_id(sid)
            rt        = base_time + TIMESTEP * global_i

            rows.append((
                dev, rt,
                float(row["node_a_pressure"]), float(row["velocity_a"]),
                float(row["node_b_pressure"]), float(row["velocity_b"]),
                float(row["node_c_pressure"]), float(row["velocity_c"]),
                float(row.get("dp_dt_a", 0.0)),
                float(row.get("dp_dt_b", 0.0)),
                float(row.get("dp_dt_c", 0.0)),
                sid, label_str,
            ))

        returned = execute_values(cur, read_sql, rows, fetch=True)
        conn.commit()

        # CHANGE 2: unpack reading_time (rt) from RETURNING result and include in alert_rows
        for rid, rt, dev, sid, label_str in returned:
            if label_str != "Normal":
                conf = synthetic_confidence(sid, label_str)
                alert_rows.append((rid, rt, dev, label_str, sid, conf))

        done = batch_start + len(batch)
        pct  = round(done / len(chunk_df) * 100, 1)
        print(f"  [worker start={start_index}] {done}/{len(chunk_df)} ({pct}%)")

    cur.close()
    conn.close()
    return alert_rows


def insert_alerts(all_alert_rows: list):
    conn = psycopg2.connect(DATABASE_URL)
    conn.autocommit = False
    cur  = conn.cursor()

    # CHANGE 3: added reading_time to INSERT columns (not null, FK constraint)
    alert_sql = """
        INSERT INTO fault_alerts
            (sensor_reading_id, reading_time,
             device_id, fault_class, severity_level,
             confidence, prob_normal, prob_warning, prob_critical,
             latency_ms, recommendation)
        VALUES %s
    """

    rows = []
    # CHANGE 4: unpack rt from the tuple
    for i, (rid, rt, dev, label, sid, conf) in enumerate(all_alert_rows):
        sev = derive_severity(label, conf)
        fc  = label.upper()
        if label == "Leak":
            pn, pw, pc = round(1 - conf - 0.001, 4), round(conf, 4), 0.001
        else:
            pn, pw, pc = round(1 - conf - 0.001, 4), 0.001, round(conf, 4)

        # CHANGE 5: rt inserted as second value matching reading_time column
        rows.append((
            rid, rt, dev, fc, sev,
            conf, pn, pw, pc,
            800 + (i % 400),
            f"{label} detected in '{sid}'. "
            f"Confidence: {round(conf * 100, 1)}%. Severity: {sev}. "
            f"Inspect pipeline segment immediately.",
        ))

    for batch_start in range(0, len(rows), BATCH_SIZE):
        execute_values(cur, alert_sql, rows[batch_start: batch_start + BATCH_SIZE])
        conn.commit()
        done = min(batch_start + BATCH_SIZE, len(rows))
        print(f"  [alerts] {done}/{len(rows)}")

    cur.close()
    conn.close()


def main():
    print("Loading CSV...")
    df = pd.read_csv(CSV_PATH)

    # Sort by scenario then reading order so LSTM window stays within same scenario
    df = df.sort_values(by=["scenario_id", df.columns[0]]).reset_index(drop=True)

    total_rows = len(df)

    base = datetime.now(timezone.utc) - timedelta(days=TOTAL_DAYS)
    print(f"  {total_rows} rows  {NUM_WORKERS} parallel workers")
    print(f"  Timeline: {base.strftime('%Y-%m-%d %H:%M')}  now")
    print(f"  Data order: sorted by scenario (LSTM-friendly)")

    chunk_size = (total_rows + NUM_WORKERS - 1) // NUM_WORKERS
    chunks = [
        (df.iloc[i: i + chunk_size], base, i)
        for i in range(0, total_rows, chunk_size)
    ]

    all_alert_rows = []

    print("\nPhase 1: inserting sensor_readings")
    with ThreadPoolExecutor(max_workers=NUM_WORKERS) as pool:
        futures = {pool.submit(insert_chunk, *args): idx
                   for idx, args in enumerate(chunks)}
        for fut in as_completed(futures):
            alert_rows = fut.result()
            all_alert_rows.extend(alert_rows)
            print(f"  worker {futures[fut]} complete  {len(alert_rows)} alerts queued")

    print(f"\n  sensor_readings done. {len(all_alert_rows)} alerts to insert.")

    print("\nPhase 2: inserting fault_alerts")
    insert_alerts(all_alert_rows)

    print("\nSummary")
    conn = psycopg2.connect(DATABASE_URL)
    cur  = conn.cursor()
    cur.execute("SELECT COUNT(*) FROM sensor_readings")
    print(f"  sensor_readings : {cur.fetchone()[0]}")
    cur.execute("SELECT COUNT(*) FROM fault_alerts")
    print(f"  fault_alerts    : {cur.fetchone()[0]}")
    cur.execute("SELECT MIN(reading_time), MAX(reading_time) FROM sensor_readings")
    lo, hi = cur.fetchone()
    print(f"  Time range      : {lo.strftime('%Y-%m-%d')}  {hi.strftime('%Y-%m-%d')}")
    cur.close()
    conn.close()


if __name__ == "__main__":
    main()