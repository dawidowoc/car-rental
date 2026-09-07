package com.github.mrfruit.carrental.app.api.request;

import com.github.mrfruit.carrental.domain.common.CarClass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record MakeReservationRequest(@NotNull UUID customerId,
                                     @NotBlank String branchId,
                                     @NotNull CarClass carClass,
                                     @NotNull LocalDate startDate,
                                     @NotNull LocalDate endDate,
                                     @NotNull LocalTime pickUpTime) {
}
