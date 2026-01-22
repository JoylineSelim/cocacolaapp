package com.example.cocacola.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.cocacola.R;
import com.example.cocacola.adapters.ProductGridAdapter;
import com.example.cocacola.models.Product;
import com.example.cocacola.utils.CartManager;
import java.util.ArrayList;
import java.util.List;

public class ShoppingActivity extends AppCompatActivity implements CartManager.CartUpdateListener {
    private RecyclerView recyclerViewProducts;
    private ProductGridAdapter adapter;
    private List<Product> productList;
    private TextView tvBranch, tvTotal, tvCartBadge;
    private MaterialButton btnCheckout;
    private ImageButton btnBack, btnCart;
    private LinearLayout emptyState;
    private DatabaseReference mDatabase;
    private String selectedBranch;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping);

        selectedBranch = getIntent().getStringExtra("branch");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        cartManager = CartManager.getInstance();

        // Initialize views
        tvBranch = findViewById(R.id.tvBranch);
        tvTotal = findViewById(R.id.tvTotal);
        tvCartBadge = findViewById(R.id.tvCartBadge);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBack);
        btnCart = findViewById(R.id.btnCart);
        emptyState = findViewById(R.id.emptyState);

        tvBranch.setText(selectedBranch + " Branch");

        // Setup RecyclerView with Grid Layout
        productList = new ArrayList<>();
        adapter = new ProductGridAdapter(this, productList, selectedBranch);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerViewProducts.setLayoutManager(layoutManager);
        recyclerViewProducts.setAdapter(adapter);

        // Add cart listener
        cartManager.addListener(this);
        updateCartUI();

        loadProducts();

        btnBack.setOnClickListener(v -> finish());

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(ShoppingActivity.this, CartActivity.class);
            intent.putExtra("branch", selectedBranch);
            startActivity(intent);
        });

        btnCheckout.setOnClickListener(v -> {
            if (cartManager.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ShoppingActivity.this, CartActivity.class);
            intent.putExtra("branch", selectedBranch);
            startActivity(intent);
        });
    }

    private void loadProducts() {
        mDatabase.child("products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String productId = snapshot.getKey();
                    String name = snapshot.child("name").getValue(String.class);
                    Double price = snapshot.child("price").getValue(Double.class);

                    if (name != null && price != null) {
                        Product product = new Product(productId, name, price);
                        loadStockForProduct(product);
                        productList.add(product);
                    }
                }

                if (productList.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    recyclerViewProducts.setVisibility(View.GONE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    recyclerViewProducts.setVisibility(View.VISIBLE);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ShoppingActivity.this,
                        "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadStockForProduct(Product product) {
        mDatabase.child("branches").child(selectedBranch).child("stock")
                .child(product.getName()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Long stock = snapshot.getValue(Long.class);
                        if (stock != null) {
                            // Find the product in the list and update
                            for (Product p : productList) {
                                if (p.getProductId().equals(product.getProductId())) {
                                    p.setQuantity(stock.intValue());
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Handle error
                    }
                });
    }

    @Override
    public void onCartUpdated() {
        updateCartUI();
    }

    private void updateCartUI() {
        double total = cartManager.getTotalAmount();
        int itemCount = cartManager.getItemCount();

        tvTotal.setText(String.format("KSh %.2f", total));

        if (itemCount > 0) {
            tvCartBadge.setText(String.valueOf(itemCount));
            tvCartBadge.setVisibility(View.VISIBLE);
            btnCheckout.setVisibility(View.VISIBLE);
        } else {
            tvCartBadge.setVisibility(View.GONE);
            btnCheckout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cartManager.removeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartUI();
        adapter.notifyDataSetChanged();
    }
}