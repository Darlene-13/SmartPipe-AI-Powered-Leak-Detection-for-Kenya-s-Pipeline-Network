import tensorflow as tf
import joblib, os

models = {
    "lstm": "artifacts/lstm_best_live_model.keras",
    "cnn": "artifacts/cnn_best_live_model.keras",
    "mlp": "artifacts/mlp_best_live_model.keras",
}

for name, path in models.items():
    print(f"\nProcessing {name}...")
    model = tf.keras.models.load_model(path, compile=False)

    # Save as .h5
    model.save(f"artifacts/{name}_live.h5")
    print(f"  Saved {name}_live.h5")

    # Save weights only
    model.save_weights(f"artifacts/{name}.weights.h5")
    print(f"  Saved {name}.weights.h5")

    # Save architecture JSON
    with open(f"artifacts/{name}_architecture.json", "w") as f:
        f.write(model.to_json())
    print(f"  Saved {name}_architecture.json")

print("\nDone!")