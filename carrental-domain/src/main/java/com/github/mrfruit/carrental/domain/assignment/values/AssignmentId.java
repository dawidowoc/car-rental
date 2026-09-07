package com.github.mrfruit.carrental.domain.assignment.values;

import java.util.UUID;

public record AssignmentId(UUID value) {

    public static AssignmentId newId() {
        return new AssignmentId(UUID.randomUUID());
    }
}
