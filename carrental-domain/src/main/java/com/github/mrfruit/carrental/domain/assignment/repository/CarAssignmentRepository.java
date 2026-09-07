package com.github.mrfruit.carrental.domain.assignment.repository;

import com.github.mrfruit.carrental.domain.assignment.entity.CarAssignment;
import com.github.mrfruit.carrental.domain.assignment.values.AssignmentId;
import com.github.mrfruit.carrental.domain.common.ReservationId;

import java.util.Optional;

public interface CarAssignmentRepository {

    Optional<CarAssignment> findById(AssignmentId assignmentId);

    Optional<CarAssignment> findByReservationId(ReservationId reservationId);

    void save(CarAssignment assignment);
}
