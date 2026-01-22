package com.example.cocacola.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private DatabaseReference mDatabase;

    private FirebaseHelper() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public DatabaseReference getDatabase() {
        return mDatabase;
    }

    // Initialize default data structure
    public void initializeDefaultData() {
        // Initialize products
        Map<String, Object> products = new HashMap<>();

        Map<String, Object> coke = new HashMap<>();
        coke.put("name", "Coke");
        coke.put("price", 1.0);

        Map<String, Object> fanta = new HashMap<>();
        fanta.put("name", "Fanta");
        fanta.put("price", 50.0);

        Map<String, Object> sprite = new HashMap<>();
        sprite.put("name", "Sprite");
        sprite.put("price", 50.0);

        products.put("product1", coke);
        products.put("product2", fanta);
        products.put("product3", sprite);

        mDatabase.child("products").setValue(products);

        // Initialize branches with stock
        String[] branches = {"Nairobi", "Kisumu", "Mombasa", "Nakuru", "Eldoret"};

        for (String branch : branches) {
            Map<String, Integer> stock = new HashMap<>();
            stock.put("Coke", 100);
            stock.put("Fanta", 100);
            stock.put("Sprite", 100);

            Map<String, Object> branchData = new HashMap<>();
            branchData.put("stock", stock);

            mDatabase.child("branches").child(branch).setValue(branchData);
        }
    }

    // Helper method to get product price
    public interface PriceCallback {
        void onPriceRetrieved(double price);
        void onError(String error);
    }

    public void getProductPrice(String productName, PriceCallback callback) {
        mDatabase.child("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (com.google.firebase.database.DataSnapshot snapshot :
                        task.getResult().getChildren()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (productName.equals(name)) {
                        Double price = snapshot.child("price").getValue(Double.class);
                        if (price != null) {
                            callback.onPriceRetrieved(price);
                            return;
                        }
                    }
                }
                callback.onError("Product not found");
            } else {
                callback.onError("Database error");
            }
        });
    }

    // Helper method to check and update stock
    public interface StockCallback {
        void onStockChecked(boolean available);
        void onError(String error);
    }

    public void checkAndUpdateStock(String branch, String product, int quantity,
                                    boolean isDeduction, StockCallback callback) {
        DatabaseReference stockRef = mDatabase.child("branches").child(branch)
                .child("stock").child(product);

        stockRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Long currentStock = task.getResult().getValue(Long.class);

                if (currentStock == null) {
                    callback.onError("Stock data not found");
                    return;
                }

                if (isDeduction) {
                    if (currentStock < quantity) {
                        callback.onStockChecked(false);
                        return;
                    }
                    stockRef.setValue(currentStock - quantity);
                } else {
                    stockRef.setValue(currentStock + quantity);
                }

                callback.onStockChecked(true);
            } else {
                callback.onError("Database error");
            }
        });
    }
}
