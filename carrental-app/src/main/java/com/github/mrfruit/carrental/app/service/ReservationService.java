package com.github.mrfruit.carrental.app.service;

import com.github.mrfruit.carrental.domain.availability.service.BlockadeService;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.common.CustomerId;
import com.github.mrfruit.carrental.domain.common.ReservationId;
import com.github.mrfruit.carrental.domain.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

import static com.github.mrfruit.carrental.domain.reservation.entity.Reservation.confirmedReservation;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final BlockadeService blockadeService;

    public ReservationService(ReservationRepository reservationRepository, BlockadeService blockadeService) {
        this.reservationRepository = reservationRepository;
        this.blockadeService = blockadeService;
    }

    public ReservationId makeReservation(CustomerId customerId, BranchId branchId, CarClass carClass,
                                         DateRange period, LocalTime pickUpTime) {
        var blockadeId = blockadeService.block(branchId, carClass, period);

        var reservation = confirmedReservation(customerId, branchId, carClass, period, pickUpTime, blockadeId);
        reservationRepository.save(reservation);
        return reservation.id();
    }
}
