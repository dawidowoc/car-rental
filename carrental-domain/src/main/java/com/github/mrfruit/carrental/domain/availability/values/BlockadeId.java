package com.github.mrfruit.carrental.domain.availability.values;

import java.util.UUID;

public record BlockadeId(UUID value) {

    public static BlockadeId newId() {
        return new BlockadeId(UUID.randomUUID());
    }
}
