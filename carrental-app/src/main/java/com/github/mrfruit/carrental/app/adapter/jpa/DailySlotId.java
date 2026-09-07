package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.common.CarClass;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
class DailySlotId implements Serializable {

    @Column(name = "branch_id", nullable = false)
    private String branchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_class", nullable = false)
    private CarClass carClass;

    @Column(name = "slot_date", nullable = false)
    private LocalDate date;

    protected DailySlotId() {
    }

    DailySlotId(String branchId, CarClass carClass, LocalDate date) {
        this.branchId = branchId;
        this.carClass = carClass;
        this.date = date;
    }

    String branchId() {
        return branchId;
    }

    CarClass carClass() {
        return carClass;
    }

    LocalDate date() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DailySlotId other)) {
            return false;
        }
        return Objects.equals(branchId, other.branchId) && carClass == other.carClass && Objects.equals(date, other.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchId, carClass, date);
    }
}
