package com.milkledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {
    private TextView tvSessionInfo, tvUsername;
    private Button btnDailyEntry, btnSellersList, btnReports, btnCashPaid, btnCashReceived,
            btnEditingCorrection, btnBackupRestore, btnOldSellers, btnSettings;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private long sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("MilkLedgerPrefs", MODE_PRIVATE);
        sessionId = prefs.getLong("current_session_id", -1);

        if (sessionId == -1) {
            startActivity(new Intent(this, SessionSetupActivity.class));
            finish();
            return;
        }

        tvSessionInfo = findViewById(R.id.tvSessionInfo);
        tvUsername = findViewById(R.id.tvUsername);
        btnDailyEntry = findViewById(R.id.btnDailyEntry);
        btnSellersList = findViewById(R.id.btnSellersList);
        btnReports = findViewById(R.id.btnReports);
        btnCashPaid = findViewById(R.id.btnCashPaid);
        btnCashReceived = findViewById(R.id.btnCashReceived);
        btnEditingCorrection = findViewById(R.id.btnEditingCorrection);
        btnBackupRestore = findViewById(R.id.btnBackupRestore);
        btnOldSellers = findViewById(R.id.btnOldSellers);
        btnSettings = findViewById(R.id.btnSettings);

        Session session = db.getSession(sessionId);
        if (session != null) {
            tvSessionInfo.setText(session.getName() + " - " + session.getYear());
        }
        tvUsername.setText("Welcome, " + db.getUsername());

        btnDailyEntry.setOnClickListener(v -> startActivity(new Intent(this, DailyEntryActivity.class)));
        btnSellersList.setOnClickListener(v -> startActivity(new Intent(this, SellersListActivity.class)));
        btnReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsMenuActivity.class)));
        btnCashPaid.setOnClickListener(v -> {
            Intent i = new Intent(this, CashTransactionActivity.class);
            i.putExtra("type", "paid");
            startActivity(i);
        });
        btnCashReceived.setOnClickListener(v -> {
            Intent i = new Intent(this, CashTransactionActivity.class);
            i.putExtra("type", "received");
            startActivity(i);
        });
        btnEditingCorrection.setOnClickListener(v -> startActivity(new Intent(this, EditingCorrectionActivity.class)));
        btnBackupRestore.setOnClickListener(v -> startActivity(new Intent(this, BackupRestoreActivity.class)));
        btnOldSellers.setOnClickListener(v -> startActivity(new Intent(this, OldSellersActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", (d, w) -> {
                        prefs.edit().remove("current_session_id").apply();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
