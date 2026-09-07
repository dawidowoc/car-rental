package com.github.mrfruit.carrental.domain.fleet.repository;

import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.fleet.entity.Car;
import com.github.mrfruit.carrental.domain.fleet.values.CarId;

import java.util.List;
import java.util.Optional;

public interface CarRepository {

    Optional<Car> findById(CarId carId);

    List<Car> findAvailableForUpdate(BranchId branchId, CarClass carClass);

    void save(Car car);
}
