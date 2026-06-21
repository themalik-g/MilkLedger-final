package com.milkledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etPin;
    private Button btnLogin, btnRegister, btnUsePin, btnUsePassword, btnForgotPassword;
    private LinearLayout layoutPassword, layoutPin;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private boolean pinMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("MilkLedgerPrefs", MODE_PRIVATE);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etPin = findViewById(R.id.etPin);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnUsePin = findViewById(R.id.btnUsePin);
        btnUsePassword = findViewById(R.id.btnUsePassword);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutPin = findViewById(R.id.layoutPin);

        // Check if user exists
        if (!db.doesUserExist()) {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
            return;
        }

        // Auto-fill username
        String savedUser = prefs.getString("last_username", "");
        etUsername.setText(savedUser);

        // Check if PIN is enabled
        if (db.usesPin() && !savedUser.isEmpty()) {
            switchToPinMode();
        }

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
        btnUsePin.setOnClickListener(v -> switchToPinMode());
        btnUsePassword.setOnClickListener(v -> switchToPasswordMode());
        btnForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void switchToPinMode() {
        pinMode = true;
        layoutPassword.setVisibility(View.GONE);
        layoutPin.setVisibility(View.VISIBLE);
        btnUsePin.setVisibility(View.GONE);
        btnUsePassword.setVisibility(View.VISIBLE);
        etPin.requestFocus();
    }

    private void switchToPasswordMode() {
        pinMode = false;
        layoutPassword.setVisibility(View.VISIBLE);
        layoutPin.setVisibility(View.GONE);
        btnUsePin.setVisibility(View.VISIBLE);
        btnUsePassword.setVisibility(View.GONE);
        etPassword.requestFocus();
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        if (username.isEmpty()) {
            etUsername.setError("Enter username");
            return;
        }

        boolean valid;
        if (pinMode) {
            String pin = etPin.getText().toString().trim();
            if (pin.isEmpty()) {
                etPin.setError("Enter PIN");
                return;
            }
            valid = db.validatePin(username, pin);
        } else {
            String password = etPassword.getText().toString().trim();
            if (password.isEmpty()) {
                etPassword.setError("Enter password");
                return;
            }
            valid = db.validateUser(username, password);
        }

        if (valid) {
            prefs.edit().putString("last_username", username).apply();
            startActivity(new Intent(this, SessionSetupActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private void showForgotPasswordDialog() {
        final String username = etUsername.getText().toString().trim();
        if (username.isEmpty()) {
            etUsername.setError("Enter username first");
            return;
        }

        String question = db.getSecurityQuestion(username);
        if (question == null || question.isEmpty()) {
            Toast.makeText(this, "No security question set", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView tvQ = new TextView(this);
        tvQ.setText(question);
        tvQ.setPadding(0, 0, 0, 20);
        layout.addView(tvQ);

        EditText etAnswer = new EditText(this);
        etAnswer.setHint("Your answer");
        layout.addView(etAnswer);

        EditText etNewPass = new EditText(this);
        etNewPass.setHint("New password (min 4 chars)");
        layout.addView(etNewPass);

        builder.setView(layout);
        builder.setPositiveButton("Reset", (dialog, which) -> {
            String answer = etAnswer.getText().toString().trim();
            String newPass = etNewPass.getText().toString().trim();
            if (answer.isEmpty() || newPass.length() < 4) {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
                return;
            }
            if (db.checkSecurityAnswer(username, answer)) {
                db.resetPassword(username, newPass);
                Toast.makeText(this, "Password reset successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Wrong answer", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
