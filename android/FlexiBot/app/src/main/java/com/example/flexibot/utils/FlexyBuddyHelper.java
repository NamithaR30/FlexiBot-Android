package com.example.flexibot.utils;

import com.example.flexibot.R;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class FlexyBuddyHelper {

    public enum Mood {
        IDLE,
        HAPPY,
        EXCITED,
        ENCOURAGING,
        THINKING,
        CELEBRATING
    }

    public static class BuddyState {
        public final Mood mood;
        public final String moodLabel;
        public final String message;
        public final int avatarResId;

        public BuddyState(Mood mood, String moodLabel, String message, int avatarResId) {
            this.mood = mood;
            this.moodLabel = moodLabel;
            this.message = message;
            this.avatarResId = avatarResId;
        }
    }

    public static int getAvatarForMood(Mood mood) {
        if (mood == null) return R.drawable.ic_flexy_avatar;
        switch (mood) {
            case HAPPY:
                return R.drawable.ic_flexy_hydrated;
            case EXCITED:
                return R.drawable.ic_flexy_hyped;
            case THINKING:
                return R.drawable.ic_flexy_thinking;
            case CELEBRATING:
                return R.drawable.ic_flexy_celebrating;
            case ENCOURAGING:
            case IDLE:
            default:
                return R.drawable.ic_flexy_avatar;
        }
    }

    public static BuddyState getCurrentBuddyState(
            String userName,
            String fitnessGoal,
            int workoutsToday,
            int waterTodayMl,
            int waterGoalMl
    ) {
        String name = (userName != null && !userName.trim().isEmpty()) ? userName.trim() : "Champ";
        String goal = (fitnessGoal != null && !fitnessGoal.trim().isEmpty()) ? fitnessGoal : "General fitness";
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int safeGoal = Math.max(500, waterGoalMl);
        int waterPct = (waterTodayMl * 100) / safeGoal;
        int remainingWater = Math.max(0, safeGoal - waterTodayMl);

        // Calculate expected daytime hydration pace between 8 AM and 9 PM
        int expectedWaterPct = 0;
        if (hour >= 9 && hour <= 21) {
            expectedWaterPct = Math.min(100, ((hour - 8) * 100) / 13);
        } else if (hour > 21) {
            expectedWaterPct = 100;
        }

        // 1. Both workout & hydration goal hit -> CELEBRATING
        if (workoutsToday > 0 && waterPct >= 100) {
            return new BuddyState(
                    Mood.CELEBRATING,
                    "FLEXY • CELEBRATING",
                    String.format(
                            Locale.getDefault(),
                            "Double milestone, %s! Workout done AND %,d / %,d ml hydrated (100%%)!",
                            name, waterTodayMl, safeGoal
                    ),
                    getAvatarForMood(Mood.CELEBRATING)
            );
        }

        // 2. Daily hydration goal reached -> CELEBRATING
        if (waterPct >= 100) {
            return new BuddyState(
                    Mood.CELEBRATING,
                    "FLEXY • HYDRATION GOAL HIT!",
                    String.format(
                            Locale.getDefault(),
                            "100%% hydrated (%,d / %,d ml), %s! Your muscles are fueled—ready to train?",
                            waterTodayMl, safeGoal, name
                    ),
                    getAvatarForMood(Mood.CELEBRATING)
            );
        }

        // 3. Workout completed today -> EXCITED
        if (workoutsToday > 0) {
            return new BuddyState(
                    Mood.EXCITED,
                    "FLEXY • EXCITED",
                    String.format(
                            Locale.getDefault(),
                            "Workout crushed, %s! Drink %,d ml more water to finish today's %,d ml hydration goal!",
                            name, remainingWater, safeGoal
                    ),
                    getAvatarForMood(Mood.EXCITED)
            );
        }

        // 4. Falling behind daily hydration target -> ENCOURAGING
        if (hour >= 10 && waterPct + 15 < expectedWaterPct) {
            return new BuddyState(
                    Mood.ENCOURAGING,
                    "FLEXY • HYDRATION BOOST",
                    String.format(
                            Locale.getDefault(),
                            "You're behind on water, %s (%,d / %,d ml • %d%%). Grab a 250 ml glass now!",
                            name, waterTodayMl, safeGoal, waterPct
                    ),
                    getAvatarForMood(Mood.ENCOURAGING)
            );
        }

        // 5. On track with water intake -> HAPPY
        if (waterTodayMl > 0) {
            return new BuddyState(
                    Mood.HAPPY,
                    "FLEXY • HAPPY",
                    String.format(
                            Locale.getDefault(),
                            "Nice hydration pace, %s! %,d / %,d ml logged (%d%%)—only %,d ml to go!",
                            name, waterTodayMl, safeGoal, waterPct, remainingWater
                    ),
                    getAvatarForMood(Mood.HAPPY)
            );
        }

        // 6. Default relaxed state -> IDLE
        return new BuddyState(
                Mood.IDLE,
                "FLEXY • READY",
                String.format(
                        Locale.getDefault(),
                        "Hey %s! Start with 250 ml of water toward your %,d ml target, then let's train for %s!",
                        name, safeGoal, goal.toLowerCase(Locale.getDefault())
                ),
                getAvatarForMood(Mood.IDLE)
        );
    }

    public static BuddyState getWaterLoggedReaction(int addedMl, int newTotalMl, int waterGoalMl) {
        int safeGoal = Math.max(500, waterGoalMl);
        int pct = Math.min(100, (newTotalMl * 100) / safeGoal);
        int remaining = Math.max(0, safeGoal - newTotalMl);

        if (newTotalMl >= safeGoal) {
            return new BuddyState(
                    Mood.CELEBRATING,
                    "FLEXY • HYDRATION GOAL REACHED!",
                    String.format(
                            Locale.getDefault(),
                            "+%d ml logged! You hit %,d / %,d ml (100%%) today—amazing hydration!",
                            addedMl, newTotalMl, safeGoal
                    ),
                    getAvatarForMood(Mood.CELEBRATING)
            );
        }

        return new BuddyState(
                Mood.HAPPY,
                "FLEXY • HYDRATED & HAPPY",
                String.format(
                        Locale.getDefault(),
                        "+%d ml logged! You're at %,d / %,d ml (%d%%)—%,d ml left for today!",
                        addedMl, newTotalMl, safeGoal, pct, remaining
                ),
                getAvatarForMood(Mood.HAPPY)
        );
    }

    public static String getRandomBuddyTip(String userName, String fitnessGoal) {
        String name = (userName != null && !userName.trim().isEmpty()) ? userName.trim() : "Champ";
        String goal = (fitnessGoal != null && !fitnessGoal.trim().isEmpty()) ? fitnessGoal : "General fitness";

        String[] tips = new String[]{
                "Form over ego weight, " + name + "! Slow, controlled reps build real strength.",
                "Consistency beats intensity—showing up even on busy days makes you unstoppable!",
                "Don't skip your warm-up! 2 minutes of mobility keeps your joints happy.",
                "Staying hydrated boosts workout stamina by up to 15%. Grab a quick sip!",
                "Focused on " + goal + "? Ask me in AI Chat anytime for a custom routine or meal idea!"
        };

        return tips[new Random().nextInt(tips.length)];
    }

    public static BuddyState getWorkoutCoachingCue(String exerciseName, boolean isResting) {
        String safeExercise = (exerciseName != null && !exerciseName.trim().isEmpty())
                ? exerciseName.trim()
                : "this exercise";

        if (isResting) {
            return new BuddyState(
                    Mood.HAPPY,
                    "FLEXY • REST & HYDRATE",
                    "Catch your breath and take a sip of water! Up next: " + safeExercise + ".",
                    getAvatarForMood(Mood.HAPPY)
            );
        }

        return new BuddyState(
                Mood.ENCOURAGING,
                "FLEXY • FORM CUE",
                "Focus on smooth, controlled reps on " + safeExercise + "—you've got this!",
                getAvatarForMood(Mood.ENCOURAGING)
        );
    }

    public static BuddyState getWorkoutCelebration(String userName, int exercisesCompleted, int durationMinutes) {
        String name = (userName != null && !userName.trim().isEmpty()) ? userName.trim() : "Champ";
        return new BuddyState(
                Mood.EXCITED,
                "FLEXY • WORKOUT CRUSHED!",
                String.format(
                        Locale.getDefault(),
                        "Boom, %s! You finished %d exercises in %d min. Your streak just leveled up!",
                        name, exercisesCompleted, Math.max(1, durationMinutes)
                ),
                getAvatarForMood(Mood.EXCITED)
        );
    }
}