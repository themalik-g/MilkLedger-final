package com.milkledger;

public class Seller {
    private long id;
    private String number;
    private String name;
    private String contact;
    private String address;
    private String notes;
    private boolean active;
    private long sessionId;

    public Seller(long id, String number, String name, String contact, String address, String notes, boolean active, long sessionId) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.contact = contact;
        this.address = address;
        this.notes = notes;
        this.active = active;
        this.sessionId = sessionId;
    }

    public long getId() { return id; }
    public String getNumber() { return number; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public String getAddress() { return address; }
    public String getNotes() { return notes; }
    public boolean isActive() { return active; }
    public long getSessionId() { return sessionId; }

    // BUG FIX: Override toString so Spinner shows name instead of object hash
    @Override
    public String toString() {
        return number + " - " + name;
    }
}
