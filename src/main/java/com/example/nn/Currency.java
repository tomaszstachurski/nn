package com.example.nn;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.yaml.snakeyaml.util.EnumUtils;

public enum Currency {
    PLN, USD;

    @JsonCreator
    public static Currency forValue(String name) {
        return EnumUtils.findEnumInsensitiveCase(Currency.class, name);
    }
}
