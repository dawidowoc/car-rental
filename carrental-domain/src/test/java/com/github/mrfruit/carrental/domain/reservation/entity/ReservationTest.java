package com.github.mrfruit.carrental.domain.reservation.entity;

import com.github.mrfruit.carrental.domain.availability.values.BlockadeId;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.common.CustomerId;
import com.github.mrfruit.carrental.domain.common.ReservationId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTest {

    private static final BranchId BRANCH = new BranchId("wroclaw-1");
    private static final LocalDate START = LocalDate.of(2026, 9, 8);
    private static final DateRange PERIOD = new DateRange(START, START.plusDays(2));
    private static final LocalTime PICK_UP_TIME = LocalTime.of(10, 0);

    @Test
    void newReservationIsConfirmedAndKeepsWhatItBooked() {
        // given
        var customerId = CustomerId.newId();
        var blockadeId = BlockadeId.newId();

        // when
        var reservation = Reservation.confirmedReservation(customerId, BRANCH, CarClass.SUV, PERIOD, PICK_UP_TIME, blockadeId);

        // then
        assertThat(reservation.id()).isNotNull();
        assertThat(reservation.customerId()).isEqualTo(customerId);
        assertThat(reservation.branchId()).isEqualTo(BRANCH);
        assertThat(reservation.carClass()).isEqualTo(CarClass.SUV);
        assertThat(reservation.period()).isEqualTo(PERIOD);
        assertThat(reservation.pickUpTime()).isEqualTo(PICK_UP_TIME);
        assertThat(reservation.blockadeId()).isEqualTo(blockadeId);
    }

    @Test
    void eachNewReservationGetsItsOwnId() {
        // when
        var first = confirmedReservation();
        var second = confirmedReservation();

        // then
        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void reservationsWithSameIdAreEqualRegardlessOfWhatTheyBooked() {
        // given
        var id = ReservationId.newId();
        var sedan = new Reservation(id, CustomerId.newId(), BRANCH, CarClass.SEDAN, PERIOD,
                PICK_UP_TIME, BlockadeId.newId());
        var van = new Reservation(id, CustomerId.newId(), new BranchId("krakow-1"), CarClass.VAN,
                new DateRange(START.plusDays(9), START.plusDays(10)), PICK_UP_TIME, BlockadeId.newId());

        // then
        assertThat(sedan).isEqualTo(van);
        assertThat(sedan).hasSameHashCodeAs(van);
    }

    private static Reservation confirmedReservation() {
        return Reservation.confirmedReservation(CustomerId.newId(), BRANCH, CarClass.SUV, PERIOD, PICK_UP_TIME, BlockadeId.newId());
    }
}
