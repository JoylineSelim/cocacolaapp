package com.example.cocacola.models;

import java.util.Map;

public class Sale {
    private String saleId;
    private String userId;
    private String branch;
    private Map<String, Integer> items; // productName -> quantity
    private double totalAmount;
    private long timestamp;
    private String mpesaTransactionId;
    private String status; // "pending", "completed", "failed"

    public Sale() {
        // Default constructor required for Firebase
    }

    public Sale(String saleId, String userId, String branch, Map<String, Integer> items,
                double totalAmount, String mpesaTransactionId) {
        this.saleId = saleId;
        this.userId = userId;
        this.branch = branch;
        this.items = items;
        this.totalAmount = totalAmount;
        this.timestamp = System.currentTimeMillis();
        this.mpesaTransactionId = mpesaTransactionId;
        this.status = "pending";
    }

    // Getters and Setters
    public String getSaleId() { return saleId; }
    public void setSaleId(String saleId) { this.saleId = saleId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public Map<String, Integer> getItems() { return items; }
    public void setItems(Map<String, Integer> items) { this.items = items; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getMpesaTransactionId() { return mpesaTransactionId; }
    public void setMpesaTransactionId(String mpesaTransactionId) {
        this.mpesaTransactionId = mpesaTransactionId;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

