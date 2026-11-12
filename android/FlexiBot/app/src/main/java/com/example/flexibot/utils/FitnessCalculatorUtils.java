package com.example.flexibot.utils;

public class FitnessCalculatorUtils {

    // 1. BMI = Weight (kg) / Height (m)^2
    public static double calculateBMI(double weightKg, double heightCm) {
        if (weightKg <= 0 || heightCm <= 0) return 0.0;
        double heightMeters = heightCm / 100.0;
        return weightKg / (heightMeters * heightMeters);
    }

    public static String getBMICategory(double bmi) {
        if (bmi <= 0) return "Invalid Input";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal Weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    // Maps BMI range [15.0 .. 40.0] to a [0 .. 100] progress indicator
    public static int getBMIIndicatorProgress(double bmi) {
        if (bmi <= 15.0) return 5;
        if (bmi >= 40.0) return 100;
        return (int) (((bmi - 15.0) / 25.0) * 100.0);
    }

    // 2. Mifflin-St Jeor Equation for Basal Metabolic Rate (BMR)
    public static double calculateBMR(double weightKg, double heightCm, int age, String gender) {
        if (weightKg <= 0 || heightCm <= 0 || age <= 0) return 0.0;

        double base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age);
        if ("Male".equalsIgnoreCase(gender)) {
            return base + 5.0;
        } else if ("Female".equalsIgnoreCase(gender)) {
            return base - 161.0;
        } else {
            // Neutral midpoint for "Other"
            return base - 78.0;
        }
    }

    public static double getActivityMultiplier(String activityLevel) {
        if (activityLevel == null) return 1.375;
        if (activityLevel.startsWith("Sedentary")) return 1.2;
        if (activityLevel.startsWith("Lightly Active")) return 1.375;
        if (activityLevel.startsWith("Moderately Active")) return 1.55;
        if (activityLevel.startsWith("Very Active")) return 1.725;
        if (activityLevel.startsWith("Extra Active")) return 1.9;
        return 1.375;
    }

    public static int calculateDailyCalories(double bmr, String activityLevel, String fitnessGoal) {
        if (bmr <= 0) return 0;
        double tdee = bmr * getActivityMultiplier(activityLevel);

        if (fitnessGoal != null) {
            switch (fitnessGoal) {
                case "Weight loss":
                    tdee -= 500.0;
                    break;
                case "Muscle building":
                    tdee += 300.0;
                    break;
                case "Strength improvement":
                    tdee += 200.0;
                    break;
                case "Endurance improvement":
                    tdee += 150.0;
                    break;
                case "Weight maintenance":
                case "General fitness":
                default:
                    break;
            }
        }

        // Safety floor for daily calorie estimation
        return (int) Math.max(1200, Math.round(tdee));
    }

    public static String getCalorieGoalExplanation(String fitnessGoal) {
        if (fitnessGoal == null) return "Maintenance intake estimate";
        switch (fitnessGoal) {
            case "Weight loss":
                return "Includes a ~500 kcal daily deficit for steady fat loss";
            case "Muscle building":
                return "Includes a ~300 kcal lean surplus to support muscle growth";
            case "Strength improvement":
                return "Includes a ~200 kcal surplus to fuel heavy strength training";
            case "Endurance improvement":
                return "Includes a ~150 kcal boost to sustain aerobic energy";
            default:
                return "Balanced maintenance intake for your current activity level";
        }
    }

    // 3. Daily Protein Range in grams [minGrams, maxGrams]
    public static int[] calculateProteinRange(double weightKg, String fitnessGoal, String activityLevel) {
        if (weightKg <= 0) return new int[]{0, 0};

        double minFactor = 1.1;
        double maxFactor = 1.5;

        if (fitnessGoal != null) {
            switch (fitnessGoal) {
                case "Muscle building":
                    minFactor = 1.6;
                    maxFactor = 2.2;
                    break;
                case "Weight loss":
                    // Higher protein helps preserve lean muscle during a calorie deficit
                    minFactor = 1.6;
                    maxFactor = 2.0;
                    break;
                case "Strength improvement":
                    minFactor = 1.6;
                    maxFactor = 2.1;
                    break;
                case "Endurance improvement":
                    minFactor = 1.2;
                    maxFactor = 1.6;
                    break;
                case "Weight maintenance":
                case "General fitness":
                default:
                    minFactor = 1.0;
                    maxFactor = 1.4;
                    break;
            }
        }

        if (activityLevel != null && (activityLevel.startsWith("Very Active") || activityLevel.startsWith("Extra Active"))) {
            minFactor += 0.1;
            maxFactor += 0.1;
        }

        int minGrams = (int) Math.round(weightKg * minFactor);
        int maxGrams = (int) Math.round(weightKg * maxFactor);
        return new int[]{minGrams, maxGrams};
    }

    // 4. Daily Water Requirement in Millilitres (ml)
    public static int calculateWaterIntakeMl(double weightKg, String activityLevel) {
        if (weightKg <= 0) return 2000;

        // Baseline: ~35 ml per kg of body weight
        double waterMl = weightKg * 35.0;

        if (activityLevel != null) {
            if (activityLevel.startsWith("Lightly Active")) {
                waterMl += 300;
            } else if (activityLevel.startsWith("Moderately Active")) {
                waterMl += 500;
            } else if (activityLevel.startsWith("Very Active")) {
                waterMl += 750;
            } else if (activityLevel.startsWith("Extra Active")) {
                waterMl += 1000;
            }
        }

        // Round to nearest 50 ml for a clean target
        return (int) (Math.round(waterMl / 50.0) * 50);
    }

    // Helper to map user's saved FitnessLevel to a default ActivityLevel
    public static String mapFitnessLevelToActivity(String fitnessLevel) {
        if ("Advanced".equalsIgnoreCase(fitnessLevel)) {
            return "Very Active (6–7 days/week)";
        } else if ("Intermediate".equalsIgnoreCase(fitnessLevel)) {
            return "Moderately Active (3–5 days/week)";
        } else {
            return "Lightly Active (1–3 days/week)";
        }
    }
}