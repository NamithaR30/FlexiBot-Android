package com.example.flexibot.models;

public class FitnessGoalTarget {
    private String id;
    private String goalCategory;
    private String title;
    private String unit;
    private double startValue;
    private double currentValue;
    private double targetValue;
    private long updatedAt;

    // Empty constructor required for Firestore
    public FitnessGoalTarget() {}

    public FitnessGoalTarget(String id, String goalCategory, String title, String unit,
                             double startValue, double currentValue, double targetValue, long updatedAt) {
        this.id = id;
        this.goalCategory = goalCategory;
        this.title = title;
        this.unit = unit;
        this.startValue = startValue;
        this.currentValue = currentValue;
        this.targetValue = targetValue;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getGoalCategory() { return goalCategory; }
    public String getTitle() { return title; }
    public String getUnit() { return unit; }
    public double getStartValue() { return startValue; }
    public double getCurrentValue() { return currentValue; }
    public double getTargetValue() { return targetValue; }
    public long getUpdatedAt() { return updatedAt; }

    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // Calculates accurate completion % for both increasing and decreasing targets (e.g., weight loss)
    public int calculateCompletionPercentage() {
        double totalDistance = Math.abs(targetValue - startValue);
        if (totalDistance == 0) {
            return currentValue == targetValue ? 100 : 0;
        }

        double achievedDistance;
        if (targetValue > startValue) {
            achievedDistance = currentValue - startValue;
        } else {
            achievedDistance = startValue - currentValue;
        }

        double ratio = (achievedDistance / totalDistance) * 100.0;
        if (ratio < 0) return 0;
        if (ratio > 100) return 100;
        return (int) Math.round(ratio);
    }
}