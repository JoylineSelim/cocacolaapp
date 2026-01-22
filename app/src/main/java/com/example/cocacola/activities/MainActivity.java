package com.example.cocacola.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.cocacola.R;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity onCreate started");

        mAuth = FirebaseAuth.getInstance();

        // Initialize buttons
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Check if buttons were found
        if (btnLogin == null) {
            Log.e(TAG, "btnLogin is NULL! Check your layout file.");
            Toast.makeText(this, "Error: Login button not found", Toast.LENGTH_LONG).show();
            return;
        }

        if (btnRegister == null) {
            Log.e(TAG, "btnRegister is NULL! Check your layout file.");
            Toast.makeText(this, "Error: Register button not found", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "Buttons initialized successfully");

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "User already logged in: " + currentUser.getEmail());
            checkUserRoleAndNavigate(currentUser.getUid());
        }

        // Set click listeners
        btnLogin.setOnClickListener(v -> {
            Log.d(TAG, "Login button clicked");
            Toast.makeText(MainActivity.this, "Opening Login...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Log.d(TAG, "Register button clicked");
            Toast.makeText(MainActivity.this, "Opening Register...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        Log.d(TAG, "MainActivity onCreate completed");
    }

    private void checkUserRoleAndNavigate(String userId) {
        // This will be implemented in LoginActivity
        Log.d(TAG, "Checking user role for: " + userId);
    }
}
