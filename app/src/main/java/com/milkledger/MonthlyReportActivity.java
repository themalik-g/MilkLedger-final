package com.milkledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MonthlyReportActivity extends AppCompatActivity {
    private Spinner spinnerMonth, spinnerYear;
    private TextView tvReport;
    private Button btnGenerate, btnShareText, btnSharePdf, btnOpenPdf, btnBack;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private long sessionId;
    private boolean reportGenerated = false;
    private File currentPdfFile = null;
    private int currentMonth, currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_report);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("MilkLedgerPrefs", MODE_PRIVATE);
        sessionId = prefs.getLong("current_session_id", -1);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        tvReport = findViewById(R.id.tvReport);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnShareText = findViewById(R.id.btnShare);
        btnSharePdf = findViewById(R.id.btnSharePdf);
        btnOpenPdf = findViewById(R.id.btnOpenPdf);
        btnBack = findViewById(R.id.btnBack);

        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        spinnerMonth.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months));

        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int y = currentYear - 5; y <= currentYear + 5; y++) years.add(String.valueOf(y));
        spinnerYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years));
        spinnerYear.setSelection(years.indexOf(String.valueOf(currentYear)));

        btnGenerate.setOnClickListener(v -> generateReport());
        btnShareText.setOnClickListener(v -> shareReport());
        btnSharePdf.setOnClickListener(v -> generateAndSharePdf());
        btnOpenPdf.setOnClickListener(v -> openPdf());
        btnBack.setOnClickListener(v -> finish());
    }

    private void generateReport() {
        currentMonth = spinnerMonth.getSelectedItemPosition() + 1;
        currentYear = Integer.parseInt((String) spinnerYear.getSelectedItem());

        List<Entry> entries = db.getEntriesForMonth(currentMonth, currentYear, sessionId);
        List<CashTransaction> cashList = db.getCashTransactionsForMonth(currentMonth, currentYear, sessionId);

        StringBuilder sb = new StringBuilder();
        sb.append("=== MONTHLY REPORT ===
");
        sb.append(String.format(Locale.getDefault(), "Month: %02d/%d

", currentMonth, currentYear));

        double totalLiters = 0, totalAmount = 0;
        for (Entry e : entries) {
            sb.append(String.format("%s: %s %s - %.2fL @ %.2f = %.2f (%s)
",
                    e.getDate(), e.getSellerNumber(), e.getSellerName(),
                    e.getLiters(), e.getRate(), e.getAmount(), e.getTimeOfDay()));
            totalLiters += e.getLiters();
            totalAmount += e.getAmount();
        }

        sb.append(String.format("
Total Milk: %.2fL
", totalLiters));
        sb.append(String.format("Total Amount: %.2f

", totalAmount));

        double cashPaid = 0, cashReceived = 0;
        for (CashTransaction c : cashList) {
            sb.append(String.format("%s: %s - %.2f (%s) %s
",
                    c.getDate(), c.getSellerName(), c.getAmount(), c.getType(), c.getNotes()));
            if ("paid".equals(c.getType())) cashPaid += c.getAmount();
            else cashReceived += c.getAmount();
        }

        sb.append(String.format("
Cash Paid: %.2f
", cashPaid));
        sb.append(String.format("Cash Received: %.2f
", cashReceived));

        tvReport.setText(sb.toString());
        reportGenerated = true;
    }

    private void shareReport() {
        if (!reportGenerated || tvReport.getText().toString().isEmpty()) {
            Toast.makeText(this, "Generate report first", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = tvReport.getText().toString();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");

        try {
            startActivity(sendIntent);
        } catch (Exception e) {
            sendIntent.setPackage(null);
            startActivity(Intent.createChooser(sendIntent, "Share via"));
        }
    }

    private void generateAndSharePdf() {
        if (!reportGenerated) {
            Toast.makeText(this, "Generate report first", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Generating PDF...", Toast.LENGTH_SHORT).show();
        String title = String.format(Locale.getDefault(), "Monthly Report %02d-%d", currentMonth, currentYear);
        PdfGenerator.generatePdf(this, title, tvReport.getText().toString(),
                new PdfGenerator.PdfCallback() {
                    @Override
                    public void onPdfGenerated(File pdfFile) {
                        currentPdfFile = pdfFile;
                        Toast.makeText(MonthlyReportActivity.this, "PDF ready!", Toast.LENGTH_SHORT).show();
                        PdfGenerator.sharePdf(MonthlyReportActivity.this, pdfFile);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(MonthlyReportActivity.this, "PDF Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openPdf() {
        if (currentPdfFile == null || !currentPdfFile.exists()) {
            Toast.makeText(this, "Generate PDF first", Toast.LENGTH_SHORT).show();
            return;
        }
        PdfGenerator.openPdf(this, currentPdfFile);
    }
}
