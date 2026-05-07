package com.mycompany.dss.model;

public class Product {
    private int id;
    private String name;
    private double price;
    private double cost;
    private int demand;

    public Product(String name, double price, double cost, int demand) {
        this.id = 0;
        this.name = name;
        this.price = price;
        this.cost = cost;
        this.demand = demand;
    }

    public Product(int id, String name, double price, double cost, int demand) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.cost = cost;
        this.demand = demand;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    public int getDemand() { return demand; }
    public void setDemand(int demand) { this.demand = demand; }

    public double getProfit() { return (price - cost) * demand; }
    public double getMargin() { return (price - cost) / price * 100; }
}