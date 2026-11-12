package com.example.flexibot.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.flexibot.R;
import com.example.flexibot.activities.CalculatorsActivity;
import com.example.flexibot.activities.RemindersActivity;
import com.example.flexibot.activities.TrackersActivity;
import com.example.flexibot.models.User;
import com.example.flexibot.models.WaterLog;
import com.example.flexibot.models.WorkoutSession;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvGoal, tvWeight, tvStreak, tvWorkoutLabel, tvWaterLabel;
    private ProgressBar pbWorkoutProgress, pbWaterProgress;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvGreeting = view.findViewById(R.id.tvHomeGreeting);
        tvGoal = view.findViewById(R.id.tvHomeGoal);
        tvWeight = view.findViewById(R.id.tvHomeWeight);
        tvStreak = view.findViewById(R.id.tvHomeStreak);
        tvWorkoutLabel = view.findViewById(R.id.tvHomeWorkoutLabel);
        tvWaterLabel = view.findViewById(R.id.tvHomeWaterLabel);
        pbWorkoutProgress = view.findViewById(R.id.pbWorkoutProgress);
        pbWaterProgress = view.findViewById(R.id.pbWaterProgress);

        CardView cardAIChat = view.findViewById(R.id.cardAIChat);
        CardView cardWorkouts = view.findViewById(R.id.cardWorkouts);
        CardView cardTrackers = view.findViewById(R.id.cardTrackers);
        CardView cardCalculators = view.findViewById(R.id.cardCalculators);
        CardView cardDashboardQuick = view.findViewById(R.id.cardDashboardQuick);
        CardView cardReminders = view.findViewById(R.id.cardReminders);

        cardAIChat.setOnClickListener(v -> switchTab(R.id.nav_chat));
        cardWorkouts.setOnClickListener(v -> switchTab(R.id.nav_workouts));
        cardTrackers.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), TrackersActivity.class)));
        cardCalculators.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), CalculatorsActivity.class)));
        cardDashboardQuick.setOnClickListener(v -> switchTab(R.id.nav_dashboard));
        cardReminders.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), RemindersActivity.class)));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
        loadWorkoutProgress();
        loadDailyWaterProgress();
    }

    private void switchTab(int menuItemId) {
        if (getActivity() != null) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(menuItemId);
            }
        }
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            tvGreeting.setText("Welcome back, " + user.getName() + "!");
                            tvGoal.setText(user.getFitnessGoal());
                            tvWeight.setText(user.getWeight() + " kg");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadWorkoutProgress() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        Calendar startOfDay = Calendar.getInstance();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);
        long todayStartMillis = startOfDay.getTimeInMillis();

        db.collection("users").document(uid).collection("workouts").get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    int totalSessions = snapshots.size();
                    int completedTodayCount = 0;

                    for (DocumentSnapshot doc : snapshots) {
                        WorkoutSession session = doc.toObject(WorkoutSession.class);
                        if (session != null && session.getTimestamp() >= todayStartMillis) {
                            completedTodayCount++;
                        }
                    }

                    tvWorkoutLabel.setText("Workout Goal (" + Math.min(1, completedTodayCount) + " / 1 Completed)");
                    pbWorkoutProgress.setProgress(completedTodayCount > 0 ? 100 : 0);
                    tvStreak.setText(totalSessions + (totalSessions == 1 ? " Workout" : " Workouts"));
                });
    }

    private void loadDailyWaterProgress() {
        if (mAuth.getCurrentUser() == null || getContext() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        SharedPreferences prefs = getContext().getSharedPreferences("FlexiBotPrefs", Context.MODE_PRIVATE);
        int waterGoal = prefs.getInt("daily_water_goal_ml", 2500);
        String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("users").document(uid).collection("waterIntake")
                .whereEqualTo("dateKey", todayKey)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    int todayTotal = 0;
                    for (DocumentSnapshot doc : snapshots) {
                        WaterLog log = doc.toObject(WaterLog.class);
                        if (log != null) {
                            todayTotal += log.getAmountMl();
                        }
                    }
                    tvWaterLabel.setText(String.format(Locale.getDefault(), "Water Intake (%,d / %,d ml)", todayTotal, waterGoal));
                    int pct = waterGoal > 0 ? Math.min(100, (todayTotal * 100) / waterGoal) : 0;
                    pbWaterProgress.setProgress(pct);
                });
    }
}