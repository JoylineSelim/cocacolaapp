package com.example.cocacola.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.example.cocacola.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PurchaseSummaryActivity extends AppCompatActivity {
    private TextView tvTransactionId, tvMpesaRef, tvDateTime, tvBranch;
    private TextView tvItemsList, tvTotalPaid;
    private MaterialButton btnDownloadReceipt, btnDone;

    private String transactionId, mpesaRef, branch;
    private double totalAmount;
    private HashMap<String, Integer> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_summary);

        // Initialize views
        tvTransactionId = findViewById(R.id.tvTransactionId);
        tvMpesaRef = findViewById(R.id.tvMpesaRef);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvBranch = findViewById(R.id.tvBranch);
        tvItemsList = findViewById(R.id.tvItemsList);
        tvTotalPaid = findViewById(R.id.tvTotalPaid);
        btnDownloadReceipt = findViewById(R.id.btnDownloadReceipt);
        btnDone = findViewById(R.id.btnDone);

        // Get intent data
        transactionId = getIntent().getStringExtra("transactionId");
        mpesaRef = getIntent().getStringExtra("mpesaRef");
        branch = getIntent().getStringExtra("branch");
        totalAmount = getIntent().getDoubleExtra("totalAmount", 0);
        items = (HashMap<String, Integer>) getIntent().getSerializableExtra("items");

        displayReceipt();

        btnDownloadReceipt.setOnClickListener(v -> downloadReceipt());
        btnDone.setOnClickListener(v -> navigateToDashboard());
    }

    private void displayReceipt() {
        // Transaction details
        tvTransactionId.setText(transactionId);
        tvMpesaRef.setText(mpesaRef != null ? mpesaRef : "N/A");
        tvBranch.setText(branch);

        // Date and time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        tvDateTime.setText(sdf.format(new Date()));

        // Items list
        StringBuilder itemsList = new StringBuilder();
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            itemsList.append(entry.getKey())
                    .append(" × ")
                    .append(entry.getValue())
                    .append("\n");
        }
        tvItemsList.setText(itemsList.toString().trim());

        // Total amount
        tvTotalPaid.setText(String.format("KSh %.2f", totalAmount));
    }

    private void downloadReceipt() {
        // Generate receipt text
        String receipt = generateReceiptText();

        // In a real app, you would:
        // 1. Generate a PDF using library like iText
        // 2. Save to device storage
        // 3. Share via intent

        // For now, just copy to clipboard
        android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip =
                android.content.ClipData.newPlainText("Receipt", receipt);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "✅ Receipt copied to clipboard!", Toast.LENGTH_LONG).show();
    }

    private String generateReceiptText() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("════════════════════════════\n");
        receipt.append("   COCA-COLA DISTRIBUTION\n");
        receipt.append("════════════════════════════\n\n");

        receipt.append("Transaction ID: ").append(transactionId).append("\n");
        receipt.append("M-Pesa Ref: ").append(mpesaRef).append("\n");
        receipt.append("Branch: ").append(branch).append("\n");

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        receipt.append("Date: ").append(sdf.format(new Date())).append("\n\n");

        receipt.append("ITEMS PURCHASED:\n");
        receipt.append("────────────────────────────\n");

        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            receipt.append(String.format("%-20s x%d\n", entry.getKey(), entry.getValue()));
        }

        receipt.append("────────────────────────────\n");
        receipt.append(String.format("TOTAL PAID: KSh %.2f\n", totalAmount));
        receipt.append("════════════════════════════\n\n");
        receipt.append("Thank you for your purchase!\n");

        return receipt.toString();
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(PurchaseSummaryActivity.this,
                com.example.cocacola.activities.CustomerDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to checkout after successful payment
        navigateToDashboard();
    }
}
