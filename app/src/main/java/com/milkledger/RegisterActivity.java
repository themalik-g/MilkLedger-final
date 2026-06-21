package com.milkledger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etConfirmPassword, etSecurityQ, etSecurityA, etPin;
    private Button btnRegister, btnBackToLogin;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etSecurityQ = findViewById(R.id.etSecurityQ);
        etSecurityA = findViewById(R.id.etSecurityA);
        etPin = findViewById(R.id.etPin);
        btnRegister = findViewById(R.id.btnRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnRegister.setOnClickListener(v -> register());
        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void register() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();
        String securityQ = etSecurityQ.getText().toString().trim();
        String securityA = etSecurityA.getText().toString().trim();
        String pin = etPin.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty() || securityQ.isEmpty() || securityA.isEmpty()) {
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 4) {
            etPassword.setError("Min 4 characters");
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Passwords don't match");
            return;
        }

        boolean usePin = !pin.isEmpty() && pin.length() >= 4;
        long id = db.createUser(username, password, securityQ, securityA, usePin ? pin : "", usePin);
        if (id > 0) {
            Toast.makeText(this, "Account created! Please login.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
        }
    }
}
