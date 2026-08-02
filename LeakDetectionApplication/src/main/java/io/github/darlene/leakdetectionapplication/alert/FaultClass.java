package io.github.darlene.leakdetectionapplication.alert;

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
/**
 * Represents the type of fault that was detected
 * We have Normal, Leak, Blockage
 */
// Lombok generates getters - no manual getter methods needed


@Getter
public enum FaultClass {
    NORMAL("Pipeline operating within the normal parameters"),
    LEAK("Abrasive leak signature detected in pressure profile"),
    BLOCKAGE("Partial blockage detected - flow restriction present");

    private final String description;

    FaultClass(String description){
        this.description = description;
    }

}