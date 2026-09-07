package com.github.mrfruit.carrental.app.api;

import com.github.mrfruit.carrental.app.api.response.ErrorResponse;
import com.github.mrfruit.carrental.domain.assignment.exception.NoCarAvailableException;
import com.github.mrfruit.carrental.domain.assignment.exception.ReservationAlreadyAssignedException;
import com.github.mrfruit.carrental.domain.availability.exception.SlotUnavailableException;
import com.github.mrfruit.carrental.domain.reservation.exception.ReservationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(ReservationNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(ReservationNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler({
            SlotUnavailableException.class,
            NoCarAvailableException.class,
            ReservationAlreadyAssignedException.class
    })
    ResponseEntity<ErrorResponse> handleConflict(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleInvalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse(exception.getMessage()));
    }
}
