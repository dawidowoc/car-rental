package com.github.mrfruit.carrental.domain.reservation.entity;

import com.github.mrfruit.carrental.domain.availability.values.BlockadeId;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.common.CustomerId;
import com.github.mrfruit.carrental.domain.common.ReservationId;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class Reservation {

    private final ReservationId id;
    private final CustomerId customerId;
    private final BranchId branchId;
    private final CarClass carClass;
    private final DateRange period;
    private final LocalTime pickUpTime;
    private final BlockadeId blockadeId;

    public Reservation(ReservationId id, CustomerId customerId, BranchId branchId, CarClass carClass,
                       DateRange period, LocalTime pickUpTime, BlockadeId blockadeId) {
        this.id = id;
        this.customerId = customerId;
        this.branchId = branchId;
        this.carClass = carClass;
        this.period = period;
        this.pickUpTime = pickUpTime;
        this.blockadeId = blockadeId;
    }

    public static Reservation confirmedReservation(CustomerId customerId, BranchId branchId, CarClass carClass,
                                                   DateRange period, LocalTime pickUpTime, BlockadeId blockadeId) {
        return new Reservation(ReservationId.newId(), customerId, branchId, carClass, period, pickUpTime, blockadeId);
    }

    public ReservationId id() {
        return id;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public BranchId branchId() {
        return branchId;
    }

    public CarClass carClass() {
        return carClass;
    }

    public DateRange period() {
        return period;
    }

    public LocalTime pickUpTime() {
        return pickUpTime;
    }

    public LocalDateTime pickUpAt() {
        return period.start().atTime(pickUpTime);
    }

    public BlockadeId blockadeId() {
        return blockadeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Reservation{id=" + id.value()
                + ", customerId=" + customerId.value()
                + ", branchId=" + branchId.value()
                + ", carClass=" + carClass
                + ", period=" + period
                + ", pickUpTime=" + pickUpTime
                + ", blockadeId=" + blockadeId.value()
                + '}';
    }
}
