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
import java.util.List;
import java.util.Locale;

public class SellerWiseReportActivity extends AppCompatActivity {
    private Spinner spinnerSeller;
    private TextView tvReport;
    private Button btnGenerate, btnShareText, btnSharePdf, btnOpenPdf, btnBack;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private long sessionId;
    private boolean reportGenerated = false;
    private File currentPdfFile = null;
    private String currentSellerName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_wise_report);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("MilkLedgerPrefs", MODE_PRIVATE);
        sessionId = prefs.getLong("current_session_id", -1);

        spinnerSeller = findViewById(R.id.spinnerSeller);
        tvReport = findViewById(R.id.tvReport);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnShareText = findViewById(R.id.btnShare);
        btnSharePdf = findViewById(R.id.btnSharePdf);
        btnOpenPdf = findViewById(R.id.btnOpenPdf);
        btnBack = findViewById(R.id.btnBack);

        loadSellers();

        btnGenerate.setOnClickListener(v -> generateReport());
        btnShareText.setOnClickListener(v -> shareReport());
        btnSharePdf.setOnClickListener(v -> generateAndSharePdf());
        btnOpenPdf.setOnClickListener(v -> openPdf());
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadSellers() {
        List<Seller> sellers = db.getAllSellers(sessionId, false);
        ArrayAdapter<Seller> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sellers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeller.setAdapter(adapter);
    }

    private void generateReport() {
        Seller seller = (Seller) spinnerSeller.getSelectedItem();
        if (seller == null) return;
        currentSellerName = seller.getName();

        List<Entry> entries = db.getEntriesForSeller(seller.getId());
        List<CashTransaction> cashList = db.getCashTransactionsForSeller(seller.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("=== SELLER WISE REPORT ===
");
        sb.append(String.format("Seller: %s - %s
", seller.getNumber(), seller.getName()));
        sb.append(String.format("Contact: %s

", seller.getContact()));

        double totalLiters = 0, totalAmount = 0;
        for (Entry e : entries) {
            sb.append(String.format("%s: %.2fL @ %.2f = %.2f (%s)
",
                    e.getDate(), e.getLiters(), e.getRate(), e.getAmount(), e.getTimeOfDay()));
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
            sb.append(String.format("%s: %.2f (%s) %s
",
                    c.getDate(), c.getAmount(), c.getType(), c.getNotes()));
            if ("paid".equals(c.getType())) cashPaid += c.getAmount();
            else cashReceived += c.getAmount();
        }

        double balance = db.getSellerBalance(seller.getId());
        sb.append(String.format("
Cash Paid: %.2f
", cashPaid));
        sb.append(String.format("Cash Received: %.2f
", cashReceived));
        sb.append(String.format("BALANCE: %.2f (%s)
", Math.abs(balance), balance >= 0 ? "THEY OWE YOU" : "YOU OWE THEM"));

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
        PdfGenerator.generatePdf(this, "Seller Report " + currentSellerName, tvReport.getText().toString(),
                new PdfGenerator.PdfCallback() {
                    @Override
                    public void onPdfGenerated(File pdfFile) {
                        currentPdfFile = pdfFile;
                        Toast.makeText(SellerWiseReportActivity.this, "PDF ready!", Toast.LENGTH_SHORT).show();
                        PdfGenerator.sharePdf(SellerWiseReportActivity.this, pdfFile);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(SellerWiseReportActivity.this, "PDF Error: " + error, Toast.LENGTH_LONG).show();
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
