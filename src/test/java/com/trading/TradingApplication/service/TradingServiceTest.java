package com.trading.TradingApplication.service;

import com.trading.TradingApplication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TradingServiceTest {
    private TradingService service;

    @BeforeEach
    void setUp() {
        service = new TradingService();
    }

    @Test
    void shouldCreateUserAndTrackGems() {
        var user = service.createUser("alice", 10000.0);
        service.buyAsset(user.getUserId(), "AAPL", 10, 150.0);

        var stats = service.getUserStats(user.getUserId());
        assertEquals(1, stats.gemCount());
        assertEquals(8500.0, stats.balance(), 0.001);
    }

    @Test
    void shouldAwardBonusGemsAtMilestones() {
        var user = service.createUser("trader", 100_000.0);
        for (int i = 0; i < 10; i++) {
            service.buyAsset(user.getUserId(), "STK" + i, 1, 100);
        }
        assertEquals(28, user.getGemCount());   // 10 +5 +10 +3 = 28
    }

    @Test
    void shouldHandleTiedRanksCorrectly() {
        var u1 = service.createUser("a", 1000);
        var u2 = service.createUser("b", 1000);
        service.buyAsset(u1.getUserId(), "X", 1, 100);
        service.buyAsset(u2.getUserId(), "Y", 1, 100);

        var lb = service.getLeaderboard(2);
        assertEquals(1, lb.get(0).rank());
        assertEquals(1, lb.get(1).rank());
    }

    @Test
    void shouldBuyAssetWithDynamicPrice() {
        var user = service.createUser("alice", 10000.0);
        service.buyAsset(user.getUserId(), "AAPL", 10, 150.0);

        var asset = user.getPortfolio().getAssets().get(0);
        assertEquals(10, asset.getQuantity());
        assertEquals(150.0, asset.getAvgPrice(), 0.001);
        assertTrue(asset.getCurrentPrice() >= 142.5 && asset.getCurrentPrice() <= 157.5); // ±5%
    }

    @Test
    void shouldAwardStreakBonus() throws InterruptedException {
        var user = service.createUser("bob", 100_000.0);

        service.buyAsset(user.getUserId(), "X", 1, 100);
        assertEquals(1, user.getGemCount());  // +1
        assertEquals(1, user.getCurrentStreak());

        Thread.sleep(100);

        service.buyAsset(user.getUserId(), "Y", 1, 100);
        assertEquals(2, user.getGemCount());  // +1
        assertEquals(2, user.getCurrentStreak());

        Thread.sleep(100);

        service.buyAsset(user.getUserId(), "Z", 1, 100);
        assertEquals(6, user.getGemCount());  // +1 +3 = +4
        assertEquals(3, user.getCurrentStreak());
    }

    @Test
    void shouldResetStreakAfterTimeout() throws Exception {
        var user = service.createUser("charlie", 100_000.0);

        service.buyAsset(user.getUserId(), "A", 1, 100);
        service.buyAsset(user.getUserId(), "B", 1, 100);

        // Force last trade 6 min ago
        Field f = User.class.getDeclaredField("lastTradeTimestamp");
        f.setAccessible(true);
        f.setLong(user, System.currentTimeMillis() - 6 * 60 * 1000L);

        service.buyAsset(user.getUserId(), "C", 1, 100);

        assertEquals(3, user.getGemCount());   // only +1 each time
    }

    @Test
    void shouldCreateUserAndBuy() {
        var user = service.createUser("alice", 10_000.0);
        service.buyAsset(user.getUserId(), "AAPL", 10, 150.0);

        var stats = service.getUserStats(user.getUserId());
        assertEquals(8_500.0, stats.balance(), 0.001);
        assertEquals(1, stats.gemCount());
        assertTrue(stats.portfolioValue() >= 1_425 && stats.portfolioValue() <= 1_575); // ±5%
    }
}
