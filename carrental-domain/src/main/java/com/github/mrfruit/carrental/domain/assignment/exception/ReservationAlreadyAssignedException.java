package com.github.mrfruit.carrental.domain.assignment.exception;

public class ReservationAlreadyAssignedException extends RuntimeException {

    public ReservationAlreadyAssignedException(String message) {
        super(message);
    }
}
