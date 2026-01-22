package com.example.cocacola.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cocacola.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SalesReportAdapter extends RecyclerView.Adapter<SalesReportAdapter.SaleViewHolder> {
    private List<Map<String, Object>> salesList;

    public SalesReportAdapter() {
        this.salesList = new ArrayList<>();
    }

    public void setSalesList(List<Map<String, Object>> salesList) {
        this.salesList = salesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sale_report, parent, false);
        return new SaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        Map<String, Object> sale = salesList.get(position);
        holder.bind(sale);
    }

    @Override
    public int getItemCount() {
        return salesList.size();
    }

    static class SaleViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBranch, tvItems, tvAmount;

        public SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBranch = itemView.findViewById(R.id.tvBranch);
            tvItems = itemView.findViewById(R.id.tvItems);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }

        public void bind(Map<String, Object> sale) {
            String branch = (String) sale.get("branch");
            Double amount = (Double) sale.get("totalAmount");

            tvBranch.setText("Branch: " + branch);
            tvAmount.setText(String.format("Amount: KSh %.2f", amount));

            StringBuilder items = new StringBuilder("Items: ");
            if (sale.containsKey("Coke")) {
                items.append("Coke(").append(sale.get("Coke")).append(") ");
            }
            if (sale.containsKey("Fanta")) {
                items.append("Fanta(").append(sale.get("Fanta")).append(") ");
            }
            if (sale.containsKey("Sprite")) {
                items.append("Sprite(").append(sale.get("Sprite")).append(")");
            }

            tvItems.setText(items.toString());
        }
    }
}
