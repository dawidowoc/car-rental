package com.github.mrfruit.carrental.domain.availability.entity;

import com.github.mrfruit.carrental.domain.availability.exception.SlotUnavailableException;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;

import java.time.LocalDate;

import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

public class DailySlot {

    private final BranchId branchId;
    private final CarClass carClass;
    private final LocalDate date;
    private int capacity;
    private int allocated;

    public DailySlot(BranchId branchId, CarClass carClass, LocalDate date, int capacity) {
        this.branchId = requireNonNull(branchId, "branchId must not be null");
        this.carClass = requireNonNull(carClass, "carClass must not be null");
        this.date = requireNonNull(date, "date must not be null");
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must not be negative: " + capacity);
        }
        this.capacity = capacity;
        this.allocated = 0;
    }

    public void allocate() {
        ensureFreeCapacity();
        allocated++;
    }

    private void ensureFreeCapacity() {
        if (!hasFreeCapacity()) {
            throw new SlotUnavailableException("no free capacity for " + carClass + " at branch " + branchId.value()
                    + " on " + date + " (capacity " + capacity + ")");
        }
    }

    public void release() {
        ensureAllocated();
        allocated--;
    }

    private void ensureAllocated() {
        if (allocated == 0) {
            throw new IllegalStateException("nothing allocated for " + carClass + " at branch " + branchId.value()
                    + " on " + date + " to release");
        }
    }

    public void increaseCapacity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive: " + amount);
        }
        capacity += amount;
    }

    public void decreaseCapacity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive: " + amount);
        }
        if (amount > capacity) {
            throw new IllegalArgumentException("cannot decrease capacity by " + amount
                    + " for " + carClass + " at branch " + branchId.value()
                    + " on " + date + " (capacity " + capacity + ")");
        }
        capacity -= amount;
    }

    public boolean hasFreeCapacity() {
        return allocated < capacity;
    }

    public int freeCapacity() {
        return capacity - allocated;
    }

    public BranchId branchId() {
        return branchId;
    }

    public CarClass carClass() {
        return carClass;
    }

    public LocalDate date() {
        return date;
    }

    public int capacity() {
        return capacity;
    }

    public int allocated() {
        return allocated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DailySlot other)) {
            return false;
        }
        return branchId.equals(other.branchId) && carClass == other.carClass && date.equals(other.date);
    }

    @Override
    public int hashCode() {
        return hash(branchId, carClass, date);
    }

    @Override
    public String toString() {
        return "DailySlot{branchId=" + branchId.value()
                + ", carClass=" + carClass
                + ", date=" + date
                + ", allocated=" + allocated
                + ", capacity=" + capacity
                + '}';
    }
}
