package com.staysure.operations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record VisitorRequest(
        @NotBlank @Size(max = 140) String visitorName,
        @NotBlank @Size(max = 30) String visitorPhone,
        @NotBlank @Size(max = 80) String relationship,
        @NotNull LocalDate visitDate,
        @NotNull LocalTime expectedArrivalTime,
        @NotNull LocalTime expectedDepartureTime,
        @NotBlank @Size(max = 300) String purpose
) {
}
