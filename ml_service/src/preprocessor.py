from typing import Any

import numpy as np
import pandas as pd
import logging
from models import registry


def engineer_features(raw:dict) -> pd.DataFrame:

    epsilon = 1e-8

    a_p = raw["node_a_pressure"]
    b_p = raw["node_b_pressure"]
    c_p = raw["node_c_pressure"]
    v_a = raw["velocity_a"]
    v_b = raw["velocity_b"]
    v_c = raw["velocity_c"]

    features: dict[Any, Any] = {}

    # Raw readings
    features["node_a_pressure"] = a_p
    features["node_b_pressure"] = b_p
    features["node_c_pressure"] = c_p
    features["velocity_a"]      = v_a
    features["velocity_b"]      = v_b
    features["velocity_c"]      = v_c

    # Pressure drops
    features["pressure_drop_ab"] = a_p - b_p
    features["pressure_drop_bc"] = b_p - c_p
    features["pressure_drop_ac"] = a_p - c_p

    # Pressure gradients
    features["pressure_gradient_ab"] = features["pressure_drop_ab"] / 20.0
    features["pressure_gradient_bc"] = features["pressure_drop_bc"] / 20.0
    features["pressure_gradient_ac"] = features["pressure_drop_ac"] / 40.0

    # Midpoint pressure deviation
    features["expected_midpoint_pressure"]  = (a_p + c_p) / 2
    features["midpoint_pressure_deviation"] = b_p - features["expected_midpoint_pressure"]

    # Pressure asymmetry
    features["pressure_asymmetry"] = abs(
        features["pressure_drop_ab"] - features["pressure_drop_bc"]
    )

    # Pressure ratios
    features["pressure_ratio_ab"]        = a_p / (b_p + epsilon)
    features["pressure_ratio_bc"]        = b_p / (c_p + epsilon)
    features["pressure_ratio_ac"]        = a_p / (c_p + epsilon)
    features["upstream_downstream_ratio"] = a_p / (c_p + epsilon)

    # dp_dt — passed in from Spring Boot Java calculation
    # Java already computes these, Flask just passes them through
    features["dp_dt_a"] = raw.get("dp_dt_a", 0.0)
    features["dp_dt_b"] = raw.get("dp_dt_b", 0.0)
    features["dp_dt_c"] = raw.get("dp_dt_c", 0.0)

    # Second derivatives — computed from dp_dt values passed in
    features["d2p_dt2_a"] = raw.get("d2p_dt2_a", 0.0)
    features["d2p_dt2_b"] = raw.get("d2p_dt2_b", 0.0)
    features["d2p_dt2_c"] = raw.get("d2p_dt2_c", 0.0)

    # Velocity drops
    features["velocity_drop_ab"] = v_a - v_b
    features["velocity_drop_bc"] = v_b - v_c
    features["velocity_drop_ac"] = v_a - v_c

    # Mean and deviation
    features["mean_velocity"]         = (v_a + v_b + v_c) / 3
    features["velocity_deviation_a"]  = v_a - features["mean_velocity"]
    features["velocity_deviation_b"]  = v_b - features["mean_velocity"]
    features["velocity_deviation_c"]  = v_c - features["mean_velocity"]
    features["velocity_std"]          = np.std([v_a, v_b, v_c])

    # Midpoint velocity deviation
    features["expected_midpoint_velocity"]  = (v_a + v_c) / 2
    features["midpoint_velocity_deviation"] = v_b - features["expected_midpoint_velocity"]

    # Velocity temporal rates — from Java
    features["dv_dt_a"]   = raw.get("dv_dt_a",   0.0)
    features["dv_dt_b"]   = raw.get("dv_dt_b",   0.0)
    features["dv_dt_c"]   = raw.get("dv_dt_c",   0.0)
    features["d2v_dt2_a"] = raw.get("d2v_dt2_a", 0.0)
    features["d2v_dt2_b"] = raw.get("d2v_dt2_b", 0.0)
    features["d2v_dt2_c"] = raw.get("d2v_dt2_c", 0.0)

    # Hydraulic power
    features["hydraulic_power_a"] = a_p * v_a
    features["hydraulic_power_b"] = b_p * v_b
    features["hydraulic_power_c"] = c_p * v_c
    features["power_loss_ab"]     = features["hydraulic_power_a"] - features["hydraulic_power_b"]
    features["power_loss_bc"]     = features["hydraulic_power_b"] - features["hydraulic_power_c"]
    features["power_loss_ac"]     = features["hydraulic_power_a"] - features["hydraulic_power_c"]

    # Pressure-velocity ratio at B
    features["pv_ratio_b"] = b_p / (v_b ** 2 + epsilon)

    # Momentum
    features["momentum_a"]       = (v_a ** 2) * 0.40
    features["momentum_b"]       = (v_b ** 2) * 0.40
    features["momentum_c"]       = (v_c ** 2) * 0.40
    features["momentum_loss_ab"] = features["momentum_a"] - features["momentum_b"]
    features["momentum_loss_bc"] = features["momentum_b"] - features["momentum_c"]

    # Localization indicators
    features["leak_pressure_indicator"]    = 1 if features["midpoint_pressure_deviation"] < -200 else 0
    features["blockage_pressure_indicator"] = 1 if (
        features["midpoint_pressure_deviation"] > 500 and
        features["upstream_downstream_ratio"] > 8.5
    ) else 0
    features["blockage_velocity_indicator"] = 1 if features["midpoint_velocity_deviation"] < -0.05 else 0
    features["leak_score"] = features["leak_pressure_indicator"]

    # Rolling features — passed in from Java window buffer
    features["rolling_mean_node_a_pressure"]              = raw.get("rolling_mean_node_a_pressure", a_p)
    features["rolling_std_node_a_pressure"]               = raw.get("rolling_std_node_a_pressure",  0.0)
    features["rolling_mean_node_b_pressure"]              = raw.get("rolling_mean_node_b_pressure", b_p)
    features["rolling_std_node_b_pressure"]               = raw.get("rolling_std_node_b_pressure",  0.0)
    features["rolling_mean_node_c_pressure"]              = raw.get("rolling_mean_node_c_pressure", c_p)
    features["rolling_std_node_c_pressure"]               = raw.get("rolling_std_node_c_pressure",  0.0)
    features["rolling_mean_velocity_b"]                   = raw.get("rolling_mean_velocity_b",      v_b)
    features["rolling_std_velocity_b"]                    = raw.get("rolling_std_velocity_b",       0.0)
    features["rolling_mean_pressure_drop_bc"]             = raw.get("rolling_mean_pressure_drop_bc",
                                                                      features["pressure_drop_bc"])
    features["rolling_std_pressure_drop_bc"]              = raw.get("rolling_std_pressure_drop_bc",  0.0)
    features["rolling_mean_midpoint_pressure_deviation"]  = raw.get("rolling_mean_midpoint_pressure_deviation",
                                                                      features["midpoint_pressure_deviation"])
    features["rolling_std_midpoint_pressure_deviation"]   = raw.get("rolling_std_midpoint_pressure_deviation", 0.0)
    features["rolling_mean_midpoint_velocity_deviation"]  = raw.get("rolling_mean_midpoint_velocity_deviation",
                                                                      features["midpoint_velocity_deviation"])
    features["rolling_std_midpoint_velocity_deviation"]   = raw.get("rolling_std_midpoint_velocity_deviation", 0.0)

    return features


def preprocess(raw:dict) -> dict:
    features = engineer_features(raw)

    ordered = [features.get(f, 0.0) for f in registry.feature_names]
    df = pd.DataFrame.from_records([ordered],  columns = registry.feature_names)

    scaled = registry.scalar.transform(df)

    return scaled.astype(np.float32)  #keras uses float 32 by default, 64 would work though.
