package com.trading.TradingApplication.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Asset {
    private final Long assetId;
    private final String name;
    private int quantity;
    private double avgPrice;
    private double currentPrice;

    public double getValue() {
        return quantity * avgPrice;
    }
}
