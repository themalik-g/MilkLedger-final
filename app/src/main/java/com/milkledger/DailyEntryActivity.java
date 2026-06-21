package com.milkledger;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DailyEntryActivity extends AppCompatActivity {
    private TextView tvDate, tvSessionInfo;
    private Spinner spinnerTime;
    private RadioGroup rgEntryMode;
    private RadioButton rbOneByOne, rbBulk;
    private LinearLayout layoutOneByOne, layoutBulk;
    private EditText etLiters, etRate;
    private TextView tvAmount, tvSellerInfo;
    private Button btnSaveNext, btnSkip, btnSameAsYesterday, btnPickDate;
    private ScrollView scrollView;
    private ProgressBar progressBar;

    private DatabaseHelper db;
    private SharedPreferences prefs;
    private long sessionId;
    private String currentDate;
    private String currentTimeOfDay = "morning";
    private List<Seller> allSellers;
    private int currentSellerIndex = 0;
    private boolean bulkMode = false;
    private boolean dataModified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_entry);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("MilkLedgerPrefs", MODE_PRIVATE);
        sessionId = prefs.getLong("current_session_id", -1);

        initViews();
        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        tvDate.setText(currentDate);

        loadSellers();
        setupListeners();
        showCurrentSeller();
    }

    private void initViews() {
        tvDate = findViewById(R.id.tvDate);
        tvSessionInfo = findViewById(R.id.tvSessionInfo);
        spinnerTime = findViewById(R.id.spinnerTime);
        rgEntryMode = findViewById(R.id.rgEntryMode);
        rbOneByOne = findViewById(R.id.rbOneByOne);
        rbBulk = findViewById(R.id.rbBulk);
        layoutOneByOne = findViewById(R.id.layoutOneByOne);
        layoutBulk = findViewById(R.id.layoutBulk);
        etLiters = findViewById(R.id.etLiters);
        etRate = findViewById(R.id.etRate);
        tvAmount = findViewById(R.id.tvAmount);
        tvSellerInfo = findViewById(R.id.tvSellerInfo);
        btnSaveNext = findViewById(R.id.btnSaveNext);
        btnSkip = findViewById(R.id.btnSkip);
        btnSameAsYesterday = findViewById(R.id.btnSameAsYesterday);
        btnPickDate = findViewById(R.id.btnPickDate);
        scrollView = findViewById(R.id.scrollView);
        progressBar = findViewById(R.id.progressBar);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Morning", "Evening"});
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(timeAdapter);

        // BUG FIX: Select all on focus for easy replacement
        etLiters.setSelectAllOnFocus(true);
        etRate.setSelectAllOnFocus(true);
    }

    private void setupListeners() {
        spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTimeOfDay = position == 0 ? "morning" : "evening";
                if (!bulkMode) showCurrentSeller();
                else setupBulkMode();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        rgEntryMode.setOnCheckedChangeListener((group, checkedId) -> {
            bulkMode = checkedId == R.id.rbBulk;
            layoutOneByOne.setVisibility(bulkMode ? View.GONE : View.VISIBLE);
            layoutBulk.setVisibility(bulkMode ? View.VISIBLE : View.GONE);
            if (bulkMode) setupBulkMode();
            else showCurrentSeller();
        });

        TextWatcher calcWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { calculateAmount(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        etLiters.addTextChangedListener(calcWatcher);
        etRate.addTextChangedListener(calcWatcher);

        etLiters.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                etRate.requestFocus();
                return true;
            }
            return false;
        });

        btnSaveNext.setOnClickListener(v -> saveAndNext());
        btnSkip.setOnClickListener(v -> skipSeller());
        btnSameAsYesterday.setOnClickListener(v -> fillSameAsYesterday());
        btnPickDate.setOnClickListener(v -> showDatePicker());
    }

    private void loadSellers() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            allSellers = db.getAllSellers(sessionId, true);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (allSellers.isEmpty()) {
                    Toast.makeText(this, "No sellers found. Create sellers first.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }).start();
    }

    private void showCurrentSeller() {
        if (allSellers == null || allSellers.isEmpty() || currentSellerIndex >= allSellers.size()) return;

        Seller s = allSellers.get(currentSellerIndex);
        tvSellerInfo.setText(s.getNumber() + " - " + s.getName());

        // Check if entry already exists for this date/time
        List<Entry> existing = db.getEntriesForDate(currentDate, sessionId);
        boolean found = false;
        for (Entry e : existing) {
            if (e.getSellerId() == s.getId() && e.getTimeOfDay().equals(currentTimeOfDay)) {
                etLiters.setText(String.valueOf(e.getLiters()));
                etRate.setText(String.valueOf(e.getRate()));
                calculateAmount();
                found = true;
                break;
            }
        }
        if (!found) {
            etLiters.setText("");
            etRate.setText("");
            tvAmount.setText("Amount: 0.00");
        }
        etLiters.requestFocus();
    }

    private void calculateAmount() {
        try {
            String litersStr = etLiters.getText().toString().replace(",", ".");
            String rateStr = etRate.getText().toString().replace(",", ".");
            double liters = litersStr.isEmpty() ? 0 : Double.parseDouble(litersStr);
            double rate = rateStr.isEmpty() ? 0 : Double.parseDouble(rateStr);
            double amount = liters * rate;
            tvAmount.setText(String.format(Locale.getDefault(), "Amount: %.2f", amount));
        } catch (NumberFormatException e) {
            tvAmount.setText("Amount: 0.00");
        }
    }

    private void saveAndNext() {
        if (allSellers == null || currentSellerIndex >= allSellers.size()) return;

        Seller s = allSellers.get(currentSellerIndex);
        String litersStr = etLiters.getText().toString().replace(",", ".");
        String rateStr = etRate.getText().toString().replace(",", ".");

        if (litersStr.isEmpty() || rateStr.isEmpty()) {
            Toast.makeText(this, "Enter liters and rate", Toast.LENGTH_SHORT).show();
            return;
        }

        double liters, rate;
        try {
            liters = Double.parseDouble(litersStr);
            rate = Double.parseDouble(rateStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = liters * rate;
        saveEntry(s.getId(), liters, rate, amount);

        dataModified = true;
        currentSellerIndex++;
        if (currentSellerIndex < allSellers.size()) {
            showCurrentSeller();
        } else {
            Toast.makeText(this, "All sellers done!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveEntry(long sellerId, double liters, double rate, double amount) {
        List<Entry> existing = db.getEntriesForDate(currentDate, sessionId);
        for (Entry e : existing) {
            if (e.getSellerId() == sellerId && e.getTimeOfDay().equals(currentTimeOfDay)) {
                db.updateEntry(e.getId(), liters, rate, amount, currentTimeOfDay);
                return;
            }
        }
        db.addEntry(currentDate, sellerId, liters, rate, amount, currentTimeOfDay, sessionId);
    }

    private void skipSeller() {
        currentSellerIndex++;
        if (currentSellerIndex < allSellers.size()) {
            showCurrentSeller();
        } else {
            Toast.makeText(this, "All sellers done!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fillSameAsYesterday() {
        if (allSellers == null || currentSellerIndex >= allSellers.size()) return;
        Seller s = allSellers.get(currentSellerIndex);
        Entry last = db.getLastEntryForSeller(s.getId());
        if (last != null) {
            etLiters.setText(String.valueOf(last.getLiters()));
            etRate.setText(String.valueOf(last.getRate()));
            calculateAmount();
            Toast.makeText(this, "Filled from " + last.getDate(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No previous entry found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBulkMode() {
        layoutBulk.removeAllViews();
        if (allSellers == null) return;

        for (Seller s : allSellers) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_bulk_entry, layoutBulk, false);
            TextView tvName = row.findViewById(R.id.tvBulkName);
            EditText etBulkLiters = row.findViewById(R.id.etBulkLiters);
            EditText etBulkRate = row.findViewById(R.id.etBulkRate);
            CheckBox cbSkip = row.findViewById(R.id.cbSkip);

            tvName.setText(s.getNumber() + " - " + s.getName());
            etBulkLiters.setSelectAllOnFocus(true);
            etBulkRate.setSelectAllOnFocus(true);

            // Pre-fill if exists
            List<Entry> existing = db.getEntriesForDate(currentDate, sessionId);
            for (Entry e : existing) {
                if (e.getSellerId() == s.getId() && e.getTimeOfDay().equals(currentTimeOfDay)) {
                    etBulkLiters.setText(String.valueOf(e.getLiters()));
                    etBulkRate.setText(String.valueOf(e.getRate()));
                    break;
                }
            }

            row.setTag(s.getId());
            layoutBulk.addView(row);
        }

        Button btnSaveBulk = new Button(this);
        btnSaveBulk.setText("Save All Entries");
        btnSaveBulk.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        btnSaveBulk.setTextColor(getResources().getColor(android.R.color.white));
        btnSaveBulk.setOnClickListener(v -> saveBulkEntries());
        layoutBulk.addView(btnSaveBulk);
    }

    private void saveBulkEntries() {
        int savedCount = 0;
        for (int i = 0; i < layoutBulk.getChildCount() - 1; i++) {
            View row = layoutBulk.getChildAt(i);
            if (row.getTag() == null) continue;
            long sellerId = (long) row.getTag();
            EditText etBulkLiters = row.findViewById(R.id.etBulkLiters);
            EditText etBulkRate = row.findViewById(R.id.etBulkRate);
            CheckBox cbSkip = row.findViewById(R.id.cbSkip);

            if (cbSkip != null && cbSkip.isChecked()) continue;

            String litersStr = etBulkLiters.getText().toString().replace(",", ".");
            String rateStr = etBulkRate.getText().toString().replace(",", ".");
            if (litersStr.isEmpty() || rateStr.isEmpty()) continue;

            try {
                double liters = Double.parseDouble(litersStr);
                double rate = Double.parseDouble(rateStr);
                double amount = liters * rate;
                saveEntry(sellerId, liters, rate, amount);
                savedCount++;
            } catch (NumberFormatException ignored) {}
        }
        dataModified = true;
        Toast.makeText(this, savedCount + " entries saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            currentDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvDate.setText(currentDate);
            currentSellerIndex = 0;
            if (bulkMode) setupBulkMode();
            else showCurrentSeller();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onBackPressed() {
        if (dataModified) {
            new AlertDialog.Builder(this)
                    .setTitle("Unsaved Changes?")
                    .setMessage("Are you sure you want to go back?")
                    .setPositiveButton("Yes", (d, w) -> DailyEntryActivity.super.onBackPressed())
                    .setNegativeButton("No", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
