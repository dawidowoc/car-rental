package com.github.mrfruit.carrental.domain.assignment.policy;

import com.github.mrfruit.carrental.domain.fleet.entity.Car;

import java.util.List;
import java.util.Optional;

public class CarSelectionPolicy {

    public Optional<Car> select(List<Car> candidates) {
        return candidates.stream().findFirst();
    }
}
