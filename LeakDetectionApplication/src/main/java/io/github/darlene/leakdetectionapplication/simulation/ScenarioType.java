package io.github.darlene.leakdetectionapplication.simulation;

import io.github.darlene.leakdetectionapplication.alert.*;
import io.github.darlene.leakdetectionapplication.analytics.*;
import io.github.darlene.leakdetectionapplication.auth.*;
import io.github.darlene.leakdetectionapplication.configuration.*;
import io.github.darlene.leakdetectionapplication.messaging.*;
import io.github.darlene.leakdetectionapplication.monitoring.*;
import io.github.darlene.leakdetectionapplication.pipeline.*;
import io.github.darlene.leakdetectionapplication.recommendation.*;
import io.github.darlene.leakdetectionapplication.sensor.*;
import io.github.darlene.leakdetectionapplication.simulation.*;
import io.github.darlene.leakdetectionapplication.shared.*;

import lombok.Getter;

@Getter
public enum ScenarioType {
    NORMAL_BASELINE("Normal baseline operation"),
    LEAK_INCIPIENT("Early stage leak detected"),
    LEAK_MODERATE("Moderate leak detected"),
    LEAK_CRITICAL("Critical leak detected"),
    BLOCKAGE_25("25% blockage detected"),
    BLOCKAGE_50("50% blockage detected"),
    BLOCKAGE_75("75% blockage detected");

    private final String description;

    ScenarioType(String description) {
        this.description = description;
    }
}