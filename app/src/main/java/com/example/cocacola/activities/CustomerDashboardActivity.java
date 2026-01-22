package com.example.cocacola.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.cocacola.R;

public class CustomerDashboardActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private AutoCompleteTextView spinnerBranch;
    private MaterialButton btnStartShopping, btnLogout;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvWelcome = findViewById(R.id.tvWelcome);
        spinnerBranch = findViewById(R.id.spinnerBranch);
        btnStartShopping = findViewById(R.id.btnStartShopping);
        btnLogout = findViewById(R.id.btnLogout);

        loadUserData();
        setupBranchSpinner();

        btnStartShopping.setOnClickListener(v -> {
            String selectedBranch = spinnerBranch.getText().toString();
            if (selectedBranch.isEmpty()) {
                spinnerBranch.setError("Please select a branch");
                return;
            }
            saveSelectedBranch(selectedBranch);

            Intent intent = new Intent(CustomerDashboardActivity.this, ShoppingActivity.class);
            intent.putExtra("branch", selectedBranch);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(CustomerDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String name = task.getResult().child("name").getValue(String.class);
                        tvWelcome.setText("Welcome, " + name + "!");
                    }
                });
    }

    private void setupBranchSpinner() {
        String[] branches = {"Nairobi", "Kisumu", "Mombasa", "Nakuru", "Eldoret"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, branches);
        spinnerBranch.setAdapter(adapter);
    }

    private void saveSelectedBranch(String branch) {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).child("selectedBranch").setValue(branch);
    }
}
