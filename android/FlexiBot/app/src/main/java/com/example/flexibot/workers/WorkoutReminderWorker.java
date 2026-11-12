package com.example.flexibot.workers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.flexibot.utils.NotificationHelper;

public class WorkoutReminderWorker extends Worker {

    public WorkoutReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("FlexiBotPrefs", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("workout_reminder_enabled", false);

        if (enabled) {
            NotificationHelper.showReminderNotification(
                    context,
                    NotificationHelper.NOTIF_ID_WORKOUT,
                    "Time to Train with FlexiBot!",
                    "Your daily workout session is waiting. Open FlexiBot to crush today's training!"
            );
        }
        return Result.success();
    }
}