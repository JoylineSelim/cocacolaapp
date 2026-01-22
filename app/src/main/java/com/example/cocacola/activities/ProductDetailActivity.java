package com.example.cocacola.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.example.cocacola.R;
import com.example.cocacola.models.CartItem;
import com.example.cocacola.utils.CartManager;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView ivProductImage, ivStockIcon;
    private TextView tvProductName, tvPrice, tvStockStatus, tvStockLevel, tvQuantity, tvVolume, tvSku;
    private MaterialButton btnAddToCart;
    private ImageButton btnMinus, btnPlus;
    private Toolbar toolbar;

    private String productId, productName, branch;
    private double price;
    private int stock, quantity = 1;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        cartManager = CartManager.getInstance();

        // Get data from intent
        productId = getIntent().getStringExtra("productId");
        productName = getIntent().getStringExtra("productName");
        price = getIntent().getDoubleExtra("price", 0);
        stock = getIntent().getIntExtra("stock", 0);
        branch = getIntent().getStringExtra("branch");

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        ivProductImage = findViewById(R.id.ivProductImage);
        ivStockIcon = findViewById(R.id.ivStockIcon);
        tvProductName = findViewById(R.id.tvProductName);
        tvPrice = findViewById(R.id.tvPrice);
        tvStockStatus = findViewById(R.id.tvStockStatus);
        tvStockLevel = findViewById(R.id.tvStockLevel);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvVolume = findViewById(R.id.tvVolume);
        tvSku = findViewById(R.id.tvSku);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Set product data
        tvProductName.setText(productName);
        tvPrice.setText(String.format("KSh %.2f", price));
        tvStockLevel.setText(stock + " units available");

        // Set product-specific data
        setProductSpecificData();
        updateStockStatus();
        setProductImage();

        // Handle quantity controls
        btnPlus.setOnClickListener(v -> {
            if (quantity < stock) {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this, "Maximum stock reached", Toast.LENGTH_SHORT).show();
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            if (stock > 0) {
                CartItem cartItem = new CartItem(productId, productName, price, quantity, stock);
                cartManager.addItem(cartItem);
                Toast.makeText(this, "Added " + quantity + " item(s) to cart", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Product out of stock", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setProductSpecificData() {
        String sku = "CC-500ML-001";
        String volume = "500ml";

        if (productName.toLowerCase().contains("fanta")) {
            sku = "FT-500ML-002";
            volume = "500ml";
        } else if (productName.toLowerCase().contains("sprite")) {
            sku = "SP-500ML-003";
            volume = "500ml";
        }

        tvVolume.setText(volume);
        tvSku.setText(sku);
    }

    private void updateStockStatus() {
        if (stock == 0) {
            tvStockStatus.setText("Out of Stock");
            tvStockStatus.setTextColor(getResources().getColor(R.color.status_out_of_stock));
            ivStockIcon.setColorFilter(getResources().getColor(R.color.status_out_of_stock));
            btnAddToCart.setEnabled(false);
            btnAddToCart.setAlpha(0.5f);
        } else if (stock <= 10) {
            tvStockStatus.setText("Low Stock");
            tvStockStatus.setTextColor(getResources().getColor(R.color.status_low_stock));
            ivStockIcon.setColorFilter(getResources().getColor(R.color.status_low_stock));
        } else {
            tvStockStatus.setText("In Stock");
            tvStockStatus.setTextColor(getResources().getColor(R.color.status_in_stock));
            ivStockIcon.setColorFilter(getResources().getColor(R.color.status_in_stock));
        }
    }

    private void setProductImage() {
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
