package com.example.cocacola.models;

public class CartItem {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
    private String imageUrl;
    private int availableStock;

    public CartItem() {
        // Default constructor required for Firebase
    }

    public CartItem(String productId, String productName, double price, int quantity, int availableStock) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.availableStock = availableStock;
    }

    public double getSubtotal() {
        return price * quantity;
    }

    public boolean canAddMore() {
        return quantity < availableStock;
    }

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getAvailableStock() { return availableStock; }
    public void setAvailableStock(int availableStock) { this.availableStock = availableStock; }
}
