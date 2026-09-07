package com.github.mrfruit.carrental.domain.availability.service;

import com.github.mrfruit.carrental.domain.availability.entity.Blockade;
import com.github.mrfruit.carrental.domain.availability.entity.DailySlot;
import com.github.mrfruit.carrental.domain.availability.exception.SlotUnavailableException;
import com.github.mrfruit.carrental.domain.availability.repository.BlockadeRepository;
import com.github.mrfruit.carrental.domain.availability.repository.DailySlotRepository;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockadeServiceTest {

    private static final BranchId BRANCH = new BranchId("wroclaw-1");
    private static final CarClass CAR_CLASS = CarClass.SUV;
    private static final LocalDate START = LocalDate.of(2026, 9, 8);

    @Mock
    private DailySlotRepository dailySlotRepository;

    @Mock
    private BlockadeRepository blockadeRepository;

    @InjectMocks
    private BlockadeService service;

    @Captor
    private ArgumentCaptor<List<DailySlot>> savedSlotsCaptor;

    @Captor
    private ArgumentCaptor<Blockade> savedBlockadeCaptor;

    @Test
    void blocksOneUnitOnEveryDayInRange() {
        // given
        var day1 = slot(START, 2);
        var day2 = slot(START.plusDays(1), 2);
        var day3 = slot(START.plusDays(2), 2);
        var period = new DateRange(START, START.plusDays(2));
        when(dailySlotRepository.findForRangeForUpdate(BRANCH, CAR_CLASS, period)).thenReturn(List.of(day1, day2, day3));

        // when
        service.block(BRANCH, CAR_CLASS, period);

        // then
        assertThat(day1.allocated()).isEqualTo(1);
        assertThat(day2.allocated()).isEqualTo(1);
        assertThat(day3.allocated()).isEqualTo(1);
        verify(dailySlotRepository).saveAll(savedSlotsCaptor.capture());
        assertThat(savedSlotsCaptor.getValue()).containsExactly(day1, day2, day3);
    }

    @Test
    void blockReturnsIdOfSavedActiveBlockadeBlockade() {
        // given
        var period = new DateRange(START, START.plusDays(1));
        when(dailySlotRepository.findForRangeForUpdate(BRANCH, CAR_CLASS, period))
                .thenReturn(List.of(slot(START, 1), slot(START.plusDays(1), 1)));

        // when
        var blockadeId = service.block(BRANCH, CAR_CLASS, period);

        // then
        verify(blockadeRepository).save(savedBlockadeCaptor.capture());
        var saved = savedBlockadeCaptor.getValue();
        assertThat(saved.id()).isEqualTo(blockadeId);
        assertThat(saved.branchId()).isEqualTo(BRANCH);
        assertThat(saved.carClass()).isEqualTo(CAR_CLASS);
        assertThat(saved.period()).isEqualTo(period);
    }

    @Test
    void blocksASingleDayRange() {
        // given
        var only = slot(START, 1);
        var period = new DateRange(START, START);
        when(dailySlotRepository.findForRangeForUpdate(BRANCH, CAR_CLASS, period)).thenReturn(List.of(only));

        // when
        service.block(BRANCH, CAR_CLASS, period);

        // then
        assertThat(only.allocated()).isEqualTo(1);
    }

    @Test
    void failsAndBlocksNothingWhenADayHasNoSlot() {
        // given
        var day1 = slot(START, 2);
        var day3 = slot(START.plusDays(2), 2);
        var period = new DateRange(START, START.plusDays(2));
        when(dailySlotRepository.findForRangeForUpdate(BRANCH, CAR_CLASS, period)).thenReturn(List.of(day1, day3));

        // then
        assertThatExceptionOfType(SlotUnavailableException.class)
                .isThrownBy(() -> service.block(BRANCH, CAR_CLASS, period))
                .withMessageContaining(START.plusDays(1).toString());
        assertThat(day1.allocated()).isZero();
        assertThat(day3.allocated()).isZero();
        verify(dailySlotRepository, never()).saveAll(any());
        verify(blockadeRepository, never()).save(any());
    }

    @Test
    void failsWithoutSavingAnythingWhenADayIsFullyAllocated() {
        // given
        var day1 = slot(START, 2);
        var day2 = slot(START.plusDays(1), 1);
        day2.allocate();
        var day3 = slot(START.plusDays(2), 2);
        var period = new DateRange(START, START.plusDays(2));
        when(dailySlotRepository.findForRangeForUpdate(BRANCH, CAR_CLASS, period)).thenReturn(List.of(day1, day2, day3));

        // then
        assertThatExceptionOfType(SlotUnavailableException.class)
                .isThrownBy(() -> service.block(BRANCH, CAR_CLASS, period));
        verify(dailySlotRepository, never()).saveAll(any());
        verify(blockadeRepository, never()).save(any());
    }

    private static DailySlot slot(LocalDate date, int capacity) {
        return new DailySlot(BRANCH, CAR_CLASS, date, capacity);
    }
}
