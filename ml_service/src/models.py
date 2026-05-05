import json
import  logging
import numpy as np
import joblib
import tensorflow as tf
from pathlib import Path



BASE_DIR    = Path(__file__).parent
CONFIG_PATH = BASE_DIR / "artifacts" / "live_model_config.json"

# Model Registry
class ModelRegistry:
    def __init__(self):
        self.config = None
        self.model = None
        self.scaler = None
        self.feature_names = None
        self.active_name = None
        self.requires_sequence = False
        self.window_size = 10
        self.n_features = 74
        self.label_map = {}

    def load(self):
        logging.info("Loading model registry from config......")

        with open(CONFIG_PATH) as f:
            self.config = json.load(f)

        self.active_name = self.config["active_model"]
        self.window_size = self.config["window_size"]
        self.n_features = self.config["n_features"]
        self.n_classes = self.config["n_classes"]
        self.label_map = self.config["label_map"]

        model_config = self.config["model"][self.active_name]
        self.requires_sequence = model_config["requires_sequence"]

        model_path = BASE_DIR / model_config["path"]
        scaler_path = BASE_DIR / model_config["scaler_path"]
        feature_path = BASE_DIR / model_config["feature_path"]


        # Load scaler
        self.scaler = joblib.load(scaler_path)
        logging.info(f"Scaler loaded from {scaler_path}")

        # Load model based on type
        if model_config["type"] == "keras":
            self.model = tf.keras.models.load_model(model_path)
            logging.info

