package com.milkledger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BackupRestoreActivity extends AppCompatActivity {
    private Button btnBackup, btnRestore, btnAutoBackup;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private static final int REQUEST_CREATE_BACKUP = 100;
    private static final int REQUEST_RESTORE_BACKUP = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("MilkLedgerPrefs", MODE_PRIVATE);
        btnBackup = findViewById(R.id.btnBackup);
        btnRestore = findViewById(R.id.btnRestore);
        btnAutoBackup = findViewById(R.id.btnAutoBackup);

        boolean autoBackup = prefs.getBoolean("auto_backup", false);
        btnAutoBackup.setText(autoBackup ? "Auto Backup: ON" : "Enable Auto Backup");

        btnBackup.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use Storage Access Framework for Android 10+
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/octet-stream");
                String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(Calendar.getInstance().getTime());
                intent.putExtra(Intent.EXTRA_TITLE, "MilkLedger_backup_" + time + ".db");
                startActivityForResult(intent, REQUEST_CREATE_BACKUP);
            } else {
                doLegacyBackup();
            }
        });

        btnRestore.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_RESTORE_BACKUP);
        });

        btnAutoBackup.setOnClickListener(v -> {
            boolean current = prefs.getBoolean("auto_backup", false);
            prefs.edit().putBoolean("auto_backup", !current).apply();
            btnAutoBackup.setText(!current ? "Auto Backup: ON" : "Enable Auto Backup");
            Toast.makeText(this, !current ? "Auto backup enabled" : "Auto backup disabled", Toast.LENGTH_SHORT).show();
        });
    }

    private void doLegacyBackup() {
        try {
            File dbFile = new File(db.getDatabasePath());
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());
            File backupFile = new File(dir, "MilkLedger_backup_" + time + ".db");

            FileChannel src = new FileInputStream(dbFile).getChannel();
            FileChannel dst = new FileOutputStream(backupFile).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();

            Toast.makeText(this, "Backup: " + backupFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;

            if (requestCode == REQUEST_CREATE_BACKUP) {
                try {
                    File dbFile = new File(db.getDatabasePath());
                    InputStream in = new FileInputStream(dbFile);
                    OutputStream out = getContentResolver().openOutputStream(uri);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                    Toast.makeText(this, "Backup saved!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Backup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else if (requestCode == REQUEST_RESTORE_BACKUP) {
                try {
                    File dbFile = new File(db.getDatabasePath());
                    InputStream in = getContentResolver().openInputStream(uri);
                    OutputStream out = new FileOutputStream(dbFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                    Toast.makeText(this, "Restore complete! Restart app.", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Restore failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
