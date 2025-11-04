package com.trading.TradingApplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.TradingApplication.dto.CreateUserRequest;
import com.trading.TradingApplication.dto.InsightResponse;
import com.trading.TradingApplication.model.User;
import com.trading.TradingApplication.service.TradingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(TradingController.class)
public class TradingControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private TradingService service;

    @Test
    void shouldCreateUser() throws Exception {
        when(service.createUser(anyString(), anyDouble()))
                .thenAnswer(inv -> {
                    String username = inv.getArgument(0);
                    double balance = inv.getArgument(1);
                    return new User(1L, username, balance);
                });

        var req = new CreateUserRequest("alice", 10_000.0);
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectInvalidTrade() throws Exception {
        mockMvc.perform(post("/api/users/1/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assetName\":\"\",\"quantity\":-5,\"price\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnPortfolioInsights() throws Exception {
        when(service.getPortfolioInsights()).thenReturn(
                new InsightResponse("AAPL", 8500.0, "alice")
        );

        mockMvc.perform(get("/api/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mostTradedAsset").value("AAPL"))
                .andExpect(jsonPath("$.highestPortfolioValue").value(8500.0))
                .andExpect(jsonPath("$.topPortfolioUser").value("alice"));
    }
}
