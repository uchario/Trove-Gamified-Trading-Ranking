package com.trading.TradingApplication.integration;

import com.trading.TradingApplication.TradingApplication;
import com.trading.TradingApplication.dto.CreateUserRequest;
import com.trading.TradingApplication.dto.InsightResponse;
import com.trading.TradingApplication.dto.TradeRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TradingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TradingSystemIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCompleteFullTradingFlow() {
        // Create user
        var createReq = new CreateUserRequest("alice", 10000.0);
        var userResponse = restTemplate.postForEntity(
                "/api/users", createReq, Object.class);
        assertThat(userResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Extract userId from response (simplified)
        Long userId = 1L;

        // Buy asset
        var tradeReq = new TradeRequest("AAPL", 10, 150.0);
        var buyResponse = restTemplate.postForEntity(
                "/api/users/" + userId + "/buy", tradeReq, Object.class);
        assertThat(buyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Check leaderboard
        var lb = restTemplate.getForEntity("/api/leaderboard?top=1", Object[].class);
        assertThat(lb.getBody()).isNotEmpty();
    }

    @Test
    void shouldGetPortfolioInsights() {
        // Create users and trade
        var user1 = restTemplate.postForEntity("/api/users",
                new CreateUserRequest("alice", 10000), Object.class).getBody();
        // Extract ID, buy assets...

        var response = restTemplate.getForEntity("/api/insights", InsightResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().mostTradedAsset()).isNotNull();
    }
}
