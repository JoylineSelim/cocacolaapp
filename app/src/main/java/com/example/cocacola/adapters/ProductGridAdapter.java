package com.example.cocacola.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.example.cocacola.R;
import com.example.cocacola.activities.ProductDetailActivity;
import com.example.cocacola.models.CartItem;
import com.example.cocacola.models.Product;
import com.example.cocacola.utils.CartManager;
import java.util.List;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;
    private String branch;
    private CartManager cartManager;

    public ProductGridAdapter(Context context, List<Product> productList, String branch) {
        this.context = context;
        this.productList = productList;
        this.branch = branch;
        this.cartManager = CartManager.getInstance();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_grid, parent, false);
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
        private ImageView ivProductImage;
        private TextView tvProductName, tvPrice, tvStockLevel, tvStockBadge, tvQuantity;
        private MaterialButton btnAddToCart;
        private LinearLayout quantityControls;
        private ImageButton btnMinus, btnPlus;
        private FrameLayout soldOutOverlay;
        private CardView cardView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStockLevel = itemView.findViewById(R.id.tvStockLevel);
            tvStockBadge = itemView.findViewById(R.id.tvStockBadge);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            quantityControls = itemView.findViewById(R.id.quantityControls);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            soldOutOverlay = itemView.findViewById(R.id.soldOutOverlay);
            cardView = (CardView) itemView;
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvPrice.setText(String.format("KSh %.2f", product.getPrice()));

            int availableStock = product.getQuantity();
            updateStockStatus(availableStock);

            // Set product image based on name
            setProductImage(product.getName());

            // Check if product is in cart
            updateCartButtons(product);

            // Click listeners
            cardView.setOnClickListener(v -> {
                if (availableStock > 0) {
                    Intent intent = new Intent(context, ProductDetailActivity.class);
                    intent.putExtra("productId", product.getProductId());
                    intent.putExtra("productName", product.getName());
                    intent.putExtra("price", product.getPrice());
                    intent.putExtra("stock", availableStock);
                    intent.putExtra("branch", branch);
                    context.startActivity(intent);
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                if (availableStock > 0) {
                    addToCart(product, 1);
                } else {
                    Toast.makeText(context, "Product out of stock", Toast.LENGTH_SHORT).show();
                }
            });

            btnPlus.setOnClickListener(v -> {
                CartItem cartItem = cartManager.getItem(product.getProductId());
                if (cartItem != null && cartItem.canAddMore()) {
                    cartManager.incrementItem(product.getProductId());
                    updateCartButtons(product);
                } else {
                    Toast.makeText(context, "Maximum stock reached", Toast.LENGTH_SHORT).show();
                }
            });

            btnMinus.setOnClickListener(v -> {
                cartManager.decrementItem(product.getProductId());
                updateCartButtons(product);
            });
        }

        private void updateStockStatus(int stock) {
            tvStockLevel.setText(stock + " units available");

            if (stock == 0) {
                // Sold out
                tvStockBadge.setText("Sold Out");
                tvStockBadge.setBackgroundResource(R.drawable.bg_stock_badge_out);
                tvStockBadge.setTextColor(Color.WHITE);
                soldOutOverlay.setVisibility(View.VISIBLE);
                btnAddToCart.setEnabled(false);
                btnAddToCart.setAlpha(0.5f);
            } else if (stock <= 10) {
                // Low stock
                tvStockBadge.setText("Low Stock");
                tvStockBadge.setBackgroundResource(R.drawable.bg_stock_badge_low);
                tvStockBadge.setTextColor(Color.WHITE);
                soldOutOverlay.setVisibility(View.GONE);
                btnAddToCart.setEnabled(true);
                btnAddToCart.setAlpha(1.0f);
            } else {
                // In stock
                tvStockBadge.setText("In Stock");
                tvStockBadge.setBackgroundResource(R.drawable.bg_stock_badge_in);
                tvStockBadge.setTextColor(Color.WHITE);
                soldOutOverlay.setVisibility(View.GONE);
                btnAddToCart.setEnabled(true);
                btnAddToCart.setAlpha(1.0f);
            }
        }

        private void updateCartButtons(Product product) {
            if (cartManager.containsProduct(product.getProductId())) {
                CartItem cartItem = cartManager.getItem(product.getProductId());
                btnAddToCart.setVisibility(View.GONE);
                quantityControls.setVisibility(View.VISIBLE);
                tvQuantity.setText(String.valueOf(cartItem.getQuantity()));
            } else {
                btnAddToCart.setVisibility(View.VISIBLE);
                quantityControls.setVisibility(View.GONE);
            }
        }

        private void addToCart(Product product, int quantity) {
            CartItem cartItem = new CartItem(
                    product.getProductId(),
                    product.getName(),
                    product.getPrice(),
                    quantity,
                    product.getQuantity() // available stock
            );

            cartManager.addItem(cartItem);
            updateCartButtons(product);
            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
        }

        private void setProductImage(String productName) {
            // Set product image based on name
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