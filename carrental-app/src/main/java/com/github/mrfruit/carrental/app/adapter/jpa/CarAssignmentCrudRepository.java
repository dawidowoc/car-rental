package com.github.mrfruit.carrental.app.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface CarAssignmentCrudRepository extends JpaRepository<CarAssignmentEntity, UUID> {

    Optional<CarAssignmentEntity> findByReservationId(UUID reservationId);
}
