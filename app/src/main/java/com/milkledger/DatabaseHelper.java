package com.milkledger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "MilkLedger.db";
    private static final int DATABASE_VERSION = 3;

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_SESSIONS = "sessions";
    private static final String TABLE_SELLERS = "sellers";
    private static final String TABLE_ENTRIES = "entries";
    private static final String TABLE_CASH_PAID = "cash_paid";
    private static final String TABLE_BACKUP = "backup_meta";

    // Users columns
    private static final String COL_USER_ID = "user_id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_SECURITY_Q = "security_question";
    private static final String COL_SECURITY_A = "security_answer";
    private static final String COL_PIN = "pin";
    private static final String COL_USE_PIN = "use_pin";

    // Sessions columns
    private static final String COL_SESSION_ID = "session_id";
    private static final String COL_SESSION_NAME = "session_name";
    private static final String COL_YEAR = "year";
    private static final String COL_USER_REF = "user_ref";

    // Sellers columns
    private static final String COL_SELLER_ID = "seller_id";
    private static final String COL_SELLER_NUMBER = "seller_number";
    private static final String COL_SELLER_NAME = "seller_name";
    private static final String COL_SELLER_CONTACT = "seller_contact";
    private static final String COL_SELLER_ADDRESS = "seller_address";
    private static final String COL_SELLER_NOTES = "seller_notes";
    private static final String COL_SELLER_ACTIVE = "seller_active";
    private static final String COL_SELLER_SESSION = "seller_session";

    // Entries columns
    private static final String COL_ENTRY_ID = "entry_id";
    private static final String COL_ENTRY_DATE = "entry_date";
    private static final String COL_ENTRY_SELLER_ID = "entry_seller_id";
    private static final String COL_ENTRY_LITERS = "liters";
    private static final String COL_ENTRY_RATE = "rate";
    private static final String COL_ENTRY_AMOUNT = "amount";
    private static final String COL_ENTRY_TIME = "entry_time";
    private static final String COL_ENTRY_SESSION = "entry_session";

    // Cash columns
    private static final String COL_CASH_ID = "cash_id";
    private static final String COL_CASH_DATE = "cash_date";
    private static final String COL_CASH_SELLER_ID = "cash_seller_id";
    private static final String COL_CASH_AMOUNT = "cash_amount";
    private static final String COL_CASH_NOTES = "cash_notes";
    private static final String COL_CASH_TYPE = "cash_type";
    private static final String COL_CASH_SESSION = "cash_session";

    // Backup meta
    private static final String COL_BACKUP_ID = "backup_id";
    private static final String COL_BACKUP_DATE = "backup_date";
    private static final String COL_BACKUP_PATH = "backup_path";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT, " +
                COL_SECURITY_Q + " TEXT, " +
                COL_SECURITY_A + " TEXT, " +
                COL_PIN + " TEXT, " +
                COL_USE_PIN + " INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_SESSIONS + " (" +
                COL_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SESSION_NAME + " TEXT, " +
                COL_YEAR + " INTEGER, " +
                COL_USER_REF + " INTEGER, " +
                "FOREIGN KEY(" + COL_USER_REF + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_SELLERS + " (" +
                COL_SELLER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SELLER_NUMBER + " TEXT, " +
                COL_SELLER_NAME + " TEXT, " +
                COL_SELLER_CONTACT + " TEXT, " +
                COL_SELLER_ADDRESS + " TEXT, " +
                COL_SELLER_NOTES + " TEXT, " +
                COL_SELLER_ACTIVE + " INTEGER DEFAULT 1, " +
                COL_SELLER_SESSION + " INTEGER, " +
                "FOREIGN KEY(" + COL_SELLER_SESSION + ") REFERENCES " + TABLE_SESSIONS + "(" + COL_SESSION_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_ENTRIES + " (" +
                COL_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ENTRY_DATE + " TEXT, " +
                COL_ENTRY_SELLER_ID + " INTEGER, " +
                COL_ENTRY_LITERS + " REAL, " +
                COL_ENTRY_RATE + " REAL, " +
                COL_ENTRY_AMOUNT + " REAL, " +
                COL_ENTRY_TIME + " TEXT DEFAULT 'morning', " +
                COL_ENTRY_SESSION + " INTEGER, " +
                "FOREIGN KEY(" + COL_ENTRY_SELLER_ID + ") REFERENCES " + TABLE_SELLERS + "(" + COL_SELLER_ID + "), " +
                "FOREIGN KEY(" + COL_ENTRY_SESSION + ") REFERENCES " + TABLE_SESSIONS + "(" + COL_SESSION_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_CASH_PAID + " (" +
                COL_CASH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CASH_DATE + " TEXT, " +
                COL_CASH_SELLER_ID + " INTEGER, " +
                COL_CASH_AMOUNT + " REAL, " +
                COL_CASH_NOTES + " TEXT, " +
                COL_CASH_TYPE + " TEXT DEFAULT 'paid', " +
                COL_CASH_SESSION + " INTEGER, " +
                "FOREIGN KEY(" + COL_CASH_SELLER_ID + ") REFERENCES " + TABLE_SELLERS + "(" + COL_SELLER_ID + "), " +
                "FOREIGN KEY(" + COL_CASH_SESSION + ") REFERENCES " + TABLE_SESSIONS + "(" + COL_SESSION_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_BACKUP + " (" +
                COL_BACKUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BACKUP_DATE + " TEXT, " +
                COL_BACKUP_PATH + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading DB from " + oldVersion + " to " + newVersion);
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_SECURITY_Q + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_SECURITY_A + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_PIN + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_USE_PIN + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_ENTRIES + " ADD COLUMN " + COL_ENTRY_TIME + " TEXT DEFAULT 'morning'");
            db.execSQL("ALTER TABLE " + TABLE_CASH_PAID + " ADD COLUMN " + COL_CASH_TYPE + " TEXT DEFAULT 'paid'");
            db.execSQL("ALTER TABLE " + TABLE_SELLERS + " ADD COLUMN " + COL_SELLER_ACTIVE + " INTEGER DEFAULT 1");
        }
    }

    // ===================== USER METHODS =====================
    public long createUser(String username, String password, String securityQ, String securityA, String pin, boolean usePin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, password);
        cv.put(COL_SECURITY_Q, securityQ);
        cv.put(COL_SECURITY_A, securityA);
        cv.put(COL_PIN, pin);
        cv.put(COL_USE_PIN, usePin ? 1 : 0);
        return db.insert(TABLE_USERS, null, cv);
    }

    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password});
        boolean valid = c.getCount() > 0;
        c.close();
        return valid;
    }

    public boolean validatePin(String username, String pin) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=? AND " + COL_PIN + "=? AND " + COL_USE_PIN + "=1",
                new String[]{username, pin});
        boolean valid = c.getCount() > 0;
        c.close();
        return valid;
    }

    public String getSecurityQuestion(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COL_SECURITY_Q + " FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=?", new String[]{username});
        String q = null;
        if (c.moveToFirst()) q = c.getString(0);
        c.close();
        return q;
    }

    public boolean checkSecurityAnswer(String username, String answer) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_USERNAME + "=? AND " + COL_SECURITY_A + "=?",
                new String[]{username, answer});
        boolean valid = c.getCount() > 0;
        c.close();
        return valid;
    }

    public boolean resetPassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PASSWORD, newPassword);
        return db.update(TABLE_USERS, cv, COL_USERNAME + "=?", new String[]{username}) > 0;
    }

    public boolean updatePin(String username, String pin, boolean usePin) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PIN, pin);
        cv.put(COL_USE_PIN, usePin ? 1 : 0);
        return db.update(TABLE_USERS, cv, COL_USERNAME + "=?", new String[]{username}) > 0;
    }

    public boolean doesUserExist() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        boolean exists = false;
        if (c.moveToFirst()) exists = c.getInt(0) > 0;
        c.close();
        return exists;
    }

    public String getUsername() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COL_USERNAME + " FROM " + TABLE_USERS + " LIMIT 1", null);
        String name = "";
        if (c.moveToFirst()) name = c.getString(0);
        c.close();
        return name;
    }

    public boolean usesPin() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + COL_USE_PIN + " FROM " + TABLE_USERS + " LIMIT 1", null);
        boolean uses = false;
        if (c.moveToFirst()) uses = c.getInt(0) == 1;
        c.close();
        return uses;
    }

    // ===================== SESSION METHODS =====================
    public long createSession(String name, int year, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SESSION_NAME, name);
        cv.put(COL_YEAR, year);
        cv.put(COL_USER_REF, userId);
        return db.insert(TABLE_SESSIONS, null, cv);
    }

    public List<Session> getAllSessions() {
        List<Session> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SESSIONS + " ORDER BY " + COL_SESSION_ID + " DESC", null);
        while (c.moveToNext()) {
            list.add(new Session(c.getLong(0), c.getString(1), c.getInt(2)));
        }
        c.close();
        return list;
    }

    public Session getSession(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SESSIONS + " WHERE " + COL_SESSION_ID + "=?", new String[]{String.valueOf(id)});
        Session s = null;
        if (c.moveToFirst()) {
            s = new Session(c.getLong(0), c.getString(1), c.getInt(2));
        }
        c.close();
        return s;
    }

    // ===================== SELLER METHODS =====================
    public long createSeller(String number, String name, String contact, String address, String notes, long sessionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SELLER_NUMBER, number);
        cv.put(COL_SELLER_NAME, name);
        cv.put(COL_SELLER_CONTACT, contact);
        cv.put(COL_SELLER_ADDRESS, address);
        cv.put(COL_SELLER_NOTES, notes);
        cv.put(COL_SELLER_ACTIVE, 1);
        cv.put(COL_SELLER_SESSION, sessionId);
        return db.insert(TABLE_SELLERS, null, cv);
    }

    public List<Seller> getAllSellers(long sessionId, boolean activeOnly) {
        List<Seller> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_SELLERS + " WHERE " + COL_SELLER_SESSION + "=?";
        if (activeOnly) query += " AND " + COL_SELLER_ACTIVE + "=1";
        query += " ORDER BY " + COL_SELLER_NUMBER;
        Cursor c = db.rawQuery(query, new String[]{String.valueOf(sessionId)});
        while (c.moveToNext()) {
            list.add(new Seller(c.getLong(0), c.getString(1), c.getString(2), c.getString(3),
                    c.getString(4), c.getString(5), c.getInt(6) == 1, c.getLong(7)));
        }
        c.close();
        return list;
    }

    public List<Seller> getAllSellersAllSessions() {
        List<Seller> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SELLERS + " ORDER BY " + COL_SELLER_NUMBER, null);
        while (c.moveToNext()) {
            list.add(new Seller(c.getLong(0), c.getString(1), c.getString(2), c.getString(3),
                    c.getString(4), c.getString(5), c.getInt(6) == 1, c.getLong(7)));
        }
        c.close();
        return list;
    }

    public List<Seller> searchSellers(long sessionId, String query) {
        List<Seller> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String q = "%" + query + "%";
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SELLERS + " WHERE " + COL_SELLER_SESSION + "=? AND (" +
                COL_SELLER_NAME + " LIKE ? OR " + COL_SELLER_NUMBER + " LIKE ? OR " + COL_SELLER_CONTACT + " LIKE ?) AND " +
                COL_SELLER_ACTIVE + "=1 ORDER BY " + COL_SELLER_NUMBER,
                new String[]{String.valueOf(sessionId), q, q, q});
        while (c.moveToNext()) {
            list.add(new Seller(c.getLong(0), c.getString(1), c.getString(2), c.getString(3),
                    c.getString(4), c.getString(5), c.getInt(6) == 1, c.getLong(7)));
        }
        c.close();
        return list;
    }

    public Seller getSeller(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SELLERS + " WHERE " + COL_SELLER_ID + "=?", new String[]{String.valueOf(id)});
        Seller s = null;
        if (c.moveToFirst()) {
            s = new Seller(c.getLong(0), c.getString(1), c.getString(2), c.getString(3),
                    c.getString(4), c.getString(5), c.getInt(6) == 1, c.getLong(7));
        }
        c.close();
        return s;
    }

    public boolean updateSeller(long id, String name, String contact, String address, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SELLER_NAME, name);
        cv.put(COL_SELLER_CONTACT, contact);
        cv.put(COL_SELLER_ADDRESS, address);
        cv.put(COL_SELLER_NOTES, notes);
        return db.update(TABLE_SELLERS, cv, COL_SELLER_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean setSellerActive(long id, boolean active) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SELLER_ACTIVE, active ? 1 : 0);
        return db.update(TABLE_SELLERS, cv, COL_SELLER_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public int getNextSellerNumber(long sessionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SELLERS + " WHERE " + COL_SELLER_SESSION + "=?", new String[]{String.valueOf(sessionId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count + 1;
    }

    // ===================== ENTRY METHODS =====================
    public long addEntry(String date, long sellerId, double liters, double rate, double amount, String timeOfDay, long sessionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ENTRY_DATE, date);
        cv.put(COL_ENTRY_SELLER_ID, sellerId);
        cv.put(COL_ENTRY_LITERS, liters);
        cv.put(COL_ENTRY_RATE, rate);
        cv.put(COL_ENTRY_AMOUNT, amount);
        cv.put(COL_ENTRY_TIME, timeOfDay);
        cv.put(COL_ENTRY_SESSION, sessionId);
        return db.insert(TABLE_ENTRIES, null, cv);
    }

    public boolean updateEntry(long entryId, double liters, double rate, double amount, String timeOfDay) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ENTRY_LITERS, liters);
        cv.put(COL_ENTRY_RATE, rate);
        cv.put(COL_ENTRY_AMOUNT, amount);
        cv.put(COL_ENTRY_TIME, timeOfDay);
        return db.update(TABLE_ENTRIES, cv, COL_ENTRY_ID + "=?", new String[]{String.valueOf(entryId)}) > 0;
    }

    public boolean deleteEntry(long entryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ENTRIES, COL_ENTRY_ID + "=?", new String[]{String.valueOf(entryId)}) > 0;
    }

    public List<Entry> getEntriesForDate(String date, long sessionId) {
        List<Entry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT e.*, s." + COL_SELLER_NAME + ", s." + COL_SELLER_NUMBER +
                " FROM " + TABLE_ENTRIES + " e JOIN " + TABLE_SELLERS + " s ON e." + COL_ENTRY_SELLER_ID + "=s." + COL_SELLER_ID +
                " WHERE e." + COL_ENTRY_DATE + "=? AND e." + COL_ENTRY_SESSION + "=? ORDER BY s." + COL_SELLER_NUMBER,
                new String[]{date, String.valueOf(sessionId)});
        while (c.moveToNext()) {
            list.add(new Entry(c.getLong(0), c.getString(1), c.getLong(2), c.getDouble(3),
                    c.getDouble(4), c.getDouble(5), c.getString(6), c.getLong(7), c.getString(8), c.getString(9)));
        }
        c.close();
        return list;
    }

    public List<Entry> getEntriesForSeller(long sellerId) {
        List<Entry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT e.*, s." + COL_SELLER_NAME + ", s." + COL_SELLER_NUMBER +
                " FROM " + TABLE_ENTRIES + " e JOIN " + TABLE_SELLERS + " s ON e." + COL_ENTRY_SELLER_ID + "=s." + COL_SELLER_ID +
                " WHERE e." + COL_ENTRY_SELLER_ID + "=? ORDER BY e." + COL_ENTRY_DATE + " DESC",
                new String[]{String.valueOf(sellerId)});
        while (c.moveToNext()) {
            list.add(new Entry(c.getLong(0), c.getString(1), c.getLong(2), c.getDouble(3),
                    c.getDouble(4), c.getDouble(5), c.getString(6), c.getLong(7), c.getString(8), c.getString(9)));
        }
        c.close();
        return list;
    }

    public List<Entry> getEntriesForSellerAllSessions(long sellerId) {
        List<Entry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT e.*, s." + COL_SELLER_NAME + ", s." + COL_SELLER_NUMBER +
                " FROM " + TABLE_ENTRIES + " e JOIN " + TABLE_SELLERS + " s ON e." + COL_ENTRY_SELLER_ID + "=s." + COL_SELLER_ID +
                " WHERE e." + COL_ENTRY_SELLER_ID + "=? ORDER BY e." + COL_ENTRY_DATE + " DESC",
                new String[]{String.valueOf(sellerId)});
        while (c.moveToNext()) {
            list.add(new Entry(c.getLong(0), c.getString(1), c.getLong(2), c.getDouble(3),
                    c.getDouble(4), c.getDouble(5), c.getString(6), c.getLong(7), c.getString(8), c.getString(9)));
        }
        c.close();
        return list;
    }

    public List<Entry> getEntriesForMonth(int month, int year, long sessionId) {
        List<Entry> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);
        Cursor c = db.rawQuery("SELECT e.*, s." + COL_SELLER_NAME + ", s." + COL_SELLER_NUMBER +
                " FROM " + TABLE_ENTRIES + " e JOIN " + TABLE_SELLERS + " s ON e." + COL_ENTRY_SELLER_ID + "=s." + COL_SELLER_ID +
                " WHERE e." + COL_ENTRY_DATE + " LIKE ? AND e." + COL_ENTRY_SESSION + "=? ORDER BY e." + COL_ENTRY_DATE,
                new String[]{"%" + yearStr + "-" + monthStr + "-%", String.valueOf(sessionId)});
        while (c.moveToNext()) {
            list.add(new Entry(c.getLong(0), c.getString(1), c.getLong(2), c.getDouble(3),
                    c.getDouble(4), c.getDouble(5), c.getString(6), c.getLong(7), c.getString(8), c.getString(9)));
        }
        c.close();
        return list;
    }

    public Entry getEntry(long entryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT e.*, s." + COL_SELLER_NAME + ", s." + COL_SELLER_NUMBER +
                " FROM " + TABLE_ENTRIES + " e JOIN " + TABLE_SELLERS + " s ON e." + COL_ENTRY_SELLER_ID + "=s." + COL_SELLER_ID +
                " WHERE e." + COL_ENTRY_ID + "=?", new String[]{String.valueOf(entryId)});
        Entry e = null;
        if (c.moveToFirst()) {
            e = new Entry(c.getLong(0), c.getString(1), c.getLong(2), c.getDouble(3),
                    c.getDouble(4), c.getDouble(5), c.getString(6), c.getLong(7), c.getString(8), c.getString(9));
        }
        c.close();
        return e;
    }

    public Entry getLastEntryForSeller(long sellerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT e.*, s." + COL_SELLER_NAME + ", s." + COL_SELLER_NUMBER +
                " FROM " + TABLE_ENTRIES + " e JOIN " + TABLE_SELLERS + " s ON e." + COL_ENTRY_SELLER_ID + "=s." + COL_SELLER_ID +
                " WHERE e." + COL_ENTRY_SELLER_ID + "=? ORDER BY e." + COL_ENTRY_DATE + " DESC, e." + COL_ENTRY_ID + " DESC LIMIT 1",
                new String[]{String.valueOf(sellerId)});
        Entry e = null;
        if (c.moveToFirst()) {
            e = new Entry(c.getLong(0), c.getString(1), c.getLong(2), c.getDouble(3),
                    c.getDouble(4), c.getDouble(5), c.getString(6), c.getLong(7), c.getString(8), c.getString(9));
        }
        c.close();
        return e;
    }

    // ===================== CASH METHODS =====================
    public long addCashTransaction(String date, long sellerId, double amount, String notes, String type, long sessionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CASH_DATE, date);
        cv.put(COL_CASH_SELLER_ID, sellerId);
        cv.put(COL_CASH_AMOUNT, amount);
        cv.put(COL_CASH_NOTES, notes);
        cv.put(COL_CASH_TYPE, type);
        cv.put(COL_CASH_SESSION, sessionId);
        return db.insert(TABLE_CASH_PAID, null, cv);
    }

    public boolean updateCashTransaction(long cashId, double amount, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CASH_AMOUNT, amount);
        cv.put(COL_CASH_NOTES, notes);
        return db.update(TABLE_CASH_PAID, cv, COL_CASH_ID + "=?", new String[]{String.valueOf(cashId)}) > 0;
    }

    public boolean deleteCashTransaction(long cashId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CASH_PAID, COL_CASH_ID + "=?", new String[]{String.valueOf(cashId)}) > 0;
    }

    public List<CashTransaction> getCashTransactionsForDate(String date, long sessionId) {
        List<CashTransaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT c.*, s." + COL_SELLER_NAME + ", s." + COL_SELLER_NUMBER +
                " FROM " + TABLE_CASH_PAID + " c JOIN " + TABLE_SELLERS + " s ON c." + COL_CASH_SELLER_ID + "=s." + COL_SELLER_ID +
                " WHERE c." + COL_CASH_DATE + "=? AND c." + COL_CASH_SESSION + "=? ORDER BY c." + COL_CASH_ID + " DESC",
                new String[]{date, String.valueOf(sessionId)});
        while (c.moveToNext()) {
            list.add(new CashTransaction(c.getLong(0), c.getString(1), c.getLong(2), c.getDouble(3),
                    c.getString(4), c.getString(5), c.getLong(6), c.getString(7), c.getString(8)));
        }
        c.close();
        return list;
    }

    public List<CashTransaction> getCashTransactionsForSeller(long sellerId) {
        List<CashTransaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT c.*, s." + COL_SELLER_NAME + ", s." + COL_SELLER_NUMBER +
                " FROM " + TABLE_CASH_PAID + " c JOIN " + TABLE_SELLERS + " s ON c." + COL_CASH_SELLER_ID + "=s." + COL_SELLER_ID +
                " WHERE c." + COL_CASH_SELLER_ID + "=? ORDER BY c." + COL_CASH_DATE + " DESC",
                new String[]{String.valueOf(sellerId)});
        while (c.moveToNext()) {
            list.add(new CashTransaction(c.getLong(0), c.getString(1), c.getLong(2), c.getDouble(3),
                    c.getString(4), c.getString(5), c.getLong(6), c.getString(7), c.getString(8)));
        }
        c.close();
        return list;
    }

    public List<CashTransaction> getCashTransactionsForMonth(int month, int year, long sessionId) {
        List<CashTransaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);
        Cursor c = db.rawQuery("SELECT c.*, s." + COL_SELLER_NAME + ", s." + COL_SELLER_NUMBER +
                " FROM " + TABLE_CASH_PAID + " c JOIN " + TABLE_SELLERS + " s ON c." + COL_CASH_SELLER_ID + "=s." + COL_SELLER_ID +
                " WHERE c." + COL_CASH_DATE + " LIKE ? AND c." + COL_CASH_SESSION + "=? ORDER BY c." + COL_CASH_DATE,
                new String[]{"%" + yearStr + "-" + monthStr + "-%", String.valueOf(sessionId)});
        while (c.moveToNext()) {
            list.add(new CashTransaction(c.getLong(0), c.getString(1), c.getLong(2), c.getDouble(3),
                    c.getString(4), c.getString(5), c.getLong(6), c.getString(7), c.getString(8)));
        }
        c.close();
        return list;
    }

    // ===================== BALANCE METHODS =====================
    public double getSellerBalance(long sellerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Total milk amount (what they owe you for milk)
        Cursor c1 = db.rawQuery("SELECT SUM(" + COL_ENTRY_AMOUNT + ") FROM " + TABLE_ENTRIES +
                " WHERE " + COL_ENTRY_SELLER_ID + "=?", new String[]{String.valueOf(sellerId)});
        double milkTotal = 0;
        if (c1.moveToFirst() && !c1.isNull(0)) milkTotal = c1.getDouble(0);
        c1.close();

        // Cash paid (you paid them - reduces their debt)
        Cursor c2 = db.rawQuery("SELECT SUM(" + COL_CASH_AMOUNT + ") FROM " + TABLE_CASH_PAID +
                " WHERE " + COL_CASH_SELLER_ID + "=? AND " + COL_CASH_TYPE + "='paid'", new String[]{String.valueOf(sellerId)});
        double cashPaid = 0;
        if (c2.moveToFirst() && !c2.isNull(0)) cashPaid = c2.getDouble(0);
        c2.close();

        // Cash received (they paid you - increases their credit)
        Cursor c3 = db.rawQuery("SELECT SUM(" + COL_CASH_AMOUNT + ") FROM " + TABLE_CASH_PAID +
                " WHERE " + COL_CASH_SELLER_ID + "=? AND " + COL_CASH_TYPE + "='received'", new String[]{String.valueOf(sellerId)});
        double cashReceived = 0;
        if (c3.moveToFirst() && !c3.isNull(0)) cashReceived = c3.getDouble(0);
        c3.close();

        // Balance: Positive = they owe you (milkTotal + received - paid)
        return milkTotal + cashReceived - cashPaid;
    }

    public double getTotalMilkForDate(String date, long sessionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + COL_ENTRY_LITERS + ") FROM " + TABLE_ENTRIES +
                " WHERE " + COL_ENTRY_DATE + "=? AND " + COL_ENTRY_SESSION + "=?",
                new String[]{date, String.valueOf(sessionId)});
        double total = 0;
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close();
        return total;
    }

    public double getTotalAmountForDate(String date, long sessionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + COL_ENTRY_AMOUNT + ") FROM " + TABLE_ENTRIES +
                " WHERE " + COL_ENTRY_DATE + "=? AND " + COL_ENTRY_SESSION + "=?",
                new String[]{date, String.valueOf(sessionId)});
        double total = 0;
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close();
        return total;
    }

    public double getTotalCashPaidForDate(String date, long sessionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + COL_CASH_AMOUNT + ") FROM " + TABLE_CASH_PAID +
                " WHERE " + COL_CASH_DATE + "=? AND " + COL_CASH_TYPE + "='paid' AND " + COL_CASH_SESSION + "=?",
                new String[]{date, String.valueOf(sessionId)});
        double total = 0;
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close();
        return total;
    }

    public double getTotalCashReceivedForDate(String date, long sessionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + COL_CASH_AMOUNT + ") FROM " + TABLE_CASH_PAID +
                " WHERE " + COL_CASH_DATE + "=? AND " + COL_CASH_TYPE + "='received' AND " + COL_CASH_SESSION + "=?",
                new String[]{date, String.valueOf(sessionId)});
        double total = 0;
        if (c.moveToFirst() && !c.isNull(0)) total = c.getDouble(0);
        c.close();
        return total;
    }

    // ===================== BACKUP =====================
    public String getDatabasePath() {
        return getReadableDatabase().getPath();
    }
}
