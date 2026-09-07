package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.availability.entity.DailySlot;
import com.github.mrfruit.carrental.domain.common.BranchId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "daily_slot")
class DailySlotEntity {

    @EmbeddedId
    private DailySlotId id;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "allocated", nullable = false)
    private int allocated;

    protected DailySlotEntity() {
    }

    static DailySlotEntity from(DailySlot slot) {
        var entity = new DailySlotEntity();
        entity.id = new DailySlotId(slot.branchId().value(), slot.carClass(), slot.date());
        entity.capacity = slot.capacity();
        entity.allocated = slot.allocated();
        return entity;
    }

    DailySlot toDomain() {
        var slot = new DailySlot(new BranchId(id.branchId()), id.carClass(), id.date(), capacity);
        for (var i = 0; i < allocated; i++) {
            slot.allocate();
        }
        return slot;
    }
}
