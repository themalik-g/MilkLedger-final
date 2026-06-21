package com.milkledger;

public class Entry {
    private long id;
    private String date;
    private long sellerId;
    private double liters;
    private double rate;
    private double amount;
    private String timeOfDay;
    private long sessionId;
    private String sellerName;
    private String sellerNumber;

    public Entry(long id, String date, long sellerId, double liters, double rate, double amount,
                 String timeOfDay, long sessionId, String sellerName, String sellerNumber) {
        this.id = id;
        this.date = date;
        this.sellerId = sellerId;
        this.liters = liters;
        this.rate = rate;
        this.amount = amount;
        this.timeOfDay = timeOfDay;
        this.sessionId = sessionId;
        this.sellerName = sellerName;
        this.sellerNumber = sellerNumber;
    }

    public long getId() { return id; }
    public String getDate() { return date; }
    public long getSellerId() { return sellerId; }
    public double getLiters() { return liters; }
    public double getRate() { return rate; }
    public double getAmount() { return amount; }
    public String getTimeOfDay() { return timeOfDay; }
    public long getSessionId() { return sessionId; }
    public String getSellerName() { return sellerName; }
    public String getSellerNumber() { return sellerNumber; }
}
