package com.milkledger;

public class Session {
    private long id;
    private String name;
    private int year;

    public Session(long id, String name, int year) {
        this.id = id;
        this.name = name;
        this.year = year;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public int getYear() { return year; }

    @Override
    public String toString() {
        return name + " (" + year + ")";
    }
}
