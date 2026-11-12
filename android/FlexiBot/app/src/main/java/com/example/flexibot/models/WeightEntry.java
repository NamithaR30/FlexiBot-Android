package com.example.flexibot.models;

public class WeightEntry {
    private String id;
    private double weightKg;
    private String note;
    private long timestamp;

    // Empty constructor required for Firestore
    public WeightEntry() {}

    public WeightEntry(String id, double weightKg, String note, long timestamp) {
        this.id = id;
        this.weightKg = weightKg;
        this.note = note;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public double getWeightKg() { return weightKg; }
    public String getNote() { return note; }
    public long getTimestamp() { return timestamp; }
}