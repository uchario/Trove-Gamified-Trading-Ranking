package com.trading.TradingApplication.controller;

import com.trading.TradingApplication.dto.*;
import com.trading.TradingApplication.service.TradingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TradingController {
    private final TradingService service;

    public TradingController(TradingService service) {
        this.service = service;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest req) {
        var user = service.createUser(req.username(), req.initialBalance());
        return service.getUserStats(user.getUserId());
    }

    @PostMapping("/users/{userId}/buy")
    public UserResponse buy(@PathVariable Long userId, @Valid @RequestBody TradeRequest req) {
        service.buyAsset(userId, req.assetName(), req.quantity(), req.price());
        return service.getUserStats(userId);
    }

    @PostMapping("/users/{userId}/sell")
    public UserResponse sell(@PathVariable Long userId, @Valid @RequestBody TradeRequest req) {
        service.sellAsset(userId, req.assetName(), req.quantity(), req.price());
        return service.getUserStats(userId);
    }

    @GetMapping("/users/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        return service.getUserStats(userId);
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardResponse> leaderboard(@RequestParam(defaultValue = "10") int top) {
        return service.getLeaderboard(top);
    }

    @GetMapping("/insights")
    public InsightResponse getInsights() {
        return service.getPortfolioInsights();
    }
}
