import json
import logging
import numpy as np
import joblib
import tensorflow as tf
from pathlib import Path


BASE_DIR    = Path(__file__).parent.parent
CONFIG_PATH = BASE_DIR / "artifacts" / "live_model_config.json"


class ModelRegistry:
    def __init__(self):
        self.config          = None
        self.model           = None
        self.scaler          = None
        self.feature_names   = None
        self.active_name     = None
        self.requires_sequence = False
        self.window_size     = 10
        self.n_features      = 74
        self.label_map       = {}

    def load(self):
        logging.info("Loading model registry from config......")

        with open(CONFIG_PATH) as f:
            self.config = json.load(f)

        self.active_name   = self.config["active_model"]
        self.window_size   = self.config["window_size"]
        self.n_features    = self.config["n_features"]
        self.n_classes     = self.config["n_classes"]
        self.label_map     = self.config["label_map"]

        model_config          = self.config["models"][self.active_name]
        self.requires_sequence = model_config["requires_sequence"]

        model_path   = BASE_DIR / model_config["path"]
        scaler_path  = BASE_DIR / model_config["scaler_path"]
        features_path = BASE_DIR / model_config["features_path"]

        self.scaler = joblib.load(scaler_path)
        logging.info(f"Scaler loaded from {scaler_path}")

        # Load feature names
        with open(features_path) as f:
            self.feature_names = [line.strip() for line in f.readlines()]
        logging.info(f"Loaded {len(self.feature_names)} feature names")

        try:
            if model_config["type"] == "keras":
                self.model = tf.keras.models.load_model(model_path)
                logging.info(f"Keras model loaded: {self.active_name}")
            elif model_config["type"] == "sklearn":
                self.model = joblib.load(model_path)
                logging.info(f"Sklearn model loaded: {self.active_name}")
            else:
                raise ValueError(f"Unknown model type: {model_config['type']}")
            logging.info(f"Model loaded from {model_path}")
        except FileNotFoundError:
            logging.error(f"Model not found at {model_path}")
            raise
        except Exception as e:
            logging.error(f"Failed to load model from {model_path}: {e}")
            raise

    def predict_proba(self, X: np.ndarray) -> np.ndarray:
        if self.config["model"][self.active_name]["type"] == "keras":
            return self.model.predict(X, verbose=0)
        else:
            return self.model.predict_proba(X)

    def decode_prediction(self, proba: np.ndarray) -> dict:
        predicted_class = int(np.argmax(proba))
        confidence      = round(float(proba[predicted_class]), 4)
        label           = self.label_map[str(predicted_class)]

        return {
            "predicted_class": predicted_class,
            "label":           label,
            "confidence":      confidence,
            "probabilities": {
                self.label_map["0"]: round(float(proba[0]), 4),
                self.label_map["1"]: round(float(proba[1]), 4),
                self.label_map["2"]: round(float(proba[2]), 4),
            }
        }


registry = ModelRegistry()