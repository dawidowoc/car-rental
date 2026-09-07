package com.github.mrfruit.carrental.domain.fleet.entity;

import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.fleet.values.CarStatus;
import com.github.mrfruit.carrental.domain.fleet.values.Vin;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class CarTest {

    private static final BranchId WROCLAW = new BranchId("wroclaw-1");
    private static final Vin VIN = new Vin("WBA1234567890ABCD");

    @Test
    void newCarIsAvailable() {
        // when
        var car = Car.newCar(VIN, CarClass.SUV, WROCLAW);

        // then
        assertThat(car.status()).isEqualTo(CarStatus.AVAILABLE);
        assertThat(car.isAvailable()).isTrue();
    }

    @Test
    void pickedUpCarIsRented() {
        // given
        var car = Car.newCar(VIN, CarClass.SUV, WROCLAW);

        // when
        car.pickUp();

        // then
        assertThat(car.status()).isEqualTo(CarStatus.RENTED);
        assertThat(car.isAvailable()).isFalse();
    }

    @Test
    void rentedCarCannotBePickedUpAgain() {
        // given
        var car = Car.newCar(VIN, CarClass.SUV, WROCLAW);
        car.pickUp();

        // then
        assertThatIllegalStateException().isThrownBy(car::pickUp);
    }

    @Test
    void droppedOffCarIsAvailableAgain() {
        // given
        var car = Car.newCar(VIN, CarClass.SUV, WROCLAW);
        car.pickUp();

        // when
        car.dropOff();

        // then
        assertThat(car.status()).isEqualTo(CarStatus.AVAILABLE);
        assertThat(car.isAvailable()).isTrue();
    }

    @Test
    void availableCarCannotBeDroppedOff() {
        // given
        var car = Car.newCar(VIN, CarClass.SUV, WROCLAW);

        // then
        assertThatIllegalStateException().isThrownBy(car::dropOff);
    }

    @Test
    void droppedOffCarCanBePickedUpAgain() {
        // given
        var car = Car.newCar(VIN, CarClass.SUV, WROCLAW);
        car.pickUp();
        car.dropOff();

        // when
        car.pickUp();

        // then
        assertThat(car.status()).isEqualTo(CarStatus.RENTED);
    }
}
