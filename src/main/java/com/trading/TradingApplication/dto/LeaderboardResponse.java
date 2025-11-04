package com.trading.TradingApplication.dto;

public record LeaderboardResponse(
        Long userId,
        String username,
        int gemCount,
        int rank
) { }
