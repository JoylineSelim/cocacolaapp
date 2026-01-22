package com.example.cocacola.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.cocacola.R;
import com.example.cocacola.activities.RestockActivity.InventoryItem;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
    private Context context;
    private List<InventoryItem> items;
    private DatabaseReference mDatabase;

    public InventoryAdapter(Context context, List<InventoryItem> items) {
        this.context = context;
        this.items = items;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class InventoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName, tvBranchName, tvStockStatus, tvStockQuantity, tvLastUpdated;
        private ProgressBar progressStock;
        private TextInputEditText etStockAdjustment, etPrice;
        private MaterialButton btnUpdateStock, btnUpdatePrice;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvBranchName = itemView.findViewById(R.id.tvBranchName);
            tvStockStatus = itemView.findViewById(R.id.tvStockStatus);
            tvStockQuantity = itemView.findViewById(R.id.tvStockQuantity);
            tvLastUpdated = itemView.findViewById(R.id.tvLastUpdated);
            progressStock = itemView.findViewById(R.id.progressStock);
            etStockAdjustment = itemView.findViewById(R.id.etStockAdjustment);
            etPrice = itemView.findViewById(R.id.etPrice);
            btnUpdateStock = itemView.findViewById(R.id.btnUpdateStock);
            btnUpdatePrice = itemView.findViewById(R.id.btnUpdatePrice);
        }

        public void bind(InventoryItem item) {
            tvProductName.setText(item.getProductName());
            tvBranchName.setText(item.getBranchName() + " Branch");
            tvStockQuantity.setText(item.getQuantity() + " / " + item.getMaxCapacity());
            etPrice.setText(String.format("%.2f", item.getPrice()));

            // Update stock status badge
            String status = item.getStockStatus();
            tvStockStatus.setText(status.replace("_", " "));
            updateStatusBadge(status);

            // Update progress bar
            int percentage = item.getStockPercentage();
            progressStock.setProgress(percentage);
            updateProgressColor(percentage);

            // Last updated info
            tvLastUpdated.setText("Last updated: " +
                    DateUtils.getRelativeTimeSpanString(System.currentTimeMillis() - 3600000));

            // Update Stock Button
            btnUpdateStock.setOnClickListener(v -> {
                String adjustmentStr = etStockAdjustment.getText().toString().trim();
                if (adjustmentStr.isEmpty()) {
                    etStockAdjustment.setError("Enter quantity");
                    return;
                }

                try {
                    int adjustment = Integer.parseInt(adjustmentStr);
                    updateStock(item, adjustment);
                } catch (NumberFormatException e) {
                    etStockAdjustment.setError("Invalid number");
                }
            });

            // Update Price Button
            btnUpdatePrice.setOnClickListener(v -> {
                String priceStr = etPrice.getText().toString().trim();
                if (priceStr.isEmpty()) {
                    etPrice.setError("Enter price");
                    return;
                }

                try {
                    double newPrice = Double.parseDouble(priceStr);
                    if (newPrice <= 0) {
                        etPrice.setError("Price must be positive");
                        return;
                    }
                    updatePrice(item, newPrice);
                } catch (NumberFormatException e) {
                    etPrice.setError("Invalid price");
                }
            });
        }

        private void updateStatusBadge(String status) {
            switch (status) {
                case "HEALTHY":
                    tvStockStatus.setBackgroundResource(R.drawable.bg_stock_badge_in);
                    tvStockStatus.setTextColor(Color.WHITE);
                    break;
                case "LOW":
                    tvStockStatus.setBackgroundResource(R.drawable.bg_stock_badge_low);
                    tvStockStatus.setTextColor(Color.WHITE);
                    break;
                case "CRITICAL":
                case "OUT_OF_STOCK":
                    tvStockStatus.setBackgroundResource(R.drawable.bg_stock_badge_out);
                    tvStockStatus.setTextColor(Color.WHITE);
                    break;
            }
        }

        private void updateProgressColor(int percentage) {
            int color;
            if (percentage == 0) {
                color = context.getResources().getColor(R.color.status_out_of_stock);
            } else if (percentage <= 20) {
                color = context.getResources().getColor(R.color.status_out_of_stock);
            } else if (percentage <= 40) {
                color = context.getResources().getColor(R.color.status_low_stock);
            } else {
                color = context.getResources().getColor(R.color.status_in_stock);
            }
            progressStock.getProgressDrawable().setColorFilter(color,
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }

        private void updateStock(InventoryItem item, int adjustment) {
            int newQuantity = item.getQuantity() + adjustment;

            if (newQuantity < 0) {
                Toast.makeText(context, "Cannot reduce below 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newQuantity > item.getMaxCapacity()) {
                Toast.makeText(context,
                        "Cannot exceed max capacity of " + item.getMaxCapacity(),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Update in Firebase
            DatabaseReference stockRef = mDatabase.child("branches")
                    .child(item.getBranchName())
                    .child("stock")
                    .child(item.getProductName());

            stockRef.setValue(newQuantity).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    item.setQuantity(newQuantity);
                    notifyItemChanged(getAdapterPosition());
                    etStockAdjustment.setText("");
                    Toast.makeText(context,
                            "✅ Stock updated: " + item.getProductName() + " at " +
                                    item.getBranchName() + " = " + newQuantity,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "❌ Failed to update stock",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void updatePrice(InventoryItem item, double newPrice) {
            // Update in Firebase products node (global price)
            DatabaseReference productRef = mDatabase.child("products")
                    .child(item.getProductId())
                    .child("price");

            productRef.setValue(newPrice).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    item.setPrice(newPrice);
                    notifyItemChanged(getAdapterPosition());
                    Toast.makeText(context,
                            "✅ Price updated: " + item.getProductName() +
                                    " = KSh " + String.format("%.2f", newPrice),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "❌ Failed to update price",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}