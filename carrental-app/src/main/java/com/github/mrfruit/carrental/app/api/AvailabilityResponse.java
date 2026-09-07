package com.github.mrfruit.carrental.app.api;

import com.github.mrfruit.carrental.app.service.AvailableCarClass;

import java.util.List;

public record AvailabilityResponse(List<AvailableCarClass> available) {
}
