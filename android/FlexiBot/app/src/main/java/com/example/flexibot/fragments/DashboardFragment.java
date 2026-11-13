package com.example.flexibot.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.flexibot.R;
import com.example.flexibot.activities.CalculatorsActivity;
import com.example.flexibot.activities.TrackersActivity;
import com.example.flexibot.models.FitnessGoalTarget;
import com.example.flexibot.models.User;
import com.example.flexibot.models.WaterLog;
import com.example.flexibot.models.WeightEntry;
import com.example.flexibot.models.WorkoutSession;
import com.example.flexibot.views.WeeklyActivityChartView;
import com.example.flexibot.views.WeightTrendChartView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private ProgressBar progressDashboard;
    private TextView tvTotalWorkouts, tvTotalExercises, tvActiveMinutes, tvWeeklyWorkouts;
    private TextView tvCurrentWeight, tvWeightDelta, tvGoalAvg, tvActiveGoalsCount;
    private TextView tvWeightEntriesCount, tvCoachInsight;
    private TextView chipChartWorkouts, chipChartWater;

    private WeightTrendChartView chartWeightTrend;
    private WeeklyActivityChartView chartWeeklyActivity;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final String[] last7DayLabels = new String[7];
    private final String[] last7DayKeys = new String[7];
    private final int[] weeklyWorkoutMinutes = new int[7];
    private final int[] weeklyWaterMl = new int[7];
    private boolean showingWaterChart = false;

    private User currentUserProfile;
    private int totalWorkoutCount = 0;
    private int todayWaterIntakeMl = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);
        prepareLast7DaysAxis();
        setupChartToggle();

        return view;
    }

    private void initViews(View view) {
        progressDashboard = view.findViewById(R.id.progressDashboard);

        tvTotalWorkouts = view.findViewById(R.id.tvDashTotalWorkouts);
        tvTotalExercises = view.findViewById(R.id.tvDashTotalExercises);
        tvActiveMinutes = view.findViewById(R.id.tvDashActiveMinutes);
        tvWeeklyWorkouts = view.findViewById(R.id.tvDashWeeklyWorkouts);
        tvCurrentWeight = view.findViewById(R.id.tvDashCurrentWeight);
        tvWeightDelta = view.findViewById(R.id.tvDashWeightDelta);
        tvGoalAvg = view.findViewById(R.id.tvDashGoalAvg);
        tvActiveGoalsCount = view.findViewById(R.id.tvDashActiveGoalsCount);
        tvWeightEntriesCount = view.findViewById(R.id.tvDashWeightEntriesCount);
        tvCoachInsight = view.findViewById(R.id.tvDashCoachInsight);

        chipChartWorkouts = view.findViewById(R.id.chipChartWorkouts);
        chipChartWater = view.findViewById(R.id.chipChartWater);

        chartWeightTrend = view.findViewById(R.id.chartWeightTrend);
        chartWeeklyActivity = view.findViewById(R.id.chartWeeklyActivity);

        Button btnOpenTrackers = view.findViewById(R.id.btnDashOpenTrackers);
        Button btnOpenCalculators = view.findViewById(R.id.btnDashOpenCalculators);

        btnOpenTrackers.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), TrackersActivity.class)));
        btnOpenCalculators.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), CalculatorsActivity.class)));
    }

    private void prepareLast7DaysAxis() {
        SimpleDateFormat dayFmt = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat keyFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {
            Date d = cal.getTime();
            last7DayLabels[i] = dayFmt.format(d);
            last7DayKeys[i] = keyFmt.format(d);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void setupChartToggle() {
        chipChartWorkouts.setOnClickListener(v -> {
            showingWaterChart = false;
            updateToggleChips();
            refreshWeeklyBarChart();
        });

        chipChartWater.setOnClickListener(v -> {
            showingWaterChart = true;
            updateToggleChips();
            refreshWeeklyBarChart();
        });
    }

    private void updateToggleChips() {
        if (!isAdded() || getContext() == null) return;
        chipChartWorkouts.setBackgroundResource(!showingWaterChart ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipChartWorkouts.setTextColor(ContextCompat.getColor(getContext(), !showingWaterChart ? R.color.white : R.color.grey_text));

        chipChartWater.setBackgroundResource(showingWaterChart ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipChartWater.setTextColor(ContextCompat.getColor(getContext(), showingWaterChart ? R.color.white : R.color.grey_text));
    }

    private void refreshWeeklyBarChart() {
        if (showingWaterChart) {
            chartWeeklyActivity.setWeeklyData(last7DayLabels, weeklyWaterMl, "");
        } else {
            chartWeeklyActivity.setWeeklyData(last7DayLabels, weeklyWorkoutMinutes, "m");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardAnalytics();
    }

    private void loadDashboardAnalytics() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();
        progressDashboard.setVisibility(View.VISIBLE);

        // 1. Load Profile Baseline
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    if (doc.exists()) {
                        currentUserProfile = doc.toObject(User.class);
                        if (currentUserProfile != null) {
                            tvCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f kg", currentUserProfile.getWeight()));
                        }
                    }
                    updateCoachInsight();
                });

        // 2. Load Workout Analytics & 7-Day Activity
        db.collection("users").document(uid).collection("workouts").get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    progressDashboard.setVisibility(View.GONE);

                    totalWorkoutCount = snapshots.size();
                    int totalMins = 0;
                    int totalEx = 0;
                    int sessionsLast7Days = 0;

                    Map<String, Integer> minsByDayKey = new HashMap<>();
                    SimpleDateFormat keyFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                    for (DocumentSnapshot doc : snapshots) {
                        WorkoutSession session = doc.toObject(WorkoutSession.class);
                        if (session != null) {
                            totalMins += session.getDurationMinutes();
                            totalEx += session.getExercisesCompleted();

                            String dayKey = keyFmt.format(new Date(session.getTimestamp()));
                            int currentDayMins = minsByDayKey.containsKey(dayKey) ? minsByDayKey.get(dayKey) : 0;
                            minsByDayKey.put(dayKey, currentDayMins + session.getDurationMinutes());
                        }
                    }

                    for (int i = 0; i < 7; i++) {
                        int mins = minsByDayKey.containsKey(last7DayKeys[i]) ? minsByDayKey.get(last7DayKeys[i]) : 0;
                        weeklyWorkoutMinutes[i] = mins;
                        if (mins > 0) sessionsLast7Days++;
                    }

                    tvTotalWorkouts.setText(String.valueOf(totalWorkoutCount));
                    tvTotalExercises.setText(totalEx + " exercises done");
                    tvActiveMinutes.setText(totalMins + " min");
                    tvWeeklyWorkouts.setText(sessionsLast7Days + " active days this week");

                    refreshWeeklyBarChart();
                    updateCoachInsight();
                });

        // 3. Load Weight History & Trend Chart
        db.collection("users").document(uid).collection("weightHistory")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    List<WeightEntry> entries = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        WeightEntry entry = doc.toObject(WeightEntry.class);
                        if (entry != null) entries.add(entry);
                    }

                    tvWeightEntriesCount.setText(entries.size() + (entries.size() == 1 ? " entry" : " entries"));
                    chartWeightTrend.setEntries(entries);

                    if (!entries.isEmpty()) {
                        double first = entries.get(0).getWeightKg();
                        double latest = entries.get(entries.size() - 1).getWeightKg();
                        double diff = latest - first;

                        tvCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f kg", latest));
                        if (Math.abs(diff) < 0.05) {
                            tvWeightDelta.setText("0.0 kg net change");
                        } else if (diff > 0) {
                            tvWeightDelta.setText(String.format(Locale.getDefault(), "+%.1f kg since start", diff));
                        } else {
                            tvWeightDelta.setText(String.format(Locale.getDefault(), "%.1f kg since start", diff));
                        }
                    }
                });

        // 4. Load 7-Day Water Intake
        db.collection("users").document(uid).collection("waterIntake").get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    Map<String, Integer> waterByDayKey = new HashMap<>();
                    for (DocumentSnapshot doc : snapshots) {
                        WaterLog log = doc.toObject(WaterLog.class);
                        if (log != null && log.getDateKey() != null) {
                            int prev = waterByDayKey.containsKey(log.getDateKey()) ? waterByDayKey.get(log.getDateKey()) : 0;
                            waterByDayKey.put(log.getDateKey(), prev + log.getAmountMl());
                        }
                    }

                    for (int i = 0; i < 7; i++) {
                        weeklyWaterMl[i] = waterByDayKey.containsKey(last7DayKeys[i]) ? waterByDayKey.get(last7DayKeys[i]) : 0;
                    }
                    todayWaterIntakeMl = weeklyWaterMl[6];

                    if (showingWaterChart) {
                        refreshWeeklyBarChart();
                    }
                    updateCoachInsight();
                });

        // 5. Load Measurable Goals Average Completion
        db.collection("users").document(uid).collection("goals").get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;
                    int count = snapshots.size();
                    if (count == 0) {
                        tvGoalAvg.setText("0%");
                        tvActiveGoalsCount.setText("0 active goals");
                        return;
                    }

                    int sumPct = 0;
                    for (DocumentSnapshot doc : snapshots) {
                        FitnessGoalTarget g = doc.toObject(FitnessGoalTarget.class);
                        if (g != null) {
                            sumPct += g.calculateCompletionPercentage();
                        }
                    }
                    int avg = Math.round((float) sumPct / count);
                    tvGoalAvg.setText(avg + "%");
                    tvActiveGoalsCount.setText(count + (count == 1 ? " active goal" : " active goals"));
                });
    }

    private void updateCoachInsight() {
        if (!isAdded() || getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences("FlexiBotPrefs", Context.MODE_PRIVATE);
        int waterGoal = prefs.getInt("daily_water_goal_ml", 2500);
        int remainingWater = Math.max(0, waterGoal - todayWaterIntakeMl);

        String goalName = (currentUserProfile != null && currentUserProfile.getFitnessGoal() != null)
                ? currentUserProfile.getFitnessGoal()
                : "General fitness";

        StringBuilder insight = new StringBuilder();
        insight.append("Primary Focus: ").append(goalName).append(". ");

        if (totalWorkoutCount == 0) {
            insight.append("Start your first guided workout today to kickstart your weekly consistency chart. ");
        } else {
            insight.append("You have completed ").append(totalWorkoutCount)
                    .append(totalWorkoutCount == 1 ? " workout session" : " workout sessions")
                    .append(" so far—keep building momentum! ");
        }

        if (remainingWater > 0) {
            insight.append(String.format(Locale.getDefault(), "Drink %,d ml more water today to hit your %,d ml hydration goal.", remainingWater, waterGoal));
        } else {
            insight.append("Awesome job—you hit 100% of today's hydration target!");
        }

        tvCoachInsight.setText(insight.toString());
    }
}