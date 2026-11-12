package com.example.flexibot.models;

public class WaterLog {
    private String id;
    private int amountMl;
    private String dateKey; // Format: yyyy-MM-dd for automatic daily reset
    private long timestamp;

    // Empty constructor required for Firestore
    public WaterLog() {}

    public WaterLog(String id, int amountMl, String dateKey, long timestamp) {
        this.id = id;
        this.amountMl = amountMl;
        this.dateKey = dateKey;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public int getAmountMl() { return amountMl; }
    public String getDateKey() { return dateKey; }
    public long getTimestamp() { return timestamp; }
}