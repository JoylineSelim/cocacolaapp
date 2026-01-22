package com.example.cocacola.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.cocacola.R;
import com.example.cocacola.adapters.SalesReportAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesReportActivity extends AppCompatActivity {
    private RecyclerView recyclerViewReport;
    private SalesReportAdapter adapter;
    private TextView tvGrandTotal, tvTotalOrders, tvCokeTotal, tvFantaTotal, tvSpriteTotal;
    private ProgressBar progressCoke, progressFanta, progressSprite;
    private AutoCompleteTextView spinnerBranch, spinnerProduct;
    private MaterialButton btnApplyFilter;
    private LinearLayout emptyState;
    private Toolbar toolbar;
    private DatabaseReference mDatabase;

    private String selectedBranch = "All Branches";
    private String selectedProduct = "All Products";
    private Map<String, Double> brandTotals;
    private double grandTotal = 0;
    private int totalOrders = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_report);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        brandTotals = new HashMap<>();
        brandTotals.put("Coke", 0.0);
        brandTotals.put("Fanta", 0.0);
        brandTotals.put("Sprite", 0.0);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        recyclerViewReport = findViewById(R.id.recyclerViewReport);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvCokeTotal = findViewById(R.id.tvCokeTotal);
        tvFantaTotal = findViewById(R.id.tvFantaTotal);
        tvSpriteTotal = findViewById(R.id.tvSpriteTotal);
        progressCoke = findViewById(R.id.progressCoke);
        progressFanta = findViewById(R.id.progressFanta);
        progressSprite = findViewById(R.id.progressSprite);
        spinnerBranch = findViewById(R.id.spinnerBranch);
        spinnerProduct = findViewById(R.id.spinnerProduct);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        emptyState = findViewById(R.id.emptyState);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new SalesReportAdapter();
        recyclerViewReport.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReport.setAdapter(adapter);

        setupFilters();
        loadSalesReport();

        btnApplyFilter.setOnClickListener(v -> {
            selectedBranch = spinnerBranch.getText().toString();
            selectedProduct = spinnerProduct.getText().toString();
            loadSalesReport();
        });
    }

    private void setupFilters() {
        // Branch filter
        String[] branches = {"All Branches", "Nairobi", "Kisumu", "Mombasa", "Nakuru", "Eldoret"};
        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, branches);
        spinnerBranch.setAdapter(branchAdapter);

        // Product filter
        String[] products = {"All Products", "Coke", "Fanta", "Sprite"};
        ArrayAdapter<String> productAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, products);
        spinnerProduct.setAdapter(productAdapter);
    }

    private void loadSalesReport() {
        mDatabase.child("sales").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Map<String, Object>> salesList = new ArrayList<>();
                grandTotal = 0;
                totalOrders = 0;
                brandTotals.put("Coke", 0.0);
                brandTotals.put("Fanta", 0.0);
                brandTotals.put("Sprite", 0.0);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String branch = snapshot.child("branch").getValue(String.class);
                    Double totalAmount = snapshot.child("totalAmount").getValue(Double.class);
                    String status = snapshot.child("status").getValue(String.class);

                    // Apply branch filter
                    if (!selectedBranch.equals("All Branches") && !selectedBranch.equals(branch)) {
                        continue;
                    }

                    if ("completed".equals(status) && totalAmount != null) {
                        DataSnapshot itemsSnapshot = snapshot.child("items");
                        Map<String, Object> saleData = new HashMap<>();
                        boolean hasSelectedProduct = false;

                        // Calculate brand-wise totals
                        for (DataSnapshot itemSnapshot : itemsSnapshot.getChildren()) {
                            String productName = itemSnapshot.getKey();
                            Long quantity = itemSnapshot.getValue(Long.class);

                            if (productName != null && quantity != null) {
                                // Apply product filter
                                if (!selectedProduct.equals("All Products") &&
                                        !selectedProduct.equalsIgnoreCase(productName)) {
                                    continue;
                                }

                                hasSelectedProduct = true;
                                saleData.put(productName, quantity);

                                // Update brand totals
                                getProductPriceAndUpdate(productName, quantity);
                            }
                        }

                        // Only add if matches product filter
                        if (selectedProduct.equals("All Products") || hasSelectedProduct) {
                            saleData.put("branch", branch);
                            saleData.put("totalAmount", totalAmount);
                            salesList.add(saleData);
                            grandTotal += totalAmount;
                            totalOrders++;
                        }
                    }
                }

                if (salesList.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    recyclerViewReport.setVisibility(View.GONE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    recyclerViewReport.setVisibility(View.VISIBLE);
                }

                adapter.setSalesList(salesList);
                updateTotals();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }

    private void getProductPriceAndUpdate(String productName, Long quantity) {
        mDatabase.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (productName.equals(name)) {
                        Double price = snapshot.child("price").getValue(Double.class);
                        if (price != null) {
                            double currentTotal = brandTotals.get(productName);
                            brandTotals.put(productName, currentTotal + (price * quantity));
                            updateTotals();
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }

    private void updateTotals() {
        tvGrandTotal.setText(String.format("KSh %.2f", grandTotal));
        tvTotalOrders.setText(String.valueOf(totalOrders));

        double cokeTotal = brandTotals.get("Coke");
        double fantaTotal = brandTotals.get("Fanta");
        double spriteTotal = brandTotals.get("Sprite");

        tvCokeTotal.setText(String.format("KSh %.2f", cokeTotal));
        tvFantaTotal.setText(String.format("KSh %.2f", fantaTotal));
        tvSpriteTotal.setText(String.format("KSh %.2f", spriteTotal));

        // Update progress bars
        if (grandTotal > 0) {
            progressCoke.setProgress((int)((cokeTotal / grandTotal) * 100));
            progressFanta.setProgress((int)((fantaTotal / grandTotal) * 100));
            progressSprite.setProgress((int)((spriteTotal / grandTotal) * 100));
        }
    }
}