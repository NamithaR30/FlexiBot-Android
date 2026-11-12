package com.example.flexibot.utils;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.flexibot.workers.WaterReminderWorker;
import com.example.flexibot.workers.WeightReminderWorker;
import com.example.flexibot.workers.WorkoutReminderWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {

    private static final String WORK_DAILY_WORKOUT = "flexibot_daily_workout_reminder";
    private static final String WORK_PERIODIC_WATER = "flexibot_periodic_water_reminder";
    private static final String WORK_WEEKLY_WEIGHT = "flexibot_weekly_weight_reminder";

    public static void scheduleDailyWorkoutReminder(Context context, int hourOfDay, int minute) {
        long initialDelayMs = calculateInitialDelayMillis(hourOfDay, minute);

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                WorkoutReminderWorker.class,
                24, TimeUnit.HOURS
        )
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_DAILY_WORKOUT,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }

    public static void cancelDailyWorkoutReminder(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_DAILY_WORKOUT);
    }

    public static void scheduleWaterReminder(Context context, int intervalHours) {
        int safeHours = Math.max(1, intervalHours);

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                WaterReminderWorker.class,
                safeHours, TimeUnit.HOURS
        )
                .setInitialDelay(safeHours, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_PERIODIC_WATER,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }

    public static void cancelWaterReminder(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_PERIODIC_WATER);
    }

    public static void scheduleWeeklyWeightReminder(Context context, int hourOfDay, int minute) {
        long initialDelayMs = calculateInitialDelayMillis(hourOfDay, minute);

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                WeightReminderWorker.class,
                7, TimeUnit.DAYS
        )
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_WEEKLY_WEIGHT,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }

    public static void cancelWeeklyWeightReminder(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_WEEKLY_WEIGHT);
    }

    private static long calculateInitialDelayMillis(int hourOfDay, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, hourOfDay);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1);
        }
        return target.getTimeInMillis() - now.getTimeInMillis();
    }
}