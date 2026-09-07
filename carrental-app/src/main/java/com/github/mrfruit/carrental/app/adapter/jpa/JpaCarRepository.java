package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.fleet.entity.Car;
import com.github.mrfruit.carrental.domain.fleet.repository.CarRepository;
import com.github.mrfruit.carrental.domain.fleet.values.CarId;
import com.github.mrfruit.carrental.domain.fleet.values.CarStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class JpaCarRepository implements CarRepository {

    private final CarCrudRepository crudRepository;

    JpaCarRepository(CarCrudRepository crudRepository) {
        this.crudRepository = crudRepository;
    }

    @Override
    public Optional<Car> findById(CarId carId) {
        return crudRepository.findById(carId.value()).map(CarEntity::toDomain);
    }

    @Override
    public List<Car> findAvailableForUpdate(BranchId branchId, CarClass carClass) {
        return crudRepository.findAvailableForUpdate(branchId.value(), carClass, CarStatus.AVAILABLE).stream()
                .map(CarEntity::toDomain)
                .toList();
    }

    @Override
    public void save(Car car) {
        crudRepository.save(CarEntity.from(car));
    }
}
