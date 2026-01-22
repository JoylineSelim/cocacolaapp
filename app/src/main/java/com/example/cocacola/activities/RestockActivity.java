package com.example.cocacola.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.cocacola.R;
import com.example.cocacola.adapters.InventoryAdapter;
import com.example.cocacola.models.BranchInventory;
import java.util.ArrayList;
import java.util.List;

public class RestockActivity extends AppCompatActivity {
    private RecyclerView recyclerViewInventory;
    private InventoryAdapter adapter;
    private AutoCompleteTextView filterBranch;
    private TextInputEditText searchProduct;
    private MaterialButton btnBack, btnSaveAll;

    private DatabaseReference mDatabase;
    private List<InventoryItem> inventoryItems;
    private List<InventoryItem> filteredItems;
    private String selectedBranch = "All Branches";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restock);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        initializeViews();
        setupBranchFilter();
        setupSearch();
        loadInventory();

        btnBack.setOnClickListener(v -> finish());
        btnSaveAll.setOnClickListener(v -> saveAllChanges());
    }

    private void initializeViews() {
        recyclerViewInventory = findViewById(R.id.recyclerViewInventory);
        filterBranch = findViewById(R.id.filterBranch);
        searchProduct = findViewById(R.id.searchProduct);
        btnBack = findViewById(R.id.btnBack);
        btnSaveAll = findViewById(R.id.btnSaveAll);

        inventoryItems = new ArrayList<>();
        filteredItems = new ArrayList<>();

        adapter = new InventoryAdapter(this, filteredItems);
        recyclerViewInventory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewInventory.setAdapter(adapter);
    }

    private void setupBranchFilter() {
        String[] branches = {"All Branches", "Nairobi", "Kisumu", "Mombasa", "Nakuru", "Eldoret"};
        ArrayAdapter<String> branchAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, branches);
        filterBranch.setAdapter(branchAdapter);

        filterBranch.setOnItemClickListener((parent, view, position, id) -> {
            selectedBranch = branches[position];
            filterInventory();
        });
    }

    private void setupSearch() {
        searchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterInventory();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadInventory() {
        String[] branches = {"Nairobi", "Kisumu", "Mombasa", "Nakuru", "Eldoret"};
        String[] products = {"Coke", "Fanta", "Sprite"};

        inventoryItems.clear();

        for (String branch : branches) {
            for (String product : products) {
                loadBranchProductData(branch, product);
            }
        }
    }

    private void loadBranchProductData(String branch, String productName) {
        DatabaseReference productRef = mDatabase.child("branches").child(branch)
                .child("stock").child(productName);

        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Long quantity = snapshot.getValue(Long.class);

                // Get price from products node
                mDatabase.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot prodSnapshot : dataSnapshot.getChildren()) {
                            String name = prodSnapshot.child("name").getValue(String.class);
                            if (productName.equals(name)) {
                                Double price = prodSnapshot.child("price").getValue(Double.class);
                                String productId = prodSnapshot.getKey();

                                InventoryItem item = new InventoryItem(
                                        productId,
                                        productName,
                                        branch,
                                        quantity != null ? quantity.intValue() : 0,
                                        price != null ? price : 50.0,
                                        500 // Max capacity
                                );

                                updateOrAddInventoryItem(item);
                                filterInventory();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RestockActivity.this,
                        "Failed to load inventory", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrAddInventoryItem(InventoryItem newItem) {
        boolean found = false;
        for (int i = 0; i < inventoryItems.size(); i++) {
            InventoryItem item = inventoryItems.get(i);
            if (item.getProductName().equals(newItem.getProductName()) &&
                    item.getBranchName().equals(newItem.getBranchName())) {
                inventoryItems.set(i, newItem);
                found = true;
                break;
            }
        }
        if (!found) {
            inventoryItems.add(newItem);
        }
    }

    private void filterInventory() {
        String searchQuery = searchProduct.getText().toString().toLowerCase().trim();
        filteredItems.clear();

        for (InventoryItem item : inventoryItems) {
            boolean matchesBranch = selectedBranch.equals("All Branches") ||
                    item.getBranchName().equals(selectedBranch);
            boolean matchesSearch = searchQuery.isEmpty() ||
                    item.getProductName().toLowerCase().contains(searchQuery);

            if (matchesBranch && matchesSearch) {
                filteredItems.add(item);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void saveAllChanges() {
        Toast.makeText(this, "Saving all changes...", Toast.LENGTH_SHORT).show();
        // Changes are saved immediately when Update buttons are clicked
        // This is just a confirmation
        Toast.makeText(this, "✅ All changes saved successfully!", Toast.LENGTH_LONG).show();
    }

    // Inner class for inventory items
    public static class InventoryItem {
        private String productId;
        private String productName;
        private String branchName;
        private int quantity;
        private double price;
        private int maxCapacity;

        public InventoryItem(String productId, String productName, String branchName,
                             int quantity, double price, int maxCapacity) {
            this.productId = productId;
            this.productName = productName;
            this.branchName = branchName;
            this.quantity = quantity;
            this.price = price;
            this.maxCapacity = maxCapacity;
        }

        // Getters and setters
        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public String getBranchName() {
            return branchName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getMaxCapacity() {
            return maxCapacity;
        }

        public int getStockPercentage() {
            if (maxCapacity == 0) return 0;
            return (int) ((quantity * 100.0) / maxCapacity);
        }

        public String getStockStatus() {
            int percentage = getStockPercentage();
            if (percentage == 0) return "OUT_OF_STOCK";
            if (percentage <= 20) return "CRITICAL";
            if (percentage <= 40) return "LOW";
            return "HEALTHY";
        }
    }
}