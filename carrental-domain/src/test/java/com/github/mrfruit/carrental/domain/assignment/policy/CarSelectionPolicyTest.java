package com.github.mrfruit.carrental.domain.assignment.policy;

import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.fleet.entity.Car;
import com.github.mrfruit.carrental.domain.fleet.values.Vin;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarSelectionPolicyTest {

    private static final BranchId BRANCH = new BranchId("wroclaw-1");
    private static final CarClass CAR_CLASS = CarClass.SUV;

    private final CarSelectionPolicy policy = new CarSelectionPolicy();

    @Test
    void selectsFirstOfTheCandidates() {
        // given
        var first = car("WBA1111111111AAAA");
        var second = car("WBA2222222222BBBB");

        // when
        var selected = policy.select(List.of(first, second));

        // then
        assertThat(selected).contains(first);
    }

    @Test
    void selectsNothingWhenThereAreNoCandidates() {
        // when
        var selected = policy.select(List.of());

        // then
        assertThat(selected).isEmpty();
    }

    private static Car car(String vin) {
        return Car.newCar(new Vin(vin), CAR_CLASS, BRANCH);
    }
}
