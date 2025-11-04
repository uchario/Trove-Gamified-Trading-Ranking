package com.trading.TradingApplication.dto;

import com.trading.TradingApplication.model.Asset;

import java.util.List;

public record UserResponse(
        Long userId,
        String username,
        double balance,
        int gemCount,
        int rank,
        int tradeCount,
        double portfolioValue,
        List<Asset> assets
) { }
