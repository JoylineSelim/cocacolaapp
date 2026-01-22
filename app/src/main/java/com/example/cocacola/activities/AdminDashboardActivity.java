package com.example.cocacola.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.cocacola.R;

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private MaterialButton btnViewReport, btnRestock, btnLogout;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvWelcome = findViewById(R.id.tvWelcome);
        btnViewReport = findViewById(R.id.btnViewReport);
        btnRestock = findViewById(R.id.btnRestock);
        btnLogout = findViewById(R.id.btnLogout);

        loadAdminData();

        btnViewReport.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, SalesReportActivity.class);
            startActivity(intent);
        });

        btnRestock.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, RestockActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadAdminData() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String name = task.getResult().child("name").getValue(String.class);
                        tvWelcome.setText("Welcome Admin, " + name + "!");
                    }
                });
    }
}