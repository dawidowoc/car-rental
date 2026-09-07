package com.github.mrfruit.carrental.domain.availability.entity;

import com.github.mrfruit.carrental.domain.availability.exception.SlotUnavailableException;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class DailySlotTest {

    private static final BranchId BRANCH = new BranchId("wroclaw-1");
    private static final LocalDate DATE = LocalDate.of(2026, 9, 8);

    @Test
    void newSlotStartsWithNothingAllocated() {
        // when
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 3);

        // then
        assertThat(slot.allocated()).isZero();
        assertThat(slot.capacity()).isEqualTo(3);
        assertThat(slot.freeCapacity()).isEqualTo(3);
        assertThat(slot.hasFreeCapacity()).isTrue();
    }

    @Test
    void constructorRejectsNullBranchId() {
        // then
        assertThatNullPointerException().isThrownBy(() -> new DailySlot(null, CarClass.SEDAN, DATE, 1));
    }

    @Test
    void constructorRejectsNullDate() {
        // then
        assertThatNullPointerException().isThrownBy(() -> new DailySlot(BRANCH, CarClass.SEDAN, null, 1));
    }

    @Test
    void constructorRejectsNullCarClass() {
        // then
        assertThatNullPointerException().isThrownBy(() -> new DailySlot(BRANCH, null, DATE, 1));
    }

    @Test
    void constructorRejectsNegativeCapacity() {
        // then
        assertThatIllegalArgumentException().isThrownBy(() -> new DailySlot(BRANCH, CarClass.SEDAN, DATE, -1));
    }

    @Test
    void allocateConsumesFreeCapacity() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SUV, DATE, 2);

        // when
        slot.allocate();

        // then
        assertThat(slot.allocated()).isEqualTo(1);
        assertThat(slot.freeCapacity()).isEqualTo(1);
        assertThat(slot.hasFreeCapacity()).isTrue();
    }

    @Test
    void allocateUpToCapacityLeavesNoFreeCapacity() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SUV, DATE, 2);

        // when
        slot.allocate();
        slot.allocate();

        // then
        assertThat(slot.allocated()).isEqualTo(2);
        assertThat(slot.freeCapacity()).isZero();
        assertThat(slot.hasFreeCapacity()).isFalse();
    }

    @Test
    void allocateBeyondCapacityThrows() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.VAN, DATE, 1);
        slot.allocate();

        // then
        assertThatExceptionOfType(SlotUnavailableException.class).isThrownBy(slot::allocate);
    }

    @Test
    void allocateOnZeroCapacitySlotThrows() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.VAN, DATE, 0);

        // then
        assertThatExceptionOfType(SlotUnavailableException.class).isThrownBy(slot::allocate);
    }

    @Test
    void releaseGivesCapacityBack() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 2);
        slot.allocate();
        slot.allocate();

        // when
        slot.release();

        // then
        assertThat(slot.allocated()).isEqualTo(1);
        assertThat(slot.freeCapacity()).isEqualTo(1);
        assertThat(slot.hasFreeCapacity()).isTrue();
    }

    @Test
    void releaseWithNothingAllocatedThrows() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 2);

        // then
        assertThatIllegalStateException().isThrownBy(slot::release);
    }

    @Test
    void releaseAfterFullAllocationAllowsAllocatingAgain() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 1);
        slot.allocate();
        slot.release();

        // when
        slot.allocate();

        // then
        assertThat(slot.allocated()).isEqualTo(1);
    }

    @Test
    void increaseCapacityAddsFreeCapacity() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 2);
        slot.allocate();

        // when
        slot.increaseCapacity(3);

        // then
        assertThat(slot.capacity()).isEqualTo(5);
        assertThat(slot.freeCapacity()).isEqualTo(4);
        assertThat(slot.hasFreeCapacity()).isTrue();
    }

    @Test
    void increaseCapacityRejectsNonPositiveAmount() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 2);

        // then
        assertThatIllegalArgumentException().isThrownBy(() -> slot.increaseCapacity(0));
        assertThatIllegalArgumentException().isThrownBy(() -> slot.increaseCapacity(-1));
    }

    @Test
    void increaseCapacityAfterFullAllocationAllowsAllocatingAgain() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 1);
        slot.allocate();

        // when
        slot.increaseCapacity(1);
        slot.allocate();

        // then
        assertThat(slot.allocated()).isEqualTo(2);
        assertThat(slot.freeCapacity()).isZero();
    }

    @Test
    void decreaseCapacityReducesFreeCapacity() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 5);
        slot.allocate();

        // when
        slot.decreaseCapacity(2);

        // then
        assertThat(slot.capacity()).isEqualTo(3);
        assertThat(slot.freeCapacity()).isEqualTo(2);
    }

    @Test
    void decreaseCapacityRejectsNonPositiveAmount() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 2);

        // then
        assertThatIllegalArgumentException().isThrownBy(() -> slot.decreaseCapacity(0));
        assertThatIllegalArgumentException().isThrownBy(() -> slot.decreaseCapacity(-1));
    }

    @Test
    void decreaseCapacityBelowZeroThrows() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 2);

        // then
        assertThatIllegalArgumentException().isThrownBy(() -> slot.decreaseCapacity(3));
    }

    @Test
    void decreaseCapacityBelowAllocatedIsAllowedAndOverbooks() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 3);
        slot.allocate();
        slot.allocate();
        slot.allocate();

        // when
        slot.decreaseCapacity(2);

        // then
        assertThat(slot.capacity()).isEqualTo(1);
        assertThat(slot.allocated()).isEqualTo(3);
        assertThat(slot.freeCapacity()).isEqualTo(-2);
        assertThat(slot.hasFreeCapacity()).isFalse();
    }

    @Test
    void allocateStaysBlockedWhileOverbooked() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 2);
        slot.allocate();
        slot.allocate();
        slot.decreaseCapacity(1);

        // then
        assertThatExceptionOfType(SlotUnavailableException.class).isThrownBy(slot::allocate);
    }

    @Test
    void releaseThenAllocateWorksAfterOverbooking() {
        // given
        var slot = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 3);
        slot.allocate();
        slot.allocate();
        slot.allocate();
        slot.decreaseCapacity(1);

        // when
        slot.release();
        slot.release();

        // then
        assertThat(slot.allocated()).isEqualTo(1);
        assertThat(slot.freeCapacity()).isEqualTo(1);
        assertThat(slot.hasFreeCapacity()).isTrue();
    }

    @Test
    void slotsWithSameBranchCarClassAndDateAreEqualRegardlessOfAllocation() {
        // given
        var first = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 3);
        var second = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 5);
        second.allocate();

        // then
        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }

    @Test
    void slotsDifferingByCarClassAreNotEqual() {
        // given
        var sedan = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 3);
        var suv = new DailySlot(BRANCH, CarClass.SUV, DATE, 3);

        // then
        assertThat(sedan).isNotEqualTo(suv);
    }

    @Test
    void slotsDifferingByDateAreNotEqual() {
        // given
        var today = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 3);
        var tomorrow = new DailySlot(BRANCH, CarClass.SEDAN, DATE.plusDays(1), 3);

        // then
        assertThat(today).isNotEqualTo(tomorrow);
    }

    @Test
    void slotsDifferingByBranchAreNotEqual() {
        // given
        var wroclaw = new DailySlot(BRANCH, CarClass.SEDAN, DATE, 3);
        var poznan = new DailySlot(new BranchId("poznan-1"), CarClass.SEDAN, DATE, 3);

        // then
        assertThat(wroclaw).isNotEqualTo(poznan);
        assertThat(wroclaw).doesNotHaveSameHashCodeAs(poznan);
    }
}
