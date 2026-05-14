import pandas as pd
import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent.parent
DIR_PATH = os.path.join(BASE_DIR,"ml_service","data", "raw")
DATA_PATH = os.path.join(DIR_PATH,"sensor_point_dataset_raw.csv")
df = pd.read_csv(DATA_PATH)

cols = [
    "node_a_pressure", "velocity_a",
    "node_b_pressure", "velocity_b",
    "node_c_pressure", "velocity_c"
]

scenarios = {
    "normal_1":      df[df["scenario_id"] == "normal_v095_c095"],
    "normal_2":      df[df["scenario_id"] == "normal_v100_c095"],
    "normal_3":      df[df["scenario_id"] == "normal_v105_c095"],
    "leak_incipient": df[df["scenario_id"].str.startswith("leak_incipient")].head(700),
    "leak_moderate":  df[df["scenario_id"].str.startswith("leak_moderate")].head(700),
    "leak_critical":  df[df["scenario_id"].str.startswith("leak_critical")].head(700),
    "blockage_25":   df[df["scenario_id"].str.startswith("blockage_25")].head(700),
    "blockage_50":   df[df["scenario_id"].str.startswith("blockage_50")].head(700),
    "blockage_75":   df[df["scenario_id"].str.startswith("blockage_75")].head(700),
}

os.makedirs("../src/data", exist_ok=True)

for name, data in scenarios.items():
    data = data[cols].dropna().head(700)
    varname = name.upper()
    lines = []
    lines.append(f"#pragma once")
    lines.append(f"#include <pgmspace.h>")
    lines.append(f"const float {varname}_DATA[][6] PROGMEM = {{")
    for _, row in data.iterrows():
        lines.append(f"    {{{row['node_a_pressure']:.2f}f, {row['velocity_a']:.4f}f, "
                     f"{row['node_b_pressure']:.2f}f, {row['velocity_b']:.4f}f, "
                     f"{row['node_c_pressure']:.2f}f, {row['velocity_c']:.4f}f}},")
    lines.append("};")
    lines.append(f"const int {varname}_LEN = {len(data)};")

    with open(f"../src/data/{name}.h", "w") as f:
        f.write("\n".join(lines))

    print(f"Generated {name}.h — {len(data)} rows")

print("Done.")