package com.example.flexibot.models;

public class User {
    private String name;
    private String email;
    private int age;
    private String gender;
    private double height;
    private double weight;
    private String fitnessGoal;
    private String fitnessLevel;
    private String equipment;
    private long createdAt;

    // Empty constructor required for Firebase Firestore
    public User() {}

    public User(String name, String email, int age, String gender, double height, double weight, String fitnessGoal, String fitnessLevel, String equipment, long createdAt) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.fitnessGoal = fitnessGoal;
        this.fitnessLevel = fitnessLevel;
        this.equipment = equipment;
        this.createdAt = createdAt;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getAge() { return age; }
    public String getGender() { return gender; }
    public double getHeight() { return height; }
    public double getWeight() { return weight; }
    public String getFitnessGoal() { return fitnessGoal; }
    public String getFitnessLevel() { return fitnessLevel; }
    public String getEquipment() { return equipment; }
    public long getCreatedAt() { return createdAt; }
}