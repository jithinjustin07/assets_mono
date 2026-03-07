package com.aumReport.aum.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum VendorType {
    Black_Diamond("Black Diamond"),
    Addepar("Addepar");

    private final String value;

    VendorType(String value) {
        this.value = value;
    }
    
    public static List<String> getAllVendorNames() {
        return Arrays.stream(VendorType.values())
                .map(VendorType::getValue)
                .collect(Collectors.toList());
    }
}
