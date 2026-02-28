package io.github.darlene.leakdetection.domain;

import lombok.Getter;

/**
 * Represents the overall operational status of the pipeline system.
 * Used for dashboard display, WebSocket broadcasts and ESP32 LED control.
 * colorCode provides hex values for direct frontend rendering.
 */

@Getter

public enum SystemStatus{
    NORMAL("Pipeline Operating Normally", "#00FF00", false),
    LEAK_DETECTED("Leak detected - operator action required", "#FF0000", true),
    BLOCKAGE_DETECTED("Blockage detected - operator action required", "#FFA500", true),
    OFFLINE("System offline - no data received", "#808080", true);

    private final String description;
    private final String colorCode;
    private final boolean requiresAction;

    SystemStatus(String description, String colorCode, boolean requiresAction){
        this.description = description;
        this.colorCode = colorCode;
        this.requiresAction = requiresAction;
    }
}