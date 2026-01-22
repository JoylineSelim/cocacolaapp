package com.example.cocacola.utils;

import com.example.cocacola.models.CartItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static CartManager instance;
    private Map<String, CartItem> cartItems;
    private List<CartUpdateListener> listeners;

    public interface CartUpdateListener {
        void onCartUpdated();
    }

    private CartManager() {
        cartItems = new HashMap<>();
        listeners = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addListener(CartUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(CartUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (CartUpdateListener listener : listeners) {
            listener.onCartUpdated();
        }
    }

    public void addItem(CartItem item) {
        String productId = item.getProductId();

        if (cartItems.containsKey(productId)) {
            CartItem existingItem = cartItems.get(productId);
            int newQuantity = existingItem.getQuantity() + item.getQuantity();

            if (newQuantity <= existingItem.getAvailableStock()) {
                existingItem.setQuantity(newQuantity);
            } else {
                existingItem.setQuantity(existingItem.getAvailableStock());
            }
        } else {
            cartItems.put(productId, item);
        }

        notifyListeners();
    }

    public void updateQuantity(String productId, int quantity) {
        if (cartItems.containsKey(productId)) {
            CartItem item = cartItems.get(productId);

            if (quantity <= 0) {
                removeItem(productId);
            } else if (quantity <= item.getAvailableStock()) {
                item.setQuantity(quantity);
                notifyListeners();
            }
        }
    }

    public void incrementItem(String productId) {
        if (cartItems.containsKey(productId)) {
            CartItem item = cartItems.get(productId);
            if (item.canAddMore()) {
                item.setQuantity(item.getQuantity() + 1);
                notifyListeners();
            }
        }
    }

    public void decrementItem(String productId) {
        if (cartItems.containsKey(productId)) {
            CartItem item = cartItems.get(productId);
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyListeners();
            } else {
                removeItem(productId);
            }
        }
    }

    public void removeItem(String productId) {
        cartItems.remove(productId);
        notifyListeners();
    }

    public void clearCart() {
        cartItems.clear();
        notifyListeners();
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems.values());
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : cartItems.values()) {
            count += item.getQuantity();
        }
        return count;
    }

    public double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems.values()) {
            total += item.getSubtotal();
        }
        return total;
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public boolean containsProduct(String productId) {
        return cartItems.containsKey(productId);
    }

    public CartItem getItem(String productId) {
        return cartItems.get(productId);
    }

    public Map<String, Integer> getItemsForCheckout() {
        Map<String, Integer> items = new HashMap<>();
        for (CartItem item : cartItems.values()) {
            items.put(item.getProductName(), item.getQuantity());
        }
        return items;
    }
}