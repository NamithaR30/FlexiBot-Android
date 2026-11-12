package com.example.flexibot.utils;

import com.example.flexibot.models.Exercise;
import com.example.flexibot.models.User;

import java.util.ArrayList;
import java.util.List;

public class WorkoutGenerator {

    // Master exercise catalog tagged by category and required equipment
    private static List<Exercise> getExerciseCatalog() {
        List<Exercise> catalog = new ArrayList<>();

        // --- BODYWEIGHT (No equipment) ---
        catalog.add(new Exercise("Push-Ups", "Keep your body in a straight plank line, lower chest toward the floor, and push back up.", "Chest & Triceps", "Upper body", "No equipment", 3, "12 reps", 60));
        catalog.add(new Exercise("Bodyweight Squats", "Stand shoulder-width apart, push hips back, lower until thighs are parallel to floor.", "Quads & Glutes", "Lower body", "No equipment", 3, "15 reps", 60));
        catalog.add(new Exercise("Forearm Plank", "Hold a rigid straight line from head to heels on your forearms, bracing your core tight.", "Core & Abs", "Core", "No equipment", 3, "45 sec", 45));
        catalog.add(new Exercise("Jumping Jacks", "Jump feet out wide while raising arms overhead, then return to start at a brisk pace.", "Full Body", "Cardio", "No equipment", 3, "45 sec", 30));
        catalog.add(new Exercise("Reverse Lunges", "Step backward with one leg, lower back knee near floor, and drive through front heel.", "Legs & Glutes", "Lower body", "No equipment", 3, "12 / leg", 60));
        catalog.add(new Exercise("Mountain Climbers", "In high plank position, drive knees alternately toward your chest at a fast tempo.", "Core & Cardio", "Cardio", "No equipment", 3, "30 sec", 45));
        catalog.add(new Exercise("Pike Push-Ups", "Form an inverted V with hips high and lower the top of your head toward the floor.", "Shoulders", "Upper body", "No equipment", 3, "10 reps", 60));
        catalog.add(new Exercise("Glute Bridges", "Lie on back with knees bent, drive through heels to lift hips and squeeze glutes at top.", "Glutes & Hamstrings", "Lower body", "No equipment", 3, "15 reps", 45));
        catalog.add(new Exercise("Bicycle Crunches", "Lie flat, bring opposite elbow to opposite knee in a controlled pedaling motion.", "Obliques & Abs", "Core", "No equipment", 3, "20 reps", 45));
        catalog.add(new Exercise("Burpees", "Squat down, kick feet into plank, perform a push-up, jump feet in, and leap upward.", "Full Body", "Full body", "No equipment", 3, "10 reps", 60));

        // --- DUMBBELLS ---
        catalog.add(new Exercise("Dumbbell Goblet Squats", "Hold one dumbbell vertically against your chest and squat deep with an upright torso.", "Quads & Core", "Strength training", "Dumbbells", 4, "12 reps", 75));
        catalog.add(new Exercise("Dumbbell Bench / Floor Press", "Press dumbbells directly above chest until arms are extended, then lower with control.", "Chest & Triceps", "Upper body", "Dumbbells", 4, "10 reps", 75));
        catalog.add(new Exercise("Bent-Over Dumbbell Rows", "Hinge at hips with a flat back and pull dumbbells toward your lower ribs.", "Upper Back & Lats", "Upper body", "Dumbbells", 4, "12 reps", 60));
        catalog.add(new Exercise("Dumbbell Shoulder Press", "Press dumbbells overhead from shoulder height without arching your lower back.", "Shoulders", "Strength training", "Dumbbells", 3, "10 reps", 75));
        catalog.add(new Exercise("Dumbbell Romanian Deadlifts", "Keep knees slightly bent, hinge hips back lowering dumbbells along shins, feel hamstring stretch.", "Hamstrings & Back", "Lower body", "Dumbbells", 4, "12 reps", 75));
        catalog.add(new Exercise("Dumbbell Thrusters", "Perform a front squat with dumbbells at shoulders and press overhead in one fluid motion.", "Full Body", "Full body", "Dumbbells", 3, "12 reps", 60));
        catalog.add(new Exercise("Russian Twists (Weighted)", "Sit leaned back slightly, rotate torso side to side holding a light dumbbell.", "Obliques & Core", "Core", "Dumbbells", 3, "20 twists", 45));

        // --- RESISTANCE BANDS ---
        catalog.add(new Exercise("Banded Chest Press", "Anchor band behind back and press handles forward until arms are fully extended.", "Chest", "Upper body", "Resistance bands", 3, "15 reps", 45));
        catalog.add(new Exercise("Banded Seated Rows", "Loop band around feet while seated and pull handles toward your torso squeezing shoulder blades.", "Back & Biceps", "Upper body", "Resistance bands", 3, "15 reps", 45));
        catalog.add(new Exercise("Banded Lateral Walks", "Place loop band above knees, drop into a quarter squat, and step side to side.", "Glutes & Hips", "Lower body", "Resistance bands", 3, "15 / side", 45));
        catalog.add(new Exercise("Banded Pallof Press", "Anchor band to the side and press straight out in front of chest, resisting rotation.", "Deep Core", "Core", "Resistance bands", 3, "12 / side", 45));

        // --- GYM EQUIPMENT / FULL HOME WORKOUT ---
        catalog.add(new Exercise("Barbell Back Squats", "Rest barbell across upper back, brace core, squat below parallel, and drive upward.", "Legs & Core", "Strength training", "Gym equipment", 4, "8 reps", 90));
        catalog.add(new Exercise("Lat Pulldowns", "Grip wide bar on cable machine and pull down to upper chest with controlled tempo.", "Lats & Upper Back", "Upper body", "Gym equipment", 4, "10 reps", 75));
        catalog.add(new Exercise("Cable Rope Tricep Pushdowns", "Keep elbows tucked at sides and extend cable rope downward until arms lock out.", "Triceps", "Upper body", "Gym equipment", 3, "12 reps", 60));
        catalog.add(new Exercise("Leg Press Machine", "Place feet shoulder-width on sled, lower knees toward chest, and press without locking knees.", "Quads & Glutes", "Lower body", "Gym equipment", 4, "12 reps", 75));
        catalog.add(new Exercise("Treadmill HIIT Sprints", "Alternate 30 seconds of fast running with 30 seconds of brisk walking.", "Cardiovascular", "Cardio", "Gym equipment", 5, "60 sec", 30));
        catalog.add(new Exercise("Pull-Ups", "Hang from pull-up bar with overhand grip and pull chin cleanly above the bar.", "Back & Biceps", "Full body", "Full home workout equipment", 3, "8 reps", 75));

        return catalog;
    }

    // Check if user's available equipment allows an exercise
    private static boolean isEquipmentAvailable(String userEquipment, String requiredEquipment) {
        if (requiredEquipment.equalsIgnoreCase("No equipment")) {
            return true; // Everyone can do bodyweight exercises
        }
        if (userEquipment == null) {
            return false;
        }
        switch (userEquipment) {
            case "Gym equipment":
                return true; // Full access
            case "Full home workout equipment":
                return !requiredEquipment.equalsIgnoreCase("Gym equipment");
            case "Dumbbells":
                return requiredEquipment.equalsIgnoreCase("Dumbbells");
            case "Resistance bands":
                return requiredEquipment.equalsIgnoreCase("Resistance bands");
            case "No equipment":
            default:
                return false;
        }
    }

    // Get recommended category for a given day of week
    public static String getCategoryForDay(String dayOfWeek, String fitnessGoal) {
        switch (dayOfWeek) {
            case "Mon":
                return "Upper body";
            case "Tue":
                return "Lower body";
            case "Wed":
                return fitnessGoal != null && fitnessGoal.equalsIgnoreCase("Weight loss") ? "Cardio" : "Core";
            case "Thu":
                return fitnessGoal != null && fitnessGoal.equalsIgnoreCase("Strength improvement") ? "Strength training" : "Full body";
            case "Fri":
                return "Upper body";
            case "Sat":
                return "Lower body";
            case "Sun":
            default:
                return "Core"; // Active recovery & core
        }
    }

