package com.github.mrfruit.carrental.app.adapter.jpa;

import com.github.mrfruit.carrental.domain.assignment.entity.CarAssignment;
import com.github.mrfruit.carrental.domain.assignment.values.AssignmentId;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.ReservationId;
import com.github.mrfruit.carrental.domain.fleet.values.CarId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "car_assignment")
class CarAssignmentEntity {

    @Id
    private UUID id;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Column(name = "car_id", nullable = false)
    private UUID carId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    protected CarAssignmentEntity() {
    }

    static CarAssignmentEntity from(CarAssignment assignment) {
        var entity = new CarAssignmentEntity();
        entity.id = assignment.id().value();
        entity.reservationId = assignment.reservationId().value();
        entity.carId = assignment.carId().value();
        entity.periodStart = assignment.period().start();
        entity.periodEnd = assignment.period().end();
        return entity;
    }

    CarAssignment toDomain() {
        return new CarAssignment(
                new AssignmentId(id),
                new ReservationId(reservationId),
                new CarId(carId),
                new DateRange(periodStart, periodEnd));
    }
}
