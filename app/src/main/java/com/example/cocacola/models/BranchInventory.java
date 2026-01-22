package com.example.cocacola.models;

import java.util.HashMap;
import java.util.Map;

public class BranchInventory {
    private String branchName;
    private Map<String, ProductStock> stock;

    public BranchInventory() {
        this.stock = new HashMap<>();
    }

    public BranchInventory(String branchName) {
        this.branchName = branchName;
        this.stock = new HashMap<>();
    }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public Map<String, ProductStock> getStock() { return stock; }
    public void setStock(Map<String, ProductStock> stock) { this.stock = stock; }

    public static class ProductStock {
        private int quantity;
        private double price;
        private long lastUpdated;

        public ProductStock() {}

        public ProductStock(int quantity, double price) {
            this.quantity = quantity;
            this.price = price;
            this.lastUpdated = System.currentTimeMillis();
        }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) {
            this.quantity = quantity;
            this.lastUpdated = System.currentTimeMillis();
        }

        public double getPrice() { return price; }
        public void setPrice(double price) {
            this.price = price;
            this.lastUpdated = System.currentTimeMillis();
        }

        public long getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
    }
}
