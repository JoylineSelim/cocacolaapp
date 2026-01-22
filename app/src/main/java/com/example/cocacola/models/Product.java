package com.example.cocacola.models;

public class Product {
    private String productId;
    private String name;
    private double price;
    private int quantity;
    private int maxCapacity; // For showing stock levels as percentage
    private String imageUrl;
    private long lastUpdated;

    public Product() {
        // Default constructor required for Firebase
    }

    public Product(String productId, String name, double price) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = 0;
        this.maxCapacity = 500; // Default max capacity
        this.lastUpdated = System.currentTimeMillis();
    }

    public Product(String productId, String name, double price, int quantity, int maxCapacity) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.maxCapacity = maxCapacity;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Calculate stock percentage
    public int getStockPercentage() {
        if (maxCapacity == 0) return 0;
        return (int) ((quantity * 100.0) / maxCapacity);
    }

    // Get stock status
    public String getStockStatus() {
        int percentage = getStockPercentage();
        if (percentage == 0) return "OUT_OF_STOCK";
        if (percentage <= 20) return "CRITICAL";
        if (percentage <= 40) return "LOW";
        return "HEALTHY";
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) {
        this.price = price;
        this.lastUpdated = System.currentTimeMillis();
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.lastUpdated = System.currentTimeMillis();
    }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}
