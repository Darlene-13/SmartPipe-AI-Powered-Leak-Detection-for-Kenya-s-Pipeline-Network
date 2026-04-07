import os
import pandas as pd

folder = os.path.expanduser("~/Downloads/LEAK DETECTION SYSTEM")
all_data = []

for i in range(1, 701):
    filename = f"normal_simulation-{i:04d}"
    filepath = os.path.join(folder, filename)

    if os.path.exists(filepath):
        try:
            df = pd.read_csv(
                filepath,
                sep=',',
                skipinitialspace=True,  # strips leading spaces after commas
                on_bad_lines='skip'
            )
            df['timestep'] = i
            df['label'] = 0
            all_data.append(df)
        except Exception as e:
            print(f"Failed to read {filepath}: {e}")
    else:
        print(f"Missing file: {filepath}")

if all_data:
    final = pd.concat(all_data, ignore_index=True)
    final.to_csv("normal_dataset.csv", index=False)
    print(f"Done! {len(final)} rows across {len(all_data)} files.")
else:
    print("No valid files were found.")