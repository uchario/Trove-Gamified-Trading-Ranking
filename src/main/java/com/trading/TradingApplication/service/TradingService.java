package com.trading.TradingApplication.service;

import com.trading.TradingApplication.dto.InsightResponse;
import com.trading.TradingApplication.dto.LeaderboardResponse;
import com.trading.TradingApplication.dto.UserResponse;
import com.trading.TradingApplication.model.Asset;
import com.trading.TradingApplication.model.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TradingService {
    private final Map<Long, User> users = new HashMap<>();
    private final AtomicLong userIdSeq = new AtomicLong(1);
    private final AtomicLong assetIdSeq = new AtomicLong(1);
    private static final long STREAK_WINDOW_MS = 5 * 60 * 1000; // 5 minutes
    private final Random random = new Random();

    public User createUser(String username, double initialBalance) {
        var userId = userIdSeq.getAndIncrement();
        var user = new User(userId, username, initialBalance);
        users.put(userId, user);
        updateRankings();
        return user;
    }

    public void buyAsset(Long userId, String assetName, int quantity, double price) {
        var user = getUser(userId);
        var cost = price * quantity;
        if (user.getBalance() < cost) {
            throw new IllegalStateException("Insufficient funds");
        }

        user.setBalance(user.getBalance() - cost);

        var portfolio = user.getPortfolio();
        var existing = portfolio.findByName(assetName);
        var currentMarketPrice = simulateMarketPrice(assetName);

        if (existing == null) {
            var asset = new Asset(
                    assetIdSeq.getAndIncrement(),
                    assetName,
                    quantity,
                    price,
                    currentMarketPrice
            );
            portfolio.addAsset(asset);
        } else {
            var totalCost = existing.getAvgPrice() * existing.getQuantity() + cost;
            var totalQty = existing.getQuantity() + quantity;
            existing.setAvgPrice(totalCost / totalQty);
            existing.setQuantity(totalQty);
            existing.setCurrentPrice(currentMarketPrice);
        }

        // ADD THIS LINE
        awardGems(user);

        updateRankings();
    }

    public void sellAsset(Long userId, String assetName, int quantity, double price) {
        var user = getUser(userId);
        var portfolio = user.getPortfolio();
        var asset = portfolio.findByName(assetName);
        if (asset == null || asset.getQuantity() < quantity) {
            throw new IllegalStateException("Insufficient asset quantity");
        }

        var currentMarketPrice = simulateMarketPrice(assetName);
        asset.setCurrentPrice(currentMarketPrice);

        var revenue = price * quantity;
        user.setBalance(user.getBalance() + revenue);

        var newQty = asset.getQuantity() - quantity;
        if (newQty == 0) {
            portfolio.removeAsset(asset);
        } else {
            asset.setQuantity(newQty);
        }

        awardGems(user);

        updateRankings();
    }

    private void awardGems(User user) {
        long now = System.currentTimeMillis();

        // Special case: first trade ever
        if (user.getLastTradeTimestamp() == 0) {
            user.setCurrentStreak(1);
        } else {
            long timeSinceLastTrade = now - user.getLastTradeTimestamp();
            if (timeSinceLastTrade > STREAK_WINDOW_MS) {
                user.setCurrentStreak(1);  // Start new streak
            } else {
                user.setCurrentStreak(user.getCurrentStreak() + 1);
            }
        }

        user.setLastTradeTimestamp(now);

        // Base gem
        user.setTradeCount(user.getTradeCount() + 1);
        user.setGemCount(user.getGemCount() + 1);

        // Milestones
        int trades = user.getTradeCount();
        if (trades == 5) user.setGemCount(user.getGemCount() + 5);
        else if (trades == 10) user.setGemCount(user.getGemCount() + 10);

        // Streak bonus
        if (user.getCurrentStreak() == 3) {
            user.setGemCount(user.getGemCount() + 3);
        }
    }

    private void updateRankings() {
        var sorted = new ArrayList<>(users.values());
        sorted.sort(Comparator.comparingInt(User::getGemCount).reversed());

        int rank = 1;
        int prevGems = -1;
        int tieOffset = 0;

        for (int i = 0; i < sorted.size(); i++) {
            var user = sorted.get(i);
            if (user.getGemCount() != prevGems) {
                rank = i + 1 - tieOffset;
                tieOffset = 0;
            } else {
                tieOffset++;
            }
            user.setRank(rank);
            prevGems = user.getGemCount();
        }
    }

    public UserResponse getUserStats(Long userId) {
        var user = getUser(userId);
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getBalance(),
                user.getGemCount(),
                user.getRank(),
                user.getTradeCount(),
                user.getPortfolio().calculateValue(), // Lombok: getPortfolio()
                user.getPortfolio().getAssets()
        );
    }

    public List<LeaderboardResponse> getLeaderboard(int topN) {
        if (topN <= 0) topN = 10;
        return users.values().stream()
                .sorted(Comparator.comparingInt(User::getGemCount).reversed())
                .limit(topN)
                .map(u -> new LeaderboardResponse(u.getUserId(), u.getUsername(), u.getGemCount(), u.getRank()))
                .toList();
    }

    private User getUser(Long userId) {
        var user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        return user;
    }

    private double simulateMarketPrice(String assetName) {
        double base = switch (assetName.toUpperCase()) {
            case "AAPL" -> 150.0;
            case "GOOG" -> 2800.0;
            case "TSLA" -> 700.0;
            default -> 100.0;
        };
        return base * (0.95 + random.nextDouble() * 0.1); // ±5%
    }

    public InsightResponse getPortfolioInsights() {
        if (users.isEmpty()) {
            return new InsightResponse("N/A", 0.0, "N/A");
        }

        // Most traded asset
        Map<String, Integer> tradeCount = new HashMap<>();
        users.values().forEach(u -> u.getPortfolio().getAssets().forEach(a ->
                tradeCount.merge(a.getName(), a.getQuantity(), Integer::sum)
        ));
        String mostTraded = tradeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        // Highest portfolio value
        User topUser = users.values().stream()
                .max(Comparator.comparingDouble(u -> u.getPortfolio().calculateValue()))
                .orElse(null);

        double highestValue = topUser != null ? topUser.getPortfolio().calculateValue() : 0.0;
        String topUsername = topUser != null ? topUser.getUsername() : "N/A";

        return new InsightResponse(mostTraded, highestValue, topUsername);
    }
}
