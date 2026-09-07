package com.github.mrfruit.carrental.domain.assignment.entity;

import com.github.mrfruit.carrental.domain.assignment.values.AssignmentId;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.ReservationId;
import com.github.mrfruit.carrental.domain.fleet.values.CarId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CarAssignmentTest {

    private static final LocalDate START = LocalDate.of(2026, 9, 8);
    private static final DateRange PERIOD = new DateRange(START, START.plusDays(2));

    @Test
    void newAssignmentBindsCarToReservationForPeriod() {
        // given
        var reservationId = ReservationId.newId();
        var carId = CarId.newId();

        // when
        var assignment = CarAssignment.newAssignment(reservationId, carId, PERIOD);

        // then
        assertThat(assignment.id()).isNotNull();
        assertThat(assignment.reservationId()).isEqualTo(reservationId);
        assertThat(assignment.carId()).isEqualTo(carId);
        assertThat(assignment.period()).isEqualTo(PERIOD);
    }

    @Test
    void eachNewAssignmentGetsItsOwnId() {
        // given
        var carId = CarId.newId();

        // when
        var first = CarAssignment.newAssignment(ReservationId.newId(), carId, PERIOD);
        var second = CarAssignment.newAssignment(ReservationId.newId(), carId, PERIOD);

        // then
        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void assignmentsWithSameIdAreEqualRegardlessOfCar() {
        // given
        var id = AssignmentId.newId();
        var reservationId = ReservationId.newId();
        var first = new CarAssignment(id, reservationId, CarId.newId(), PERIOD);
        var second = new CarAssignment(id, reservationId, CarId.newId(), PERIOD);

        // then
        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }
}
