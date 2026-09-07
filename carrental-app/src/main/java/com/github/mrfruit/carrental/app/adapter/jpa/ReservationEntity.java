package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.availability.values.BlockadeId;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.common.CustomerId;
import com.github.mrfruit.carrental.domain.common.ReservationId;
import com.github.mrfruit.carrental.domain.reservation.entity.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "reservation")
class ReservationEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "branch_id", nullable = false)
    private String branchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_class", nullable = false)
    private CarClass carClass;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "pick_up_time", nullable = false)
    private LocalTime pickUpTime;

    @Column(name = "blockade_id", nullable = false)
    private UUID blockadeId;

    protected ReservationEntity() {
    }

    static ReservationEntity from(Reservation reservation) {
        var entity = new ReservationEntity();
        entity.id = reservation.id().value();
        entity.customerId = reservation.customerId().value();
        entity.branchId = reservation.branchId().value();
        entity.carClass = reservation.carClass();
        entity.periodStart = reservation.period().start();
        entity.periodEnd = reservation.period().end();
        entity.pickUpTime = reservation.pickUpTime();
        entity.blockadeId = reservation.blockadeId().value();
        return entity;
    }

    Reservation toDomain() {
        return new Reservation(
                new ReservationId(id),
                new CustomerId(customerId),
                new BranchId(branchId),
                carClass,
                new DateRange(periodStart, periodEnd),
                pickUpTime,
                new BlockadeId(blockadeId));
    }
}
