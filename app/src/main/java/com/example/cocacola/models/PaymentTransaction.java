package com.example.cocacola.models;

public class PaymentTransaction {
    private String transactionId;
    private String userId;
    private String branch;
    private double amount;
    private String status; // PENDING, SUCCESS, FAILED, CANCELLED
    private String mpesaReference;
    private long timestamp;
    private String phoneNumber;

    public PaymentTransaction() {
        // Default constructor for Firebase
    }

    public PaymentTransaction(String transactionId, String userId, String branch,
                              double amount, String phoneNumber) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.branch = branch;
        this.amount = amount;
        this.phoneNumber = phoneNumber;
        this.status = "PENDING";
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMpesaReference() { return mpesaReference; }
    public void setMpesaReference(String mpesaReference) { this.mpesaReference = mpesaReference; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}