package com.mycompany.dss.model;

import java.time.LocalDateTime;

public class ScenarioResultPersistent {
    private int id;
    private int productId;
    private String productName;
    private int scenarioId;
    private String scenarioName;
    private LocalDateTime analysisDate;
    private double profit;
    private double changePercent;
    private String riskLevel;

    // Constructor for inserting new result (without id)
    public ScenarioResultPersistent(int productId, String productName, int scenarioId, String scenarioName,
            LocalDateTime analysisDate, double profit, double changePercent, String riskLevel) {
        this.productId = productId;
        this.productName = productName;
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
        this.analysisDate = analysisDate;
        this.profit = profit;
        this.changePercent = changePercent;
        this.riskLevel = riskLevel;
    }

    // Constructor for loading from database
    public ScenarioResultPersistent(int id, int productId, String productName, int scenarioId, String scenarioName,
            LocalDateTime analysisDate, double profit, double changePercent, String riskLevel) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.scenarioId = scenarioId;
        this.scenarioName = scenarioName;
        this.analysisDate = analysisDate;
        this.profit = profit;
        this.changePercent = changePercent;
        this.riskLevel = riskLevel;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getScenarioId() {
        return scenarioId;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public LocalDateTime getAnalysisDate() {
        return analysisDate;
    }

    public double getProfit() {
        return profit;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public String getRiskLevel() {
        return riskLevel;
    }
}