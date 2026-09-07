package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.common.ReservationId;
import com.github.mrfruit.carrental.domain.reservation.entity.Reservation;
import com.github.mrfruit.carrental.domain.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class JpaReservationRepository implements ReservationRepository {

    private final ReservationCrudRepository crudRepository;

    JpaReservationRepository(ReservationCrudRepository crudRepository) {
        this.crudRepository = crudRepository;
    }

    @Override
    public Optional<Reservation> findById(ReservationId reservationId) {
        return crudRepository.findById(reservationId.value()).map(ReservationEntity::toDomain);
    }

    @Override
    public void save(Reservation reservation) {
        crudRepository.save(ReservationEntity.from(reservation));
    }
}
