package com.github.mrfruit.carrental.domain.availability.values;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class DateRangeTest {

    private static final LocalDate START = LocalDate.of(2026, 9, 8);

    @Test
    void datesCoverBothEndsInclusive() {
        // given
        var range = new DateRange(START, START.plusDays(2));

        // when
        var dates = range.dates();

        // then
        assertThat(dates).containsExactly(START, START.plusDays(1), START.plusDays(2));
    }

    @Test
    void singleDayRangeHasOneDate() {
        // given
        var range = new DateRange(START, START);

        // when
        var dates = range.dates();

        // then
        assertThat(dates).containsExactly(START);
        assertThat(range.lengthInDays()).isEqualTo(1);
    }

    @Test
    void lengthCountsBothEnds() {
        // given
        var range = new DateRange(START, START.plusDays(4));

        // then
        assertThat(range.lengthInDays()).isEqualTo(5);
    }

    @Test
    void containsIsInclusiveOnBothEnds() {
        // given
        var range = new DateRange(START, START.plusDays(2));

        // then
        assertThat(range.contains(START)).isTrue();
        assertThat(range.contains(START.plusDays(2))).isTrue();
        assertThat(range.contains(START.minusDays(1))).isFalse();
        assertThat(range.contains(START.plusDays(3))).isFalse();
    }

    @Test
    void rejectsEndBeforeStart() {
        // then
        assertThatIllegalArgumentException().isThrownBy(() -> new DateRange(START, START.minusDays(1)));
    }

    @Test
    void rejectsNullBounds() {
        // then
        assertThatNullPointerException().isThrownBy(() -> new DateRange(null, START));
        assertThatNullPointerException().isThrownBy(() -> new DateRange(START, null));
    }
}
