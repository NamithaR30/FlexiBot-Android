package com.example.flexibot.models;

import java.io.Serializable;

public class Exercise implements Serializable {
    private String name;
    private String description;
    private String muscleGroup;
    private String category;
    private String equipmentRequired;
    private int sets;
    private String reps;
    private int restSeconds;
    private boolean completed;

    public Exercise() {}

    public Exercise(String name, String description, String muscleGroup, String category,
                    String equipmentRequired, int sets, String reps, int restSeconds) {
        this.name = name;
        this.description = description;
        this.muscleGroup = muscleGroup;
        this.category = category;
        this.equipmentRequired = equipmentRequired;
        this.sets = sets;
        this.reps = reps;
        this.restSeconds = restSeconds;
        this.completed = false;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getMuscleGroup() { return muscleGroup; }
    public String getCategory() { return category; }
    public String getEquipmentRequired() { return equipmentRequired; }
    public int getSets() { return sets; }
    public String getReps() { return reps; }
    public int getRestSeconds() { return restSeconds; }
    public boolean isCompleted() { return completed; }

    public void setSets(int sets) { this.sets = sets; }
    public void setReps(String reps) { this.reps = reps; }
    public void setRestSeconds(int restSeconds) { this.restSeconds = restSeconds; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}