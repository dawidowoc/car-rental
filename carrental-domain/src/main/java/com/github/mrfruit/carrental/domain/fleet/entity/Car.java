package com.github.mrfruit.carrental.domain.fleet.entity;

import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.fleet.values.CarId;
import com.github.mrfruit.carrental.domain.fleet.values.CarStatus;
import com.github.mrfruit.carrental.domain.fleet.values.Vin;

public class Car {

    private final CarId id;
    private final Vin vin;
    private final CarClass carClass;
    private final BranchId branchId;
    private CarStatus status;

    public Car(CarId id, Vin vin, CarClass carClass, BranchId branchId, CarStatus status) {
        this.id = id;
        this.vin = vin;
        this.carClass = carClass;
        this.branchId = branchId;
        this.status = status;
    }

    public static Car newCar(Vin vin, CarClass carClass, BranchId branchId) {
        return new Car(CarId.newId(), vin, carClass, branchId, CarStatus.AVAILABLE);
    }

    public void pickUp() {
        if (status != CarStatus.AVAILABLE) {
            throw new IllegalStateException("car " + id.value() + " cannot be picked up while " + status);
        }
        status = CarStatus.RENTED;
    }

    public void dropOff() {
        if (status != CarStatus.RENTED) {
            throw new IllegalStateException("car " + id.value() + " cannot be dropped off while " + status);
        }
        status = CarStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return status == CarStatus.AVAILABLE;
    }

    public CarId id() {
        return id;
    }

    public Vin vin() {
        return vin;
    }

    public CarClass carClass() {
        return carClass;
    }

    public BranchId branchId() {
        return branchId;
    }

    public CarStatus status() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Car other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Car{id=" + id.value()
                + ", vin=" + vin.value()
                + ", carClass=" + carClass
                + ", branchId=" + branchId.value()
                + ", status=" + status
                + '}';
    }
}
