package com.github.mrfruit.carrental.app.service;

import com.github.mrfruit.carrental.domain.common.CarClass;

public record AvailableCarClass(CarClass carClass, int availableCount) {
}
