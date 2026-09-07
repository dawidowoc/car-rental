package com.github.mrfruit.carrental.domain.assignment.entity;

import com.github.mrfruit.carrental.domain.assignment.values.AssignmentId;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.ReservationId;
import com.github.mrfruit.carrental.domain.fleet.values.CarId;

public class CarAssignment {

    private final AssignmentId id;
    private final ReservationId reservationId;
    private final CarId carId;
    private final DateRange period;

    public CarAssignment(AssignmentId id, ReservationId reservationId, CarId carId, DateRange period) {
        this.id = id;
        this.reservationId = reservationId;
        this.carId = carId;
        this.period = period;
    }

    public static CarAssignment newAssignment(ReservationId reservationId, CarId carId, DateRange period) {
        return new CarAssignment(AssignmentId.newId(), reservationId, carId, period);
    }

    public AssignmentId id() {
        return id;
    }

    public ReservationId reservationId() {
        return reservationId;
    }

    public CarId carId() {
        return carId;
    }

    public DateRange period() {
        return period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CarAssignment other)) {
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
        return "CarAssignment{id=" + id.value()
                + ", reservationId=" + reservationId.value()
                + ", carId=" + carId.value()
                + ", period=" + period
                + '}';
    }
}
