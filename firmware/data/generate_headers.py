import pandas as pd
import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent.parent
DIR_PATH = os.path.join(BASE_DIR, "ml_service", "data", "raw")
DATA_PATH = os.path.join(DIR_PATH, "copper_tailings_pipeline_data.csv")
df = pd.read_csv(DATA_PATH)

print("Scenarios found:", df["scenario"].unique())

# new column names
cols = [
    "pressure_A", "velocity_A",
    "pressure_B", "velocity_B",
    "pressure_C", "velocity_C"
]

scenarios = {
    "normal_1":      df[df["scenario"] == "normal"].head(700),
    "normal_2":      df[df["scenario"] == "normal"].iloc[700:1400],
    "normal_3":      df[df["scenario"] == "normal"].iloc[1400:2100],
    "leak_incipient": df[df["scenario"] == "leak_incipient"].head(700),
    "leak_moderate":  df[df["scenario"] == "leak_moderate"].head(700),
    "leak_critical":  df[df["scenario"] == "leak_critical"].head(700),
    "blockage_25":   df[df["scenario"] == "blockage_25"].head(700),
    "blockage_50":   df[df["scenario"] == "blockage_50"].head(700),
    "blockage_75":   df[df["scenario"] == "blockage_75"].head(700),
}

os.makedirs("../src/data", exist_ok=True)

for name, data in scenarios.items():
    data = data[cols].dropna().head(700)
    varname = name.upper()
    lines = []
    lines.append("#pragma once")
    lines.append("#include <pgmspace.h>")
    lines.append(f"const float {varname}_DATA[][6] PROGMEM = {{")
    for _, row in data.iterrows():
        lines.append(f"    {{{row['pressure_A']:.2f}f, {row['velocity_A']:.4f}f, "
                     f"{row['pressure_B']:.2f}f, {row['velocity_B']:.4f}f, "
                     f"{row['pressure_C']:.2f}f, {row['velocity_C']:.4f}f}},")
    lines.append("};")
    lines.append(f"const int {varname}_LEN = {len(data)};")

    with open(f"../src/data/{name}.h", "w") as f:
        f.write("\n".join(lines))

    print(f"Generated {name}.h — {len(data)} rows")

print("Done.")