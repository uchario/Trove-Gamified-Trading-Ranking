package com.trading.TradingApplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TradeRequest(
        @NotBlank String assetName,
        @Positive int quantity,
        @Positive double price
) { }
