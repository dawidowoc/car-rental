package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.fleet.entity.Car;
import com.github.mrfruit.carrental.domain.fleet.values.CarId;
import com.github.mrfruit.carrental.domain.fleet.values.CarStatus;
import com.github.mrfruit.carrental.domain.fleet.values.Vin;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "car")
class CarEntity {

    @Id
    private UUID id;

    @Column(name = "vin", nullable = false)
    private String vin;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_class", nullable = false)
    private CarClass carClass;

    @Column(name = "branch_id", nullable = false)
    private String branchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CarStatus status;

    protected CarEntity() {
    }

    static CarEntity from(Car car) {
        var entity = new CarEntity();
        entity.id = car.id().value();
        entity.vin = car.vin().value();
        entity.carClass = car.carClass();
        entity.branchId = car.branchId().value();
        entity.status = car.status();
        return entity;
    }

    Car toDomain() {
        return new Car(new CarId(id), new Vin(vin), carClass, new BranchId(branchId), status);
    }
}
