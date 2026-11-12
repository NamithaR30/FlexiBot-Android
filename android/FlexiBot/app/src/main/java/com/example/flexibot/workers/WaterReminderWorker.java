package com.example.flexibot.workers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.flexibot.utils.NotificationHelper;

import java.util.Calendar;
import java.util.Locale;

public class WaterReminderWorker extends Worker {

    public WaterReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("FlexiBotPrefs", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("water_reminder_enabled", false);

        if (!enabled) {
            return Result.success();
        }

        // Only send hydration alerts during waking hours (8:00 AM - 9:00 PM)
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (currentHour < 8 || currentHour > 21) {
            return Result.success();
        }

        int goalMl = prefs.getInt("daily_water_goal_ml", 2500);
        String msg = String.format(
                Locale.getDefault(),
                "Drink a glass of water (250 ml) to stay on track toward your %,d ml daily goal!",
                goalMl
        );

        NotificationHelper.showReminderNotification(
                context,
                NotificationHelper.NOTIF_ID_WATER,
                "Hydration Check-In",
                msg
        );

        return Result.success();
    }
}