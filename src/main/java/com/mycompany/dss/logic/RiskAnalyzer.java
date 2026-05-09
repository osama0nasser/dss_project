package com.mycompany.dss.logic;

import com.mycompany.dss.model.ScenarioResult;
import java.util.ArrayList;

public class RiskAnalyzer {
    public String getRiskLevel(double baseProfit, ArrayList<ScenarioResult> results) {
        if (results.isEmpty())
            return "Unknown";
        double avgProfit = results.stream().mapToDouble(ScenarioResult::getProfit).average().orElse(0);
        double ratio = avgProfit / baseProfit;
        if (ratio < 1)
            return "High Risk";
        if (ratio < 1.2)
            return "Medium Risk";
        return "Low Risk";
    }
}