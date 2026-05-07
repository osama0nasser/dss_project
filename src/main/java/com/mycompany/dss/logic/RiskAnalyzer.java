package com.mycompany.dss.logic;

import com.mycompany.dss.model.ScenarioResult;
import java.util.ArrayList;

public class RiskAnalyzer {
    public String getRiskLevel(double baseProfit, ArrayList<ScenarioResult> results) {
        if (results.isEmpty()) return "Unknown";
        double avgProfit = results.stream().mapToDouble(ScenarioResult::getProfit).average().orElse(0);
        double ratio = avgProfit / baseProfit;
        if (ratio < 0.7) return "High Risk";
        if (ratio < 0.9) return "Medium Risk";
        return "Low Risk";
    }
}