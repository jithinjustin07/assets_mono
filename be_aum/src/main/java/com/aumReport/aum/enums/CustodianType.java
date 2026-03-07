package com.aumReport.aum.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum CustodianType {

    ALTERNATIVE_INVESTMENT("Alternative Investment", List.of(83, 4)),;

    private final String displayName;
    private final List<Integer> ids;

    CustodianType(String displayName, List<Integer> ids) {
        this.displayName = displayName;
        this.ids = ids;
    }
}
