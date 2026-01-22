package com.example.cocacola.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cocacola.R;
import com.example.cocacola.models.CartItem;
import com.example.cocacola.utils.CartManager;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<CartItem> cartItems;
    private CartManager cartManager;

    public CartAdapter(Context context) {
        this.context = context;
        this.cartItems = new ArrayList<>();
        this.cartManager = CartManager.getInstance();
    }

    public void setCartItems(List<CartItem> items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName, tvPrice, tvSubtotal, tvQuantity;
        private ImageButton btnRemove, btnMinus, btnPlus;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }

        public void bind(CartItem item) {
            tvProductName.setText(item.getProductName());
            tvPrice.setText(String.format("KSh %.2f", item.getPrice()));
            tvSubtotal.setText(String.format("Subtotal: KSh %.2f", item.getSubtotal()));
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            setProductImage(item.getProductName());

            btnRemove.setOnClickListener(v -> {
                cartManager.removeItem(item.getProductId());
            });

            btnPlus.setOnClickListener(v -> {
                if (item.canAddMore()) {
                    cartManager.incrementItem(item.getProductId());
                }
            });

            btnMinus.setOnClickListener(v -> {
                cartManager.decrementItem(item.getProductId());
            });
        }

        private void setProductImage(String productName) {
            int imageResource = R.drawable.placeholder_product;

            if (productName.toLowerCase().contains("coke") || productName.toLowerCase().contains("cola")) {
                imageResource = R.drawable.product_coke;
            } else if (productName.toLowerCase().contains("fanta")) {
                imageResource = R.drawable.product_fanta;
            } else if (productName.toLowerCase().contains("sprite")) {
                imageResource = R.drawable.product_sprite;
            }

            ivProductImage.setImageResource(imageResource);
        }
    }
}