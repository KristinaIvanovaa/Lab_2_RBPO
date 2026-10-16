package com.example.demo.dto;

import jakarta.validation.constraints.Positive;
import com.example.demo.model.TicketStatus;

public record TicketRequest(
        @Positive Long performanceId,
        @Positive Integer seatNumber,
        @Positive Long userId,
        TicketStatus status) {
}
