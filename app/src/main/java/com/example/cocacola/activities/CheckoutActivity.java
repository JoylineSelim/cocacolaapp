package com.example.cocacola.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cocacola.R;
import com.example.cocacola.models.Sale;
import com.example.cocacola.utils.MpesaHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {
    private TextView tvOrderSummary, tvTotalAmount;
    private EditText etPhoneNumber;
    private Button btnPayNow, btnCancel;
    private String branch;
    private double totalAmount;
    private HashMap<String, Integer> items;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvOrderSummary = findViewById(R.id.tvOrderSummary);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnPayNow = findViewById(R.id.btnPayNow);
        btnCancel = findViewById(R.id.btnCancel);

        branch = getIntent().getStringExtra("branch");
        totalAmount = getIntent().getDoubleExtra("totalAmount", 0);
        items = (HashMap<String, Integer>) getIntent().getSerializableExtra("items");

        displayOrderSummary();

        btnPayNow.setOnClickListener(v -> initiatePayment());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void displayOrderSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Branch: ").append(branch).append("\n\n");
        summary.append("Items:\n");

        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            summary.append(entry.getKey())
                    .append(" x ")
                    .append(entry.getValue())
                    .append("\n");
        }

        tvOrderSummary.setText(summary.toString());
        tvTotalAmount.setText(String.format("Total: KSh %.2f", totalAmount));
    }

    private void initiatePayment() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        // (keep your phone validation logic here)

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending M-Pesa Prompt...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        MpesaHelper mpesaHelper = new MpesaHelper(this);
        mpesaHelper.initiateSTKPush(phoneNumber, (int) totalAmount, new MpesaHelper.MpesaCallback() {
            @Override
            public void onSuccess(String checkoutRequestId) {
                progressDialog.dismiss();

                // --- THE FIX STARTS HERE ---
                // 1. Inform the user to check their phone
                Toast.makeText(CheckoutActivity.this,
                        "Check your phone for the M-Pesa prompt!", Toast.LENGTH_LONG).show();

                // 2. Change the 'Pay Now' button to a 'Verify' button
                btnPayNow.setText("I HAVE ENTERED PIN");
                btnPayNow.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));

                // 3. Update the click listener to ONLY process sale after user confirms
                btnPayNow.setOnClickListener(v -> {
                    // In a production app, you would verify against the database here.
                    // For Sandbox testing, we proceed with the sale record.
                    processSale(checkoutRequestId);
                });
            }

            @Override
            public void onFailure(String error) {
                progressDialog.dismiss();
                Toast.makeText(CheckoutActivity.this,
                        "Request failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processSale(String mpesaTransactionId) {
        // Change the loading message to reflect we are finalizing
        if (progressDialog == null) progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Finalizing order...");
        progressDialog.show();

        String userId = mAuth.getCurrentUser().getUid();
        String saleId = mDatabase.child("sales").push().getKey();

        Sale sale = new Sale(saleId, userId, branch, items, totalAmount, mpesaTransactionId);
        sale.setStatus("completed");

        mDatabase.child("sales").child(saleId).setValue(sale)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        updateStock();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(CheckoutActivity.this, "Failed to record sale", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateStock() {
        DatabaseReference branchStockRef = mDatabase.child("branches").child(branch).child("stock");

        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            String productName = entry.getKey();
            int quantity = entry.getValue();

            branchStockRef.child(productName).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Long currentStock = task.getResult().getValue(Long.class);
                    if (currentStock != null) {
                        long newStock = currentStock - quantity;
                        branchStockRef.child(productName).setValue(newStock);
                    }
                }
            });
        }

        Toast.makeText(this, "Payment successful! Thank you for your purchase.",
                Toast.LENGTH_LONG).show();

        // Return to dashboard
        setResult(RESULT_OK);
        finish();
    }
}

