package com.milkledger;

public class CashTransaction {
    private long id;
    private String date;
    private long sellerId;
    private double amount;
    private String notes;
    private String type;
    private long sessionId;
    private String sellerName;
    private String sellerNumber;

    public CashTransaction(long id, String date, long sellerId, double amount, String notes,
                           String type, long sessionId, String sellerName, String sellerNumber) {
        this.id = id;
        this.date = date;
        this.sellerId = sellerId;
        this.amount = amount;
        this.notes = notes;
        this.type = type;
        this.sessionId = sessionId;
        this.sellerName = sellerName;
        this.sellerNumber = sellerNumber;
    }

    public long getId() { return id; }
    public String getDate() { return date; }
    public long getSellerId() { return sellerId; }
    public double getAmount() { return amount; }
    public String getNotes() { return notes; }
    public String getType() { return type; }
    public long getSessionId() { return sessionId; }
    public String getSellerName() { return sellerName; }
    public String getSellerNumber() { return sellerNumber; }
}
