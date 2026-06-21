package com.milkledger;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CashTransactionActivity extends AppCompatActivity {
    private TextView tvTitle, tvDate;
    private Spinner spinnerSeller;
    private EditText etAmount, etNotes;
    private Button btnSave, btnPickDate;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private long sessionId;
    private String type;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_transaction);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("MilkLedgerPrefs", MODE_PRIVATE);
        sessionId = prefs.getLong("current_session_id", -1);
        type = getIntent().getStringExtra("type");

        tvTitle = findViewById(R.id.tvTitle);
        tvDate = findViewById(R.id.tvDate);
        spinnerSeller = findViewById(R.id.spinnerSeller);
        etAmount = findViewById(R.id.etAmount);
        etNotes = findViewById(R.id.etNotes);
        btnSave = findViewById(R.id.btnSave);
        btnPickDate = findViewById(R.id.btnPickDate);

        tvTitle.setText(type.equals("paid") ? "Cash Payment (You Pay)" : "Cash Received");
        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        tvDate.setText(currentDate);

        loadSellers();
        btnSave.setOnClickListener(v -> saveTransaction());
        btnPickDate.setOnClickListener(v -> showDatePicker());
    }

    private void loadSellers() {
        List<Seller> sellers = db.getAllSellers(sessionId, true);
        ArrayAdapter<Seller> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sellers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeller.setAdapter(adapter);
    }

    private void saveTransaction() {
        Seller s = (Seller) spinnerSeller.getSelectedItem();
        if (s == null) {
            Toast.makeText(this, "Select a seller", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = etAmount.getText().toString().replace(",", ".");
        if (amountStr.isEmpty()) {
            etAmount.setError("Enter amount");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            return;
        }

        String notes = etNotes.getText().toString().trim();

        db.addCashTransaction(currentDate, s.getId(), amount, notes, type, sessionId);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            currentDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvDate.setText(currentDate);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
}
