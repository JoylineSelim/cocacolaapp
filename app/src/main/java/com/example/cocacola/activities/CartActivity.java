package com.example.cocacola.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.example.cocacola.R;
import com.example.cocacola.adapters.CartAdapter;
import com.example.cocacola.models.CartItem;
import com.example.cocacola.utils.CartManager;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartManager.CartUpdateListener {
    private RecyclerView recyclerViewCart;
    private CartAdapter adapter;
    private TextView tvSubtotal, tvItemCount, tvTotal;
    private MaterialButton btnCheckout, btnClearCart, btnContinueShopping;
    private LinearLayout emptyCartState, bottomButtons;
    private Toolbar toolbar;

    private CartManager cartManager;
    private String branch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance();
        branch = getIntent().getStringExtra("branch");

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnClearCart = findViewById(R.id.btnClearCart);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
        emptyCartState = findViewById(R.id.emptyCartState);
        bottomButtons = findViewById(R.id.bottomButtons);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        adapter = new CartAdapter(this);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setAdapter(adapter);

        cartManager.addListener(this);
        updateUI();

        btnCheckout.setOnClickListener(v -> proceedToCheckout());

        btnClearCart.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Cart")
                    .setMessage("Are you sure you want to remove all items from cart?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        cartManager.clearCart();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        btnContinueShopping.setOnClickListener(v -> finish());
    }

    private void updateUI() {
        List<CartItem> items = cartManager.getCartItems();

        if (items.isEmpty()) {
            emptyCartState.setVisibility(View.VISIBLE);
            recyclerViewCart.setVisibility(View.GONE);
            findViewById(R.id.summaryCard).setVisibility(View.GONE);
            bottomButtons.setVisibility(View.GONE);
        } else {
            emptyCartState.setVisibility(View.GONE);
            recyclerViewCart.setVisibility(View.VISIBLE);
            findViewById(R.id.summaryCard).setVisibility(View.VISIBLE);
            bottomButtons.setVisibility(View.VISIBLE);

            adapter.setCartItems(items);

            double total = cartManager.getTotalAmount();
            int count = cartManager.getItemCount();

            tvSubtotal.setText(String.format("KSh %.2f", total));
            tvTotal.setText(String.format("KSh %.2f", total));
            tvItemCount.setText(count + " item" + (count != 1 ? "s" : ""));
        }
    }

    private void proceedToCheckout() {
        Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
        intent.putExtra("branch", branch);
        intent.putExtra("totalAmount", cartManager.getTotalAmount());
        intent.putExtra("items", (java.io.Serializable) cartManager.getItemsForCheckout());
        startActivity(intent);
    }

    @Override
    public void onCartUpdated() {
        runOnUiThread(() -> updateUI());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cartManager.removeListener(this);
    }
}