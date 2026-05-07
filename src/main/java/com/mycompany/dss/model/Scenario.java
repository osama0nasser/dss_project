package com.mycompany.dss.model;

public class Scenario {
    private int id;
    private String name;
    private double priceChange;
    private double costChange;
    private double demandChange;

    public Scenario(String name, double priceChange, double costChange, double demandChange) {
        this.id = 0;
        this.name = name;
        this.priceChange = priceChange;
        this.costChange = costChange;
        this.demandChange = demandChange;
    }

    public Scenario(int id, String name, double priceChange, double costChange, double demandChange) {
        this.id = id;
        this.name = name;
        this.priceChange = priceChange;
        this.costChange = costChange;
        this.demandChange = demandChange;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPriceChange() { return priceChange; }
    public void setPriceChange(double priceChange) { this.priceChange = priceChange; }
    public double getCostChange() { return costChange; }
    public void setCostChange(double costChange) { this.costChange = costChange; }
    public double getDemandChange() { return demandChange; }
    public void setDemandChange(double demandChange) { this.demandChange = demandChange; }
}