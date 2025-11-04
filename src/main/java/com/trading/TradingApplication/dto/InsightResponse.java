package com.trading.TradingApplication.dto;

public record InsightResponse(
        String mostTradedAsset,
        double highestPortfolioValue,
        String topPortfolioUser
) {
}
