package com.trading.TradingApplication.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @EqualsAndHashCode.Include
    private final Long userId;

    private String username;
    private double balance;
    private int gemCount;
    private int rank;
    private int tradeCount;
    private int currentStreak = 0;
    private long lastTradeTimestamp = 0;

    @Getter
    private final Portfolio portfolio = new Portfolio();

    public User(Long userId, String username, double balance) {
        this.userId = userId;
        this.username = username;
        this.balance = balance;
        this.gemCount = 0;
        this.tradeCount = 0;
        this.rank = 1;
    }
}
