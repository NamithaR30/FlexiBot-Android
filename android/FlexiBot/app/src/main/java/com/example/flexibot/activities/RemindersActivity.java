package com.example.flexibot.activities;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.example.flexibot.R;
import com.example.flexibot.utils.NotificationHelper;
import com.example.flexibot.utils.ReminderScheduler;

import java.util.Locale;

public class RemindersActivity extends AppCompatActivity {

    private SwitchCompat switchWorkout, switchWater, switchWeight;
    private Button btnPickWorkoutTime, btnPickWeightTime, btnTestNotification;
    private TextView chipWater1Hr, chipWater2Hr, chipWater3Hr;

    private SharedPreferences prefs;

    private int workoutHour = 18;
    private int workoutMinute = 0;
    private int waterIntervalHours = 2;
    private int weightHour = 8;
    private int weightMinute = 0;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Notification permission denied. Please allow notifications in Settings.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        NotificationHelper.createNotificationChannel(this);
        prefs = getSharedPreferences("FlexiBotPrefs", MODE_PRIVATE);

        initViews();
        loadSavedSettings();
        setupListeners();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBackReminders);
        btnBack.setOnClickListener(v -> finish());

        switchWorkout = findViewById(R.id.switchWorkoutReminder);
        switchWater = findViewById(R.id.switchWaterReminder);
        switchWeight = findViewById(R.id.switchWeightReminder);

        btnPickWorkoutTime = findViewById(R.id.btnPickWorkoutTime);
        btnPickWeightTime = findViewById(R.id.btnPickWeightTime);
        btnTestNotification = findViewById(R.id.btnSendTestNotification);

        chipWater1Hr = findViewById(R.id.chipWater1Hr);
        chipWater2Hr = findViewById(R.id.chipWater2Hr);
        chipWater3Hr = findViewById(R.id.chipWater3Hr);
    }

    private void loadSavedSettings() {
        boolean workoutEnabled = prefs.getBoolean("workout_reminder_enabled", false);
        workoutHour = prefs.getInt("workout_reminder_hour", 18);
        workoutMinute = prefs.getInt("workout_reminder_minute", 0);

        boolean waterEnabled = prefs.getBoolean("water_reminder_enabled", false);
        waterIntervalHours = prefs.getInt("water_reminder_interval_hours", 2);

        boolean weightEnabled = prefs.getBoolean("weight_reminder_enabled", false);
        weightHour = prefs.getInt("weight_reminder_hour", 8);
        weightMinute = prefs.getInt("weight_reminder_minute", 0);

        switchWorkout.setChecked(workoutEnabled);
        btnPickWorkoutTime.setText(formatTime12Hour(workoutHour, workoutMinute));

        switchWater.setChecked(waterEnabled);
        updateWaterIntervalChips(waterIntervalHours);

        switchWeight.setChecked(weightEnabled);
        btnPickWeightTime.setText(formatTime12Hour(weightHour, weightMinute));
    }

    private void setupListeners() {
        // 1. Daily Workout Reminder
        switchWorkout.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ensureNotificationPermission();
                prefs.edit().putBoolean("workout_reminder_enabled", true).apply();
                ReminderScheduler.scheduleDailyWorkoutReminder(this, workoutHour, workoutMinute);
                Toast.makeText(this, "Daily workout reminder set for " + formatTime12Hour(workoutHour, workoutMinute), Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit().putBoolean("workout_reminder_enabled", false).apply();
                ReminderScheduler.cancelDailyWorkoutReminder(this);
                Toast.makeText(this, "Daily workout reminder turned off", Toast.LENGTH_SHORT).show();
            }
        });

        btnPickWorkoutTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                workoutHour = hourOfDay;
                workoutMinute = minute;
                btnPickWorkoutTime.setText(formatTime12Hour(workoutHour, workoutMinute));

                prefs.edit()
                        .putInt("workout_reminder_hour", workoutHour)
                        .putInt("workout_reminder_minute", workoutMinute)
                        .apply();

                if (switchWorkout.isChecked()) {
                    ReminderScheduler.scheduleDailyWorkoutReminder(this, workoutHour, workoutMinute);
                    Toast.makeText(this, "Workout reminder updated to " + formatTime12Hour(workoutHour, workoutMinute), Toast.LENGTH_SHORT).show();
                }
            }, workoutHour, workoutMinute, false).show();
        });

        // 2. Hydration Reminder
        switchWater.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ensureNotificationPermission();
                prefs.edit().putBoolean("water_reminder_enabled", true).apply();
                ReminderScheduler.scheduleWaterReminder(this, waterIntervalHours);
                Toast.makeText(this, "Hydration reminders active (every " + waterIntervalHours + " hr)", Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit().putBoolean("water_reminder_enabled", false).apply();
                ReminderScheduler.cancelWaterReminder(this);
                Toast.makeText(this, "Hydration reminders turned off", Toast.LENGTH_SHORT).show();
            }
        });

        chipWater1Hr.setOnClickListener(v -> selectWaterInterval(1));
        chipWater2Hr.setOnClickListener(v -> selectWaterInterval(2));
        chipWater3Hr.setOnClickListener(v -> selectWaterInterval(3));

        // 3. Weekly Weight Reminder
        switchWeight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ensureNotificationPermission();
                prefs.edit().putBoolean("weight_reminder_enabled", true).apply();
                ReminderScheduler.scheduleWeeklyWeightReminder(this, weightHour, weightMinute);
                Toast.makeText(this, "Weekly weight check-in set for " + formatTime12Hour(weightHour, weightMinute), Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit().putBoolean("weight_reminder_enabled", false).apply();
                ReminderScheduler.cancelWeeklyWeightReminder(this);
                Toast.makeText(this, "Weekly weight reminder turned off", Toast.LENGTH_SHORT).show();
            }
        });

        btnPickWeightTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                weightHour = hourOfDay;
                weightMinute = minute;
                btnPickWeightTime.setText(formatTime12Hour(weightHour, weightMinute));

                prefs.edit()
                        .putInt("weight_reminder_hour", weightHour)
                        .putInt("weight_reminder_minute", weightMinute)
                        .apply();

                if (switchWeight.isChecked()) {
                    ReminderScheduler.scheduleWeeklyWeightReminder(this, weightHour, weightMinute);
                    Toast.makeText(this, "Weight reminder updated to " + formatTime12Hour(weightHour, weightMinute), Toast.LENGTH_SHORT).show();
                }
            }, weightHour, weightMinute, false).show();
        });

        // 4. Instant Test Notification
        btnTestNotification.setOnClickListener(v -> {
            ensureNotificationPermission();
            NotificationHelper.showReminderNotification(
                    this,
                    NotificationHelper.NOTIF_ID_TEST,
                    "FlexiBot Reminder Active!",
                    "Notifications are working properly. Stay hydrated and crush your next workout!"
            );
            Toast.makeText(this, "Test notification sent!", Toast.LENGTH_SHORT).show();
        });
    }

    private void selectWaterInterval(int hours) {
        waterIntervalHours = hours;
        updateWaterIntervalChips(hours);
        prefs.edit().putInt("water_reminder_interval_hours", hours).apply();

        if (switchWater.isChecked()) {
            ReminderScheduler.scheduleWaterReminder(this, hours);
            Toast.makeText(this, "Hydration interval set to every " + hours + " hr", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWaterIntervalChips(int hours) {
        TextView[] chips = {chipWater1Hr, chipWater2Hr, chipWater3Hr};
        int[] values = {1, 2, 3};
        for (int i = 0; i < chips.length; i++) {
            boolean selected = (values[i] == hours);
            chips[i].setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            chips[i].setTextColor(ContextCompat.getColor(this, selected ? R.color.white : R.color.grey_text));
        }
    }

    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private String formatTime12Hour(int hourOfDay, int minute) {
        String amPm = hourOfDay >= 12 ? "PM" : "AM";
        int hour12 = hourOfDay % 12;
        if (hour12 == 0) hour12 = 12;
        return String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm);
    }
}