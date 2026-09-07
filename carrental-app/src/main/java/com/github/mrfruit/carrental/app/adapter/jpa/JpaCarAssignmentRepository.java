package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.assignment.entity.CarAssignment;
import com.github.mrfruit.carrental.domain.assignment.repository.CarAssignmentRepository;
import com.github.mrfruit.carrental.domain.assignment.values.AssignmentId;
import com.github.mrfruit.carrental.domain.common.ReservationId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class JpaCarAssignmentRepository implements CarAssignmentRepository {

    private final CarAssignmentCrudRepository crudRepository;

    JpaCarAssignmentRepository(CarAssignmentCrudRepository crudRepository) {
        this.crudRepository = crudRepository;
    }

    @Override
    public Optional<CarAssignment> findById(AssignmentId assignmentId) {
        return crudRepository.findById(assignmentId.value()).map(CarAssignmentEntity::toDomain);
    }

    @Override
    public Optional<CarAssignment> findByReservationId(ReservationId reservationId) {
        return crudRepository.findByReservationId(reservationId.value()).map(CarAssignmentEntity::toDomain);
    }

    @Override
    public void save(CarAssignment assignment) {
        crudRepository.save(CarAssignmentEntity.from(assignment));
    }
}
