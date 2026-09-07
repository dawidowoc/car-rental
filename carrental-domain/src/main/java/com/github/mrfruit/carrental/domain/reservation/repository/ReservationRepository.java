package com.github.mrfruit.carrental.domain.reservation.repository;

import com.github.mrfruit.carrental.domain.common.ReservationId;
import com.github.mrfruit.carrental.domain.reservation.entity.Reservation;

import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findById(ReservationId reservationId);

    void save(Reservation reservation);
}
