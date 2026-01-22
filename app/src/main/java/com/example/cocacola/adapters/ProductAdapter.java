package com.example.cocacola.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cocacola.R;
import com.example.cocacola.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private OnQuantityChangeListener listener;

    public interface OnQuantityChangeListener {
        void onQuantityChanged();
    }

    public ProductAdapter(List<Product> productList, OnQuantityChangeListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProductName, tvPrice, tvQuantity;
        private Button btnMinus, btnPlus;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvPrice.setText(String.format("KSh %.2f", product.getPrice()));
            tvQuantity.setText(String.valueOf(product.getQuantity()));

            btnPlus.setOnClickListener(v -> {
                product.setQuantity(product.getQuantity() + 1);
                tvQuantity.setText(String.valueOf(product.getQuantity()));
                listener.onQuantityChanged();
            });

            btnMinus.setOnClickListener(v -> {
                if (product.getQuantity() > 0) {
                    product.setQuantity(product.getQuantity() - 1);
                    tvQuantity.setText(String.valueOf(product.getQuantity()));
                    listener.onQuantityChanged();
                }
            });
        }
    }
}
