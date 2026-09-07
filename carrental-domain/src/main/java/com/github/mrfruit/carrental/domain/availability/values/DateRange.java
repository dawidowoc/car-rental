package com.github.mrfruit.carrental.domain.availability.values;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public record DateRange(LocalDate start, LocalDate end) {

    public DateRange {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end " + end + " must not be before start " + start);
        }
    }

    public List<LocalDate> dates() {
        return start.datesUntil(end.plusDays(1)).toList();
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    public long lengthInDays() {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }
}
