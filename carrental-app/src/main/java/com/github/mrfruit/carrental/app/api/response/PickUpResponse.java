package com.github.mrfruit.carrental.app.api.response;

import java.util.UUID;

public record PickUpResponse(UUID assignmentId, UUID carId) {
}
