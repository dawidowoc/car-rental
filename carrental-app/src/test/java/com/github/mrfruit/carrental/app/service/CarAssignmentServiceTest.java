package com.github.mrfruit.carrental.app.service;

import com.github.mrfruit.carrental.domain.assignment.entity.CarAssignment;
import com.github.mrfruit.carrental.domain.assignment.exception.AssignmentNotFoundException;
import com.github.mrfruit.carrental.domain.assignment.exception.NoCarAvailableException;
import com.github.mrfruit.carrental.domain.assignment.exception.ReservationAlreadyAssignedException;
import com.github.mrfruit.carrental.domain.assignment.policy.CarSelectionPolicy;
import com.github.mrfruit.carrental.domain.assignment.repository.CarAssignmentRepository;
import com.github.mrfruit.carrental.domain.assignment.values.AssignmentId;
import com.github.mrfruit.carrental.domain.availability.values.BlockadeId;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.common.CustomerId;
import com.github.mrfruit.carrental.domain.common.ReservationId;
import com.github.mrfruit.carrental.domain.fleet.entity.Car;
import com.github.mrfruit.carrental.domain.fleet.repository.CarRepository;
import com.github.mrfruit.carrental.domain.fleet.values.CarId;
import com.github.mrfruit.carrental.domain.fleet.values.CarStatus;
import com.github.mrfruit.carrental.domain.fleet.values.Vin;
import com.github.mrfruit.carrental.domain.reservation.entity.Reservation;
import com.github.mrfruit.carrental.domain.reservation.exception.ReservationNotFoundException;
import com.github.mrfruit.carrental.domain.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarAssignmentServiceTest {

    private static final BranchId BRANCH = new BranchId("wroclaw-1");
    private static final CarClass CAR_CLASS = CarClass.SUV;
    private static final LocalDate START = LocalDate.of(2026, 9, 8);
    private static final DateRange PERIOD = new DateRange(START, START.plusDays(2));
    private static final LocalTime PICK_UP_TIME = LocalTime.of(10, 0);

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarAssignmentRepository carAssignmentRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private CarAssignmentService service;

    @BeforeEach
    void setUp() {
        service = new CarAssignmentService(carRepository, carAssignmentRepository, reservationRepository,
                new CarSelectionPolicy());
    }

    @Test
    void assignsAvailableCarAndMarksItRented() {
        // given
        var reservation = confirmedReservation();
        var car = car("WBA1111111111AAAA");
        givenReservation(reservation);
        when(carAssignmentRepository.findByReservationId(reservation.id())).thenReturn(Optional.empty());
        when(carRepository.findAvailableForUpdate(BRANCH, CAR_CLASS)).thenReturn(List.of(car));

        // when
        var assignment = service.assignCar(reservation.id());

        // then
        assertThat(car.status()).isEqualTo(CarStatus.RENTED);
        assertThat(assignment.carId()).isEqualTo(car.id());
        assertThat(assignment.reservationId()).isEqualTo(reservation.id());
        assertThat(assignment.period()).isEqualTo(PERIOD);
        verify(carRepository).save(car);
        verify(carAssignmentRepository).save(assignment);
    }

    @Test
    void looksForCarsOfTheClassAndBranchTakenFromTheReservation() {
        // given
        var reservation = confirmedReservation();
        givenReservation(reservation);
        when(carAssignmentRepository.findByReservationId(any())).thenReturn(Optional.empty());
        when(carRepository.findAvailableForUpdate(BRANCH, CAR_CLASS)).thenReturn(List.of(car("WBA1111111111AAAA")));

        // when
        service.assignCar(reservation.id());

        // then
        verify(carRepository).findAvailableForUpdate(BRANCH, CAR_CLASS);
    }

    @Test
    void assignsFirstOfTheAvailableCars() {
        // given
        var reservation = confirmedReservation();
        var first = car("WBA1111111111AAAA");
        var second = car("WBA2222222222BBBB");
        givenReservation(reservation);
        when(carAssignmentRepository.findByReservationId(any())).thenReturn(Optional.empty());
        when(carRepository.findAvailableForUpdate(BRANCH, CAR_CLASS)).thenReturn(List.of(first, second));

        // when
        var assignment = service.assignCar(reservation.id());

        // then
        assertThat(assignment.carId()).isEqualTo(first.id());
        assertThat(first.status()).isEqualTo(CarStatus.RENTED);
        assertThat(second.status()).isEqualTo(CarStatus.AVAILABLE);
    }

    @Test
    void failsWhenBranchHasNoAvailableCarOfRequestedClass() {
        // given
        var reservation = confirmedReservation();
        givenReservation(reservation);
        when(carAssignmentRepository.findByReservationId(any())).thenReturn(Optional.empty());
        when(carRepository.findAvailableForUpdate(BRANCH, CAR_CLASS)).thenReturn(List.of());

        // then
        assertThatExceptionOfType(NoCarAvailableException.class)
                .isThrownBy(() -> service.assignCar(reservation.id()))
                .withMessageContaining(CAR_CLASS.toString());
        verify(carRepository, never()).save(any());
        verify(carAssignmentRepository, never()).save(any());
    }

    @Test
    void reservationCannotBeAssignedTwice() {
        // given
        var reservation = confirmedReservation();
        givenReservation(reservation);
        when(carAssignmentRepository.findByReservationId(reservation.id()))
                .thenReturn(Optional.of(assignmentOf(CarId.newId())));

        // then
        assertThatExceptionOfType(ReservationAlreadyAssignedException.class)
                .isThrownBy(() -> service.assignCar(reservation.id()))
                .withMessageContaining(reservation.id().value().toString());
        verify(carRepository, never()).findAvailableForUpdate(any(), any());
        verify(carRepository, never()).save(any());
        verify(carAssignmentRepository, never()).save(any());
    }

    @Test
    void carCannotBePickedUpForUnknownReservation() {
        // given
        var reservationId = ReservationId.newId();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(ReservationNotFoundException.class)
                .isThrownBy(() -> service.assignCar(reservationId))
                .withMessageContaining(reservationId.value().toString());
        verify(carRepository, never()).findAvailableForUpdate(any(), any());
        verify(carAssignmentRepository, never()).save(any());
    }

    @Test
    void returningCarMakesItAvailableAgain() {
        // given
        var car = car("WBA1111111111AAAA");
        car.pickUp();
        var assignment = assignmentOf(car.id());
        when(carAssignmentRepository.findById(assignment.id())).thenReturn(Optional.of(assignment));
        when(carRepository.findById(car.id())).thenReturn(Optional.of(car));

        // when
        service.returnCar(assignment.id());

        // then
        assertThat(car.status()).isEqualTo(CarStatus.AVAILABLE);
        verify(carRepository).save(car);
    }

    @Test
    void returningTheSameAssignmentTwiceIsRejected() {
        // given
        var car = car("WBA1111111111AAAA");
        var assignment = assignmentOf(car.id());
        when(carAssignmentRepository.findById(assignment.id())).thenReturn(Optional.of(assignment));
        when(carRepository.findById(car.id())).thenReturn(Optional.of(car));

        // then
        assertThatIllegalStateException().isThrownBy(() -> service.returnCar(assignment.id()));
        verify(carRepository, never()).save(any());
    }

    @Test
    void returningUnknownAssignmentThrows() {
        // given
        var assignmentId = AssignmentId.newId();
        when(carAssignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        // then
        assertThatExceptionOfType(AssignmentNotFoundException.class)
                .isThrownBy(() -> service.returnCar(assignmentId))
                .withMessageContaining(assignmentId.value().toString());
        verify(carRepository, never()).save(any());
    }

    private void givenReservation(Reservation reservation) {
        when(reservationRepository.findById(reservation.id())).thenReturn(Optional.of(reservation));
    }

    private static Reservation confirmedReservation() {
        return Reservation.confirmedReservation(CustomerId.newId(), BRANCH, CAR_CLASS, PERIOD, PICK_UP_TIME, BlockadeId.newId());
    }

    private static Car car(String vin) {
        return Car.newCar(new Vin(vin), CAR_CLASS, BRANCH);
    }

    private static CarAssignment assignmentOf(CarId carId) {
        return CarAssignment.newAssignment(ReservationId.newId(), carId, PERIOD);
    }
}
