package com.milkledger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SessionSetupActivity extends AppCompatActivity {
    private EditText etSessionName, etYear;
    private Button btnCreateSession;
    private RecyclerView rvSessions;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private SessionAdapter sessionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_setup);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("MilkLedgerPrefs", MODE_PRIVATE);

        etSessionName = findViewById(R.id.etSessionName);
        etYear = findViewById(R.id.etYear);
        btnCreateSession = findViewById(R.id.btnCreateSession);
        rvSessions = findViewById(R.id.rvSessions);

        rvSessions.setLayoutManager(new LinearLayoutManager(this));
        loadSessions();

        btnCreateSession.setOnClickListener(v -> createSession());
    }

    private void createSession() {
        String name = etSessionName.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();

        if (name.isEmpty()) {
            etSessionName.setError("Enter session name");
            return;
        }
        if (yearStr.isEmpty()) {
            etYear.setError("Enter year");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            etYear.setError("Year must be a number");
            return;
        }

        if (year < 2000 || year > 2100) {
            etYear.setError("Year must be between 2000-2100");
            return;
        }

        long userId = 1; // simplified
        long sessionId = db.createSession(name, year, userId);
        if (sessionId > 0) {
            prefs.edit().putLong("current_session_id", sessionId).apply();
            Toast.makeText(this, "Session created", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        }
    }

    private void loadSessions() {
        List<Session> sessions = db.getAllSessions();
        sessionAdapter = new SessionAdapter(sessions, session -> {
            prefs.edit().putLong("current_session_id", session.getId()).apply();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
        rvSessions.setAdapter(sessionAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSessions();
    }
}
