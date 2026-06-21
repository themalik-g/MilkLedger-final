package com.milkledger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateSellerActivity extends AppCompatActivity {
    private EditText etName, etContact, etAddress, etNotes;
    private Button btnSave;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private long sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_seller);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("MilkLedgerPrefs", MODE_PRIVATE);
        sessionId = prefs.getLong("current_session_id", -1);

        etName = findViewById(R.id.etName);
        etContact = findViewById(R.id.etContact);
        etAddress = findViewById(R.id.etAddress);
        etNotes = findViewById(R.id.etNotes);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveSeller());
    }

    private void saveSeller() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Enter seller name");
            return;
        }
        String contact = etContact.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        int nextNum = db.getNextSellerNumber(sessionId);
        String sellerNum = String.format("S%04d", nextNum);

        long id = db.createSeller(sellerNum, name, contact, address, notes, sessionId);
        if (id > 0) {
            Toast.makeText(this, "Seller " + sellerNum + " created", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
