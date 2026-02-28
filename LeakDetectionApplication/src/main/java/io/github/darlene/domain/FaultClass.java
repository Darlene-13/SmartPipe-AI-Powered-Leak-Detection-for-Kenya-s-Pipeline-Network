package io.github.darlene.leakdetection.domain;
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