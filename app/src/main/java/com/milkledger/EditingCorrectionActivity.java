package com.milkledger;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditingCorrectionActivity extends AppCompatActivity {
    private TextView tvDate;
    private Button btnPickDate, btnRefresh;
    private LinearLayout layoutEntries, layoutCash;
    private ProgressBar progressBar;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private long sessionId;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editing_correction);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("MilkLedgerPrefs", MODE_PRIVATE);
        sessionId = prefs.getLong("current_session_id", -1);

        tvDate = findViewById(R.id.tvDate);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnRefresh = findViewById(R.id.btnRefresh);
        layoutEntries = findViewById(R.id.layoutEntries);
        layoutCash = findViewById(R.id.layoutCash);
        progressBar = findViewById(R.id.progressBar);

        currentDate = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        tvDate.setText(currentDate);

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnRefresh.setOnClickListener(v -> loadData());

        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEntries.removeAllViews();
        layoutCash.removeAllViews();

        new Thread(() -> {
            List<Entry> entries = db.getEntriesForDate(currentDate, sessionId);
            List<CashTransaction> cashList = db.getCashTransactionsForDate(currentDate, sessionId);

            runOnUiThread(() -> {
                // Entries section
                TextView tvEntryHeader = new TextView(this);
                tvEntryHeader.setText("Milk Entries");
                tvEntryHeader.setTextSize(18);
                tvEntryHeader.setPadding(0, 16, 0, 8);
                layoutEntries.addView(tvEntryHeader);

                if (entries.isEmpty()) {
                    TextView tv = new TextView(this);
                    tv.setText("No entries for this date");
                    layoutEntries.addView(tv);
                } else {
                    for (Entry e : entries) {
                        View row = LayoutInflater.from(this).inflate(R.layout.item_edit_entry, layoutEntries, false);
                        TextView tvInfo = row.findViewById(R.id.tvInfo);
                        Button btnEdit = row.findViewById(R.id.btnEdit);
                        Button btnDelete = row.findViewById(R.id.btnDelete);

                        tvInfo.setText(String.format(Locale.getDefault(), "%s - %s: %.2fL @ %.2f = %.2f (%s)",
                                e.getSellerNumber(), e.getSellerName(), e.getLiters(), e.getRate(), e.getAmount(), e.getTimeOfDay()));

                        btnEdit.setOnClickListener(v -> showEditEntryDialog(e));
                        btnDelete.setOnClickListener(v -> {
                            new AlertDialog.Builder(this)
                                    .setTitle("Delete Entry?")
                                    .setMessage("This cannot be undone.")
                                    .setPositiveButton("Delete", (dialog, which) -> {
                                        db.deleteEntry(e.getId());
                                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                        loadData();
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        });

                        layoutEntries.addView(row);
                    }
                }

                // Cash section
                TextView tvCashHeader = new TextView(this);
                tvCashHeader.setText("Cash Transactions");
                tvCashHeader.setTextSize(18);
                tvCashHeader.setPadding(0, 16, 0, 8);
                layoutCash.addView(tvCashHeader);

                if (cashList.isEmpty()) {
                    TextView tv = new TextView(this);
                    tv.setText("No cash transactions for this date");
                    layoutCash.addView(tv);
                } else {
                    for (CashTransaction c : cashList) {
                        View row = LayoutInflater.from(this).inflate(R.layout.item_edit_entry, layoutCash, false);
                        TextView tvInfo = row.findViewById(R.id.tvInfo);
                        Button btnEdit = row.findViewById(R.id.btnEdit);
                        Button btnDelete = row.findViewById(R.id.btnDelete);

                        tvInfo.setText(String.format(Locale.getDefault(), "%s - %s: %.2f (%s) %s",
                                c.getDate(), c.getSellerName(), c.getAmount(), c.getType(), c.getNotes()));

                        btnEdit.setOnClickListener(v -> showEditCashDialog(c));
                        btnDelete.setOnClickListener(v -> {
                            new AlertDialog.Builder(this)
                                    .setTitle("Delete Transaction?")
                                    .setMessage("This cannot be undone.")
                                    .setPositiveButton("Delete", (dialog, which) -> {
                                        db.deleteCashTransaction(c.getId());
                                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                                        loadData();
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        });

                        layoutCash.addView(row);
                    }
                }

                progressBar.setVisibility(View.GONE);
            });
        }).start();
    }

    private void showEditEntryDialog(Entry entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Entry");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_entry, null);
        EditText etLiters = view.findViewById(R.id.etLiters);
        EditText etRate = view.findViewById(R.id.etRate);
        EditText etTime = view.findViewById(R.id.etTime);

        etLiters.setText(String.valueOf(entry.getLiters()));
        etRate.setText(String.valueOf(entry.getRate()));
        etTime.setText(entry.getTimeOfDay());

        builder.setView(view);
        builder.setPositiveButton("Save", (d, w) -> {
            String litersStr = etLiters.getText().toString().replace(",", ".");
            String rateStr = etRate.getText().toString().replace(",", ".");
            try {
                double liters = Double.parseDouble(litersStr);
                double rate = Double.parseDouble(rateStr);
                double amount = liters * rate;
                db.updateEntry(entry.getId(), liters, rate, amount, etTime.getText().toString().trim());
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                loadData();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid numbers", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditCashDialog(CashTransaction cash) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Cash Transaction");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_cash, null);
        EditText etAmount = view.findViewById(R.id.etAmount);
        EditText etNotes = view.findViewById(R.id.etNotes);

        etAmount.setText(String.valueOf(cash.getAmount()));
        etNotes.setText(cash.getNotes());

        builder.setView(view);
        builder.setPositiveButton("Save", (d, w) -> {
            String amountStr = etAmount.getText().toString().replace(",", ".");
            try {
                double amount = Double.parseDouble(amountStr);
                db.updateCashTransaction(cash.getId(), amount, etNotes.getText().toString().trim());
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                loadData();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            currentDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvDate.setText(currentDate);
            loadData();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
}
