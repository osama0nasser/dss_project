package com.mycompany.dss.logic;

import com.mycompany.dss.model.Product;
import com.mycompany.dss.model.Scenario;

public class DecisionEngine {
    public double calculateBaseProfit(Product product) {
        return (product.getPrice() - product.getCost()) * product.getDemand();
    }

    public double calculateScenarioProfit(Product product, Scenario scenario) {
        double newPrice = product.getPrice() * (1 + scenario.getPriceChange() / 100);
        double newCost = product.getCost() * (1 + scenario.getCostChange() / 100);
        double newDemand = product.getDemand() * (1 + scenario.getDemandChange() / 100);
        return (newPrice - newCost) * newDemand;
    }
}