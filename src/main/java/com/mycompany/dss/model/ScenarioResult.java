package com.mycompany.dss.model;

public class ScenarioResult {
    private String name;
    private double profit;
    public ScenarioResult(String name, double profit) { this.name = name; this.profit = profit; }
    public String getName() { return name; }
    public double getProfit() { return profit; }
}