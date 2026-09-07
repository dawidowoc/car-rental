package com.github.mrfruit.carrental.domain.fleet.values;

import java.util.UUID;

public record CarId(UUID value) {

    public static CarId newId() {
        return new CarId(UUID.randomUUID());
    }
}
