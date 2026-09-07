package com.github.mrfruit.carrental.domain.common;

import java.util.UUID;

public record ReservationId(UUID value) {

    public static ReservationId newId() {
        return new ReservationId(UUID.randomUUID());
    }
}
