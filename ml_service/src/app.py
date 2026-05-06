import logging
import traceback
from flask import Flask, request, jsonify
from flask_cors import CORS
from models import registry
from predictor import predictor

logging.basicConfig(
    level = logging.INFO,
    format = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

app = Flask(__name__)
CORS(app)


@app.before_request
def startup():
    pass
try:
    registry.load()
    logging.info("Model registry loaded successflly")
except Exception as e:
    logging.error(f"FATAL: Could not load model: {e}")
    raise


@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "ok",
        "active_model": registry.active_model,
        "window_size": registry.window_size,
        "n_features": registry.n_features,
        "buffer_status": predictor.get_buffer_status()
    }),200


@app.route("/predict", methods=["POST"])
def predict_live():
    try:
        raw = request.get_json()

        if not raw:
            return jsonify({
                "error": "No JSON body received"
            }), 400

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
            "error"  : "Prediction failed",
            "detail" : str(e)
        }), 500

app.route("/reset/<device_id>", methods=["POST"])
def reset_buffer(device_id):
    predictor.reset_buffer(device_id)
    return jsonify({
        "status": "ok",
        "device_id": device_id,
        "message": f"Buffer reset for {device_id}"
    }), 200

if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port = 5000,
        debug = True,    # remove in production
        Threaded = True
    )