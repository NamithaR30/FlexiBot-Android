package com.example.flexibot.workers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.flexibot.utils.NotificationHelper;

public class WeightReminderWorker extends Worker {

    public WeightReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("FlexiBotPrefs", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("weight_reminder_enabled", false);

        if (enabled) {
            NotificationHelper.showReminderNotification(
                    context,
                    NotificationHelper.NOTIF_ID_WEIGHT,
                    "Weekly Weight Check-In",
                    "Step on the scale and log your weekly progress in FlexiBot's Weight Tracker."
            );
        }
        return Result.success();
    }
}