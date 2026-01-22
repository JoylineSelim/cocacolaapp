package com.example.cocacola.models;

import java.util.HashMap;
import java.util.Map;

public class Branch {
    private String branchName;
    private Map<String, Integer> stock; // productName -> quantity

    public Branch() {
        // Default constructor required for Firebase
        this.stock = new HashMap<>();
    }

    public Branch(String branchName) {
        this.branchName = branchName;
        this.stock = new HashMap<>();
        // Initialize with default stock
        stock.put("Coke", 100);
        stock.put("Fanta", 100);
        stock.put("Sprite", 100);
    }

    // Getters and Setters
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public Map<String, Integer> getStock() { return stock; }
    public void setStock(Map<String, Integer> stock) { this.stock = stock; }

    public void updateStock(String productName, int quantity) {
        if (stock.containsKey(productName)) {
            stock.put(productName, stock.get(productName) + quantity);
        } else {
            stock.put(productName, quantity);
        }
    }
}