package com.github.mrfruit.carrental.domain.availability.entity;

import com.github.mrfruit.carrental.domain.availability.values.BlockadeId;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;

public class Blockade {

    private final BlockadeId id;
    private final BranchId branchId;
    private final CarClass carClass;
    private final DateRange period;

    public Blockade(BlockadeId id, BranchId branchId, CarClass carClass, DateRange period) {
        this.id = id;
        this.branchId = branchId;
        this.carClass = carClass;
        this.period = period;
    }

    public static Blockade activeBlockade(BranchId branchId, CarClass carClass, DateRange period) {
        return new Blockade(BlockadeId.newId(), branchId, carClass, period);
    }

    public BlockadeId id() {
        return id;
    }

    public BranchId branchId() {
        return branchId;
    }

    public CarClass carClass() {
        return carClass;
    }

    public DateRange period() {
        return period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Blockade other)) {
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
        return "Blockade{id=" + id.value()
                + ", branchId=" + branchId.value()
                + ", carClass=" + carClass
                + ", period=" + period
                + '}';
    }
}
