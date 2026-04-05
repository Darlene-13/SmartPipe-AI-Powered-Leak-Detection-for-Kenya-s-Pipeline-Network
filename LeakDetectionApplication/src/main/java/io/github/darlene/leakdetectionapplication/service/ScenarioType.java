package io.github.darlene.leakdetectionapplication.service;

import lombok.Getter;

@Getter
public enum ScenarioType {
    "NORMAL_BASELINE",
    "LEAK_INCIPIENT",
    "LEAK_MODERATE",
    "LEAK_CRITICAL",
    "BLOCKAGE_25",
    "BLOCKAGE_50",
    "BLOCKAGE_75";

    private String description;
    ScenarioType(String description){
        this.description = description;
    }
}