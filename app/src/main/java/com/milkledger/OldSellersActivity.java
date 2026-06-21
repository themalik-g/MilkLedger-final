package com.milkledger;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OldSellersActivity extends AppCompatActivity {
    private RecyclerView rvOldSellers;
    private Button btnBack;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private long sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_sellers);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("MilkLedgerPrefs", MODE_PRIVATE);
        sessionId = prefs.getLong("current_session_id", -1);

        rvOldSellers = findViewById(R.id.rvOldSellers);
        btnBack = findViewById(R.id.btnBack);

        rvOldSellers.setLayoutManager(new LinearLayoutManager(this));
        loadInactiveSellers();

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadInactiveSellers() {
        new Thread(() -> {
            List<Seller> allSellers = db.getAllSellers(sessionId, false);
            List<Seller> inactive = new ArrayList<>();
            Map<Long, Double> balances = new HashMap<>();
            for (Seller s : allSellers) {
                if (!s.isActive()) {
                    inactive.add(s);
                    balances.put(s.getId(), db.getSellerBalance(s.getId()));
                }
            }
            runOnUiThread(() -> {
                SellerAdapter adapter = new SellerAdapter(inactive, balances, new SellerAdapter.OnSellerActionListener() {
                    @Override
                    public void onEdit(Seller seller) {
                        // No edit for inactive sellers from here
                    }

                    @Override
                    public void onToggleActive(Seller seller) {
                        db.setSellerActive(seller.getId(), true);
                        Toast.makeText(OldSellersActivity.this, "Seller reactivated", Toast.LENGTH_SHORT).show();
                        loadInactiveSellers();
                    }

                    @Override
                    public void onViewLedger(Seller seller) {
                        // View ledger
                    }
                });
                rvOldSellers.setAdapter(adapter);
            });
        }).start();
    }
}
