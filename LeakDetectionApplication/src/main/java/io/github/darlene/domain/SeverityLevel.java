package io.github.darlene.leakdetection.domain;

import lombok.Getter;

/**
 * Represents the severity level of a detected pipeline fault.
 * Priority indicates urgency: 0 = none, 3 = critical.
 */

@Getter
public enum SeverityLevel{
    NONE("No fault detected",0),
    LOW("Incipient fault", 1),
    MODERATE("Developing fault", 2),
    CRITICAL("Critical fault", 3);

    private final String description;
    private final int priorityLevel;

    SeverityLevel(String description, int priorityLevel){
        this.description = description;
        this.priorityLevel = priorityLevel;
    }
}