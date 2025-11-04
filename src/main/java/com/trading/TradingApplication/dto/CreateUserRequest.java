package com.trading.TradingApplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateUserRequest(
        @NotBlank(message = "Username is required") String username,
        @PositiveOrZero(message = "Balance must be non-negative") double initialBalance
) { }
