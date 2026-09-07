package com.github.mrfruit.carrental.app.service;

import com.github.mrfruit.carrental.domain.assignment.entity.CarAssignment;
import com.github.mrfruit.carrental.domain.assignment.exception.AssignmentNotFoundException;
import com.github.mrfruit.carrental.domain.assignment.exception.NoCarAvailableException;
import com.github.mrfruit.carrental.domain.assignment.exception.ReservationAlreadyAssignedException;
import com.github.mrfruit.carrental.domain.assignment.policy.CarSelectionPolicy;
import com.github.mrfruit.carrental.domain.assignment.repository.CarAssignmentRepository;
import com.github.mrfruit.carrental.domain.assignment.values.AssignmentId;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.common.ReservationId;
import com.github.mrfruit.carrental.domain.fleet.entity.Car;
import com.github.mrfruit.carrental.domain.fleet.repository.CarRepository;
import com.github.mrfruit.carrental.domain.fleet.values.CarId;
import com.github.mrfruit.carrental.domain.reservation.entity.Reservation;
import com.github.mrfruit.carrental.domain.reservation.exception.ReservationNotFoundException;
import com.github.mrfruit.carrental.domain.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CarAssignmentService {

    private final CarRepository carRepository;
    private final CarAssignmentRepository carAssignmentRepository;
    private final ReservationRepository reservationRepository;
    private final CarSelectionPolicy carSelectionPolicy;

    public CarAssignmentService(CarRepository carRepository, CarAssignmentRepository carAssignmentRepository,
                                ReservationRepository reservationRepository, CarSelectionPolicy carSelectionPolicy) {
        this.carRepository = carRepository;
        this.carAssignmentRepository = carAssignmentRepository;
        this.reservationRepository = reservationRepository;
        this.carSelectionPolicy = carSelectionPolicy;
    }

    public CarAssignment assignCar(ReservationId reservationId) {
        var reservation = requireReservation(reservationId);
        ensureNotAssignedYet(reservationId);

        var car = selectCar(reservation.branchId(), reservation.carClass());
        car.pickUp();
        carRepository.save(car);

        var assignment = CarAssignment.newAssignment(reservationId, car.id(), reservation.period());
        carAssignmentRepository.save(assignment);
        return assignment;
    }

    public void returnCar(AssignmentId assignmentId) {
        var assignment = carAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException("no assignment with id " + assignmentId.value()));

        var car = getCarById(assignment.carId());
        car.dropOff();
        carRepository.save(car);
    }

    private Reservation requireReservation(ReservationId reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("no reservation with id " + reservationId.value()));
    }

    private void ensureNotAssignedYet(ReservationId reservationId) {
        carAssignmentRepository.findByReservationId(reservationId).ifPresent(existing -> {
            throw new ReservationAlreadyAssignedException("reservation " + reservationId.value()
                    + " already has car " + existing.carId().value() + " assigned");
        });
    }

    private Car selectCar(BranchId branchId, CarClass carClass) {
        return carSelectionPolicy.select(carRepository.findAvailableForUpdate(branchId, carClass))
                .orElseThrow(() -> new NoCarAvailableException("no free " + carClass + " at branch " + branchId.value()));
    }

    private Car getCarById(CarId carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new IllegalStateException("assigned car " + carId.value() + " no longer exists"));
    }
}
