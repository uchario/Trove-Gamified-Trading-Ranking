package com.trading.TradingApplication.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Portfolio {
    private final List<Asset> assets = new ArrayList<>();

    public void addAsset(Asset asset) {
        assets.add(asset);
    }

    public void removeAsset(Asset asset) {
        assets.remove(asset);
    }

    public Asset findByName(String name) {
        return assets.stream()
                .filter(a -> a.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public double calculateValue() {
        return assets.stream().mapToDouble(Asset::getValue).sum();
    }
}