    // Generate personalized exercise list tailored to User Profile + Category
    public static List<Exercise> generateWorkout(User user, String category) {
        String userEquipment = (user != null && user.getEquipment() != null) ? user.getEquipment() : "No equipment";
        String userLevel = (user != null && user.getFitnessLevel() != null) ? user.getFitnessLevel() : "Beginner";
        String userGoal = (user != null && user.getFitnessGoal() != null) ? user.getFitnessGoal() : "General fitness";
        int userAge = (user != null && user.getAge() > 0) ? user.getAge() : 25;

        List<Exercise> filtered = new ArrayList<>();

        for (Exercise ex : getExerciseCatalog()) {
            // Strictly enforce equipment constraint
            if (!isEquipmentAvailable(userEquipment, ex.getEquipmentRequired())) {
                continue;
            }

            // Match category (or allow mixed Full body / Strength)
            boolean matchesCategory = category.equalsIgnoreCase("All")
                    || ex.getCategory().equalsIgnoreCase(category)
                    || (category.equalsIgnoreCase("Full body") && !ex.getCategory().equalsIgnoreCase("Cardio"));

            if (matchesCategory) {
                Exercise customized = customizeExercise(ex, userLevel, userGoal, userAge);
                filtered.add(customized);
            }
        }

        // Limit to a clean 5-6 exercise session so workouts are focused and actionable
        if (filtered.size() > 6) {
            return new ArrayList<>(filtered.subList(0, 6));
        }

        // Fallback if a narrow category + equipment had fewer than 3 exercises
        if (filtered.size() < 3) {
            for (Exercise ex : getExerciseCatalog()) {
                if (isEquipmentAvailable(userEquipment, ex.getEquipmentRequired()) && !containsExercise(filtered, ex.getName())) {
                    filtered.add(customizeExercise(ex, userLevel, userGoal, userAge));
                    if (filtered.size() >= 4) break;
                }
            }
        }

        return filtered;
    }

    private static boolean containsExercise(List<Exercise> list, String name) {
        for (Exercise e : list) {
            if (e.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    // Adjust sets, reps, and rest based on Fitness Level, Goal, and Age
    private static Exercise customizeExercise(Exercise base, String level, String goal, int age) {
        int sets = base.getSets();
        String reps = base.getReps();
        int rest = base.getRestSeconds();

        // 1. Adjust by Fitness Level
        if (level.equalsIgnoreCase("Beginner")) {
            sets = Math.max(2, sets - 1);
            rest += 15;
        } else if (level.equalsIgnoreCase("Advanced")) {
            sets += 1;
            rest = Math.max(30, rest - 10);
        }

        // 2. Adjust by Fitness Goal
        if (goal.equalsIgnoreCase("Strength improvement")) {
            if (reps.contains("reps")) {
                reps = "6-8 heavy reps";
            }
            rest += 15;
        } else if (goal.equalsIgnoreCase("Endurance improvement") || goal.equalsIgnoreCase("Weight loss")) {
            if (reps.contains("reps")) {
                reps = "15-18 reps";
            }
            rest = Math.max(30, rest - 15);
        } else if (goal.equalsIgnoreCase("Muscle building")) {
            if (reps.contains("reps")) {
                reps = "10-12 controlled reps";
            }
        }

        // 3. Age safety adjustment
        if (age >= 55) {
            rest += 15;
        }

        return new Exercise(
                base.getName(),
                base.getDescription(),
                base.getMuscleGroup(),
                base.getCategory(),
                base.getEquipmentRequired(),
                sets,
                reps,
                rest
        );
    }

    // Estimate total workout duration in minutes
    public static int estimateDurationMinutes(List<Exercise> exercises) {
        if (exercises == null || exercises.isEmpty()) return 15;
        int totalSeconds = 0;
        for (Exercise ex : exercises) {
            // ~40 seconds active work per set + rest period between sets
            totalSeconds += ex.getSets() * (40 + ex.getRestSeconds());
        }
        return Math.max(10, totalSeconds / 60);
    }
}