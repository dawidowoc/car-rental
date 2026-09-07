package com.github.mrfruit.carrental.app.api;

import com.github.mrfruit.carrental.app.service.CarAssignmentService;
import com.github.mrfruit.carrental.app.service.ReservationService;
import com.github.mrfruit.carrental.domain.assignment.entity.CarAssignment;
import com.github.mrfruit.carrental.domain.assignment.exception.NoCarAvailableException;
import com.github.mrfruit.carrental.domain.assignment.exception.ReservationAlreadyAssignedException;
import com.github.mrfruit.carrental.domain.availability.exception.SlotUnavailableException;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CarClass;
import com.github.mrfruit.carrental.domain.common.CustomerId;
import com.github.mrfruit.carrental.domain.common.ReservationId;
import com.github.mrfruit.carrental.domain.fleet.values.CarId;
import com.github.mrfruit.carrental.domain.reservation.exception.ReservationNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private CarAssignmentService carAssignmentService;

    @Test
    void returnsCreatedWithIdOfTheNewReservation() throws Exception {
        // given
        var reservationId = ReservationId.newId();
        when(reservationService.makeReservation(
                new CustomerId(CUSTOMER_ID),
                new BranchId("wroclaw-1"),
                CarClass.SUV,
                new DateRange(LocalDate.of(2026, 9, 8), LocalDate.of(2026, 9, 10)),
                LocalTime.of(10, 0)))
                .thenReturn(reservationId);

        // then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody("2026-09-08", "2026-09-10")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/" + reservationId.value()))
                .andExpect(jsonPath("$.reservationId").value(reservationId.value().toString()));
    }

    @Test
    void returnsConflictWhenThereIsNoFreeCapacity() throws Exception {
        // given
        when(reservationService.makeReservation(any(), any(), any(), any(), any()))
                .thenThrow(new SlotUnavailableException("no free capacity for SUV at branch wroclaw-1 on 2026-09-08"));

        // then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody("2026-09-08", "2026-09-10")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("no free capacity for SUV at branch wroclaw-1 on 2026-09-08"));
    }

    @Test
    void returnsBadRequestWhenPeriodEndsBeforeItStarts() throws Exception {
        // then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody("2026-09-10", "2026-09-08")))
                .andExpect(status().isBadRequest());
        verify(reservationService, never()).makeReservation(any(), any(), any(), any(), any());
    }

    @Test
    void returnsBadRequestWhenBranchIsMissing() throws Exception {
        // given
        var body = """
                {"customerId":"%s","carClass":"SUV","startDate":"2026-09-08","endDate":"2026-09-10","pickUpTime":"10:00"}
                """.formatted(CUSTOMER_ID);

        // then
        mockMvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
        verify(reservationService, never()).makeReservation(any(), any(), any(), any(), any());
    }


    @Test
    void picksUpCarAndReturnsCreatedWithTheAssignedCar() throws Exception {
        // given
        var reservationId = ReservationId.newId();
        var carId = CarId.newId();
        var assignment = CarAssignment.newAssignment(reservationId, carId,
                new DateRange(LocalDate.of(2026, 9, 8), LocalDate.of(2026, 9, 10)));
        when(carAssignmentService.assignCar(reservationId)).thenReturn(assignment);

        // then
        mockMvc.perform(post("/reservations/{reservationId}/pick-up", reservationId.value()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/assignments/" + assignment.id().value()))
                .andExpect(jsonPath("$.assignmentId").value(assignment.id().value().toString()))
                .andExpect(jsonPath("$.carId").value(carId.value().toString()));
    }

    @Test
    void returnsNotFoundWhenPickingUpUnknownReservation() throws Exception {
        // given
        when(carAssignmentService.assignCar(any()))
                .thenThrow(new ReservationNotFoundException("no reservation with that id"));

        // then
        mockMvc.perform(post("/reservations/{reservationId}/pick-up", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsConflictWhenNoCarIsAvailableForPickUp() throws Exception {
        // given
        when(carAssignmentService.assignCar(any()))
                .thenThrow(new NoCarAvailableException("no free SUV at branch wroclaw-1"));

        // then
        mockMvc.perform(post("/reservations/{reservationId}/pick-up", UUID.randomUUID()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("no free SUV at branch wroclaw-1"));
    }

    @Test
    void returnsConflictWhenReservationWasAlreadyPickedUp() throws Exception {
        // given
        when(carAssignmentService.assignCar(any()))
                .thenThrow(new ReservationAlreadyAssignedException("already has car assigned"));

        // then
        mockMvc.perform(post("/reservations/{reservationId}/pick-up", UUID.randomUUID()))
                .andExpect(status().isConflict());
    }

    @Test
    void returnsBadRequestForMalformedReservationId() throws Exception {
        // then
        mockMvc.perform(post("/reservations/{reservationId}/pick-up", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    private static String requestBody(String startDate, String endDate) {
        return """
                {"customerId":"%s","branchId":"wroclaw-1","carClass":"SUV","startDate":"%s","endDate":"%s","pickUpTime":"10:00"}
                """.formatted(CUSTOMER_ID, startDate, endDate);
    }
}
