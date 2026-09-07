package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.availability.entity.Blockade;
import com.github.mrfruit.carrental.domain.availability.values.BlockadeId;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "blockade")
class BlockadeEntity {

    @Id
    private UUID id;

    @Column(name = "branch_id", nullable = false)
    private String branchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_class", nullable = false)
    private CarClass carClass;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    protected BlockadeEntity() {
    }

    static BlockadeEntity from(Blockade blockade) {
        var entity = new BlockadeEntity();
        entity.id = blockade.id().value();
        entity.branchId = blockade.branchId().value();
        entity.carClass = blockade.carClass();
        entity.periodStart = blockade.period().start();
        entity.periodEnd = blockade.period().end();
        return entity;
    }

    Blockade toDomain() {
        return new Blockade(
                new BlockadeId(id),
                new BranchId(branchId),
                carClass,
                new DateRange(periodStart, periodEnd));
    }
}
