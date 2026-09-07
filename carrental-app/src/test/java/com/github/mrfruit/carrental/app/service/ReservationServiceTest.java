package com.github.mrfruit.carrental.app.service;

import com.github.mrfruit.carrental.domain.availability.exception.SlotUnavailableException;
import com.github.mrfruit.carrental.domain.availability.service.BlockadeService;
import com.github.mrfruit.carrental.domain.availability.values.BlockadeId;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.common.CustomerId;
import com.github.mrfruit.carrental.domain.reservation.entity.Reservation;
import com.github.mrfruit.carrental.domain.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final BranchId BRANCH = new BranchId("wroclaw-1");
    private static final CarClass CAR_CLASS = CarClass.SUV;
    private static final LocalDate START = LocalDate.of(2026, 9, 8);
    private static final DateRange PERIOD = new DateRange(START, START.plusDays(2));
    private static final LocalTime PICK_UP_TIME = LocalTime.of(10, 0);

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BlockadeService blockadeService;

    @InjectMocks
    private ReservationService service;

    @Captor
    private ArgumentCaptor<Reservation> savedReservationCaptor;

    @Test
    void makingReservationBlocksCapacityAndSavesConfirmedReservation() {
        // given
        var customerId = CustomerId.newId();
        var blockadeId = BlockadeId.newId();
        when(blockadeService.block(BRANCH, CAR_CLASS, PERIOD)).thenReturn(blockadeId);

        // when
        var reservationId = service.makeReservation(customerId, BRANCH, CAR_CLASS, PERIOD, PICK_UP_TIME);

        // then
        verify(reservationRepository).save(savedReservationCaptor.capture());
        var saved = savedReservationCaptor.getValue();
        assertThat(saved.id()).isEqualTo(reservationId);
        assertThat(saved.customerId()).isEqualTo(customerId);
        assertThat(saved.branchId()).isEqualTo(BRANCH);
        assertThat(saved.carClass()).isEqualTo(CAR_CLASS);
        assertThat(saved.period()).isEqualTo(PERIOD);
        assertThat(saved.pickUpTime()).isEqualTo(PICK_UP_TIME);
        assertThat(saved.blockadeId()).isEqualTo(blockadeId);
    }

    @Test
    void noReservationIsSavedWhenCapacityCannotBeBlocked() {
        // given
        when(blockadeService.block(BRANCH, CAR_CLASS, PERIOD)).thenThrow(new SlotUnavailableException("no free capacity"));

        // then
        assertThatExceptionOfType(SlotUnavailableException.class)
                .isThrownBy(() -> service.makeReservation(CustomerId.newId(), BRANCH, CAR_CLASS, PERIOD, PICK_UP_TIME));
        verify(reservationRepository, never()).save(any());
    }

}
