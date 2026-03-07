package com.aumReport.aum.enums;

import lombok.Getter;

@Getter
public enum AdvisorType {
    NOT_ASSIGNED("Not Assigned"),
    PATWA("Patwa"),
    INDIA("India"),
    AVESTAR("Avestar");

    private final String value;

    AdvisorType(String value) {
        this.value = value;
    }
}
