package com.milkledger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class SellerLedgerActivity extends AppCompatActivity {
    private TextView tvLedger;
    private Button btnShareText, btnSharePdf, btnOpenPdf, btnBack;
    private DatabaseHelper db;
    private long sellerId;
    private boolean reportGenerated = false;
    private File currentPdfFile = null;
    private String currentSellerName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view);

        db = new DatabaseHelper(this);
        sellerId = getIntent().getLongExtra("seller_id", -1);

        tvLedger = findViewById(R.id.tvReport);
        btnShareText = findViewById(R.id.btnShare);
        btnSharePdf = findViewById(R.id.btnSharePdf);
        btnOpenPdf = findViewById(R.id.btnOpenPdf);
        btnBack = findViewById(R.id.btnBack);

        generateLedger();

        btnShareText.setOnClickListener(v -> shareLedger());
        btnSharePdf.setOnClickListener(v -> generateAndSharePdf());
        btnOpenPdf.setOnClickListener(v -> openPdf());
        btnBack.setOnClickListener(v -> finish());
    }

    private void generateLedger() {
        Seller seller = db.getSeller(sellerId);
        if (seller == null) return;
        currentSellerName = seller.getName();

        List<Entry> allEntries = db.getEntriesForSellerAllSessions(sellerId);
        List<CashTransaction> allCash = db.getCashTransactionsForSeller(sellerId);

        StringBuilder sb = new StringBuilder();
        sb.append("=== SELLER LEDGER (ALL SESSIONS) ===
");
        sb.append(String.format("Seller: %s - %s
", seller.getNumber(), seller.getName()));
        sb.append(String.format("Contact: %s
", seller.getContact()));
        sb.append(String.format("Address: %s

", seller.getAddress()));

        sb.append("--- ALL ENTRIES ---
");
        double totalLiters = 0, totalAmount = 0;
        for (Entry e : allEntries) {
            sb.append(String.format(Locale.getDefault(), "%s: %.2fL @ %.2f = %.2f (%s)
",
                    e.getDate(), e.getLiters(), e.getRate(), e.getAmount(), e.getTimeOfDay()));
            totalLiters += e.getLiters();
            totalAmount += e.getAmount();
        }

        sb.append(String.format(Locale.getDefault(), "
Total Milk: %.2fL
", totalLiters));
        sb.append(String.format(Locale.getDefault(), "Total Amount: %.2f

", totalAmount));

        sb.append("--- ALL CASH ---
");
        double cashPaid = 0, cashReceived = 0;
        for (CashTransaction c : allCash) {
            sb.append(String.format(Locale.getDefault(), "%s: %.2f (%s) %s
",
                    c.getDate(), c.getAmount(), c.getType(), c.getNotes()));
            if ("paid".equals(c.getType())) cashPaid += c.getAmount();
            else cashReceived += c.getAmount();
        }

        double balance = db.getSellerBalance(sellerId);
        sb.append(String.format(Locale.getDefault(), "
Cash Paid: %.2f
", cashPaid));
        sb.append(String.format(Locale.getDefault(), "Cash Received: %.2f
", cashReceived));
        sb.append(String.format(Locale.getDefault(), "FINAL BALANCE: %.2f (%s)
",
                Math.abs(balance), balance >= 0 ? "THEY OWE YOU" : "YOU OWE THEM"));

        tvLedger.setText(sb.toString());
        reportGenerated = true;
    }

    private void shareLedger() {
        if (!reportGenerated || tvLedger.getText().toString().isEmpty()) {
            Toast.makeText(this, "No ledger to share", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = tvLedger.getText().toString();
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
            Toast.makeText(this, "Generate ledger first", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Generating PDF...", Toast.LENGTH_SHORT).show();
        PdfGenerator.generatePdf(this, "Seller Ledger " + currentSellerName, tvLedger.getText().toString(),
                new PdfGenerator.PdfCallback() {
                    @Override
                    public void onPdfGenerated(File pdfFile) {
                        currentPdfFile = pdfFile;
                        Toast.makeText(SellerLedgerActivity.this, "PDF ready!", Toast.LENGTH_SHORT).show();
                        PdfGenerator.sharePdf(SellerLedgerActivity.this, pdfFile);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(SellerLedgerActivity.this, "PDF Error: " + error, Toast.LENGTH_LONG).show();
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
