package com.github.mrfruit.carrental.app.api;

import com.github.mrfruit.carrental.app.api.request.MakeReservationRequest;
import com.github.mrfruit.carrental.app.api.response.MakeReservationResponse;
import com.github.mrfruit.carrental.app.api.response.PickUpResponse;
import com.github.mrfruit.carrental.app.service.CarAssignmentService;
import com.github.mrfruit.carrental.app.service.ReservationService;
import com.github.mrfruit.carrental.domain.availability.values.DateRange;
import com.github.mrfruit.carrental.domain.common.BranchId;
import com.github.mrfruit.carrental.domain.common.CustomerId;
import com.github.mrfruit.carrental.domain.common.ReservationId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/reservations")
class ReservationController {

    private final ReservationService reservationService;
    private final CarAssignmentService carAssignmentService;

    ReservationController(ReservationService reservationService, CarAssignmentService carAssignmentService) {
        this.reservationService = reservationService;
        this.carAssignmentService = carAssignmentService;
    }

    @PostMapping
    ResponseEntity<MakeReservationResponse> makeReservation(@Valid @RequestBody MakeReservationRequest request) {
        var reservationId = reservationService.makeReservation(
                new CustomerId(request.customerId()),
                new BranchId(request.branchId()),
                request.carClass(),
                new DateRange(request.startDate(), request.endDate()),
                request.pickUpTime());

        return ResponseEntity.created(URI.create("/reservations/" + reservationId.value()))
                .body(new MakeReservationResponse(reservationId.value()));
    }

    @PostMapping("/{reservationId}/pick-up")
    ResponseEntity<PickUpResponse> pickUp(@PathVariable UUID reservationId) {
        var assignment = carAssignmentService.assignCar(new ReservationId(reservationId));

        return ResponseEntity.created(URI.create("/assignments/" + assignment.id().value()))
                .body(new PickUpResponse(assignment.id().value(), assignment.carId().value()));
    }
}
