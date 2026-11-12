package com.example.flexibot.models;

public class WorkoutSession {
    private String sessionId;
    private String workoutTitle;
    private String category;
    private int durationMinutes;
    private int exercisesCompleted;
    private int totalExercises;
    private int caloriesBurned;
    private long timestamp;

    // Empty constructor required for Firestore
    public WorkoutSession() {}

    public WorkoutSession(String sessionId, String workoutTitle, String category,
                          int durationMinutes, int exercisesCompleted, int totalExercises,
                          int caloriesBurned, long timestamp) {
        this.sessionId = sessionId;
        this.workoutTitle = workoutTitle;
        this.category = category;
        this.durationMinutes = durationMinutes;
        this.exercisesCompleted = exercisesCompleted;
        this.totalExercises = totalExercises;
        this.caloriesBurned = caloriesBurned;
        this.timestamp = timestamp;
    }

    public String getSessionId() { return sessionId; }
    public String getWorkoutTitle() { return workoutTitle; }
    public String getCategory() { return category; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getExercisesCompleted() { return exercisesCompleted; }
    public int getTotalExercises() { return totalExercises; }
    public int getCaloriesBurned() { return caloriesBurned; }
    public long getTimestamp() { return timestamp; }
}