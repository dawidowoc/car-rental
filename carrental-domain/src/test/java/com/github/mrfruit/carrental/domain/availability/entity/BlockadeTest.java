package com.github.mrfruit.carrental.domain.availability.entity;

import com.github.mrfruit.carrental.domain.availability.values.BlockadeId;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BlockadeTest {

    private static final BranchId BRANCH = new BranchId("wroclaw-1");
    private static final LocalDate START = LocalDate.of(2026, 9, 8);
    private static final DateRange PERIOD = new DateRange(START, START.plusDays(2));

    @Test
    void newBlockadeIsActiveBlockadeAndKeepsWhatItBlocks() {
        // when
        var blockade = Blockade.activeBlockade(BRANCH, CarClass.SUV, PERIOD);

        // then
        assertThat(blockade.id()).isNotNull();
        assertThat(blockade.branchId()).isEqualTo(BRANCH);
        assertThat(blockade.carClass()).isEqualTo(CarClass.SUV);
        assertThat(blockade.period()).isEqualTo(PERIOD);
    }

    @Test
    void eachNewBlockadeGetsItsOwnId() {
        // when
        var first = Blockade.activeBlockade(BRANCH, CarClass.SUV, PERIOD);
        var second = Blockade.activeBlockade(BRANCH, CarClass.SUV, PERIOD);

        // then
        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void blockadesWithSameIdAreEqualRegardlessOfWhatTheyBlock() {
        // given
        var id = BlockadeId.newId();
        var suv = new Blockade(id, BRANCH, CarClass.SUV, PERIOD);
        var van = new Blockade(id, new BranchId("krakow-1"), CarClass.VAN,
                new DateRange(START.plusDays(9), START.plusDays(10)));

        // then
        assertThat(suv).isEqualTo(van);
        assertThat(suv).hasSameHashCodeAs(van);
    }
}
