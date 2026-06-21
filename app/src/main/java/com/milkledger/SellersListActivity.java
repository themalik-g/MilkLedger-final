package com.milkledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellersListActivity extends AppCompatActivity {
    private EditText etSearch;
    private RecyclerView rvSellers;
    private Button btnAddSeller, btnShowInactive;
    private ProgressBar progressBar;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private long sessionId;
    private SellerAdapter adapter;
    private boolean showingInactive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sellers_list);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("MilkLedgerPrefs", MODE_PRIVATE);
        sessionId = prefs.getLong("current_session_id", -1);

        etSearch = findViewById(R.id.etSearch);
        rvSellers = findViewById(R.id.rvSellers);
        btnAddSeller = findViewById(R.id.btnAddSeller);
        btnShowInactive = findViewById(R.id.btnShowInactive);
        progressBar = findViewById(R.id.progressBar);

        rvSellers.setLayoutManager(new LinearLayoutManager(this));
        loadSellers();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchSellers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAddSeller.setOnClickListener(v -> startActivity(new Intent(SellersListActivity.this, CreateSellerActivity.class)));
        btnShowInactive.setOnClickListener(v -> {
            showingInactive = !showingInactive;
            btnShowInactive.setText(showingInactive ? "Show Active" : "Show Inactive");
            loadSellers();
        });
    }

    private void loadSellers() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            List<Seller> list = db.getAllSellers(sessionId, !showingInactive);
            Map<Long, Double> balances = new HashMap<>();
            for (Seller s : list) {
                balances.put(s.getId(), db.getSellerBalance(s.getId()));
            }
            runOnUiThread(() -> {
                adapter = new SellerAdapter(list, balances, new SellerAdapter.OnSellerActionListener() {
                    @Override
                    public void onEdit(Seller seller) {
                        showEditDialog(seller);
                    }
                    @Override
                    public void onToggleActive(Seller seller) {
                        db.setSellerActive(seller.getId(), !seller.isActive());
                        Toast.makeText(SellersListActivity.this,
                                seller.isActive() ? "Seller deactivated" : "Seller activated",
                                Toast.LENGTH_SHORT).show();
                        loadSellers();
                    }
                    @Override
                    public void onViewLedger(Seller seller) {
                        Intent i = new Intent(SellersListActivity.this, SellerLedgerActivity.class);
                        i.putExtra("seller_id", seller.getId());
                        startActivity(i);
                    }
                });
                rvSellers.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            });
        }).start();
    }

    private void searchSellers(String query) {
        if (query.isEmpty()) {
            loadSellers();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            List<Seller> list = db.searchSellers(sessionId, query);
            Map<Long, Double> balances = new HashMap<>();
            for (Seller s : list) {
                balances.put(s.getId(), db.getSellerBalance(s.getId()));
            }
            runOnUiThread(() -> {
                adapter = new SellerAdapter(list, balances, new SellerAdapter.OnSellerActionListener() {
                    @Override public void onEdit(Seller seller) { showEditDialog(seller); }
                    @Override public void onToggleActive(Seller seller) {
                        db.setSellerActive(seller.getId(), !seller.isActive());
                        loadSellers();
                    }
                    @Override public void onViewLedger(Seller seller) {
                        Intent i = new Intent(SellersListActivity.this, SellerLedgerActivity.class);
                        i.putExtra("seller_id", seller.getId());
                        startActivity(i);
                    }
                });
                rvSellers.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            });
        }).start();
    }

    private void showEditDialog(Seller seller) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Seller");

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_seller, null);
        EditText etName = view.findViewById(R.id.etEditName);
        EditText etContact = view.findViewById(R.id.etEditContact);
        EditText etAddress = view.findViewById(R.id.etEditAddress);
        EditText etNotes = view.findViewById(R.id.etEditNotes);

        etName.setText(seller.getName());
        etContact.setText(seller.getContact());
        etAddress.setText(seller.getAddress());
        etNotes.setText(seller.getNotes());

        builder.setView(view);
        builder.setPositiveButton("Save", (d, w) -> {
            db.updateSeller(seller.getId(),
                    etName.getText().toString().trim(),
                    etContact.getText().toString().trim(),
                    etAddress.getText().toString().trim(),
                    etNotes.getText().toString().trim());
            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
            loadSellers();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSellers();
    }
}
