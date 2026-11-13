package com.example.flexibot.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flexibot.R;
import com.example.flexibot.adapters.GoalAdapter;
import com.example.flexibot.adapters.WeightHistoryAdapter;
import com.example.flexibot.adapters.WorkoutHistoryAdapter;
import com.example.flexibot.models.FitnessGoalTarget;
import com.example.flexibot.models.WaterLog;
import com.example.flexibot.models.WeightEntry;
import com.example.flexibot.models.WorkoutSession;
import com.example.flexibot.utils.FlexyAnimator;
import com.example.flexibot.utils.FlexyBuddyHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrackersActivity extends AppCompatActivity {

    private TextView tabWeight, tabWater, tabWorkout, tabGoal;
    private LinearLayout sectionWeight, sectionWater, sectionWorkout, sectionGoal;

    // Weight Tracker Views
    private TextView tvCurrentWeightSummary, tvTotalWeightChange;
    private EditText etWeightInput, etWeightNote;
    private Button btnPickWeightDate, btnSaveWeight;
    private RecyclerView rvWeightHistory;
    private final List<WeightEntry> weightList = new ArrayList<>();
    private WeightHistoryAdapter weightAdapter;
    private long selectedWeightTimestamp = System.currentTimeMillis();

    // Water Tracker & FLEXY Views
    private TextView tvWaterTodayTotal, tvWaterPercentText;
    private ProgressBar pbTrackerWater;
    private EditText etCustomWaterMl;
    private ImageView ivWaterFlexyAvatar;
    private TextView tvWaterFlexyBadge, tvWaterFlexySpeech;
    private int dailyWaterGoalMl = 2500;
    private int todayWaterTotalMl = 0;

    // Workout Tracker Views
    private TextView tvTrackerTotalSessions, tvTrackerTotalMinutes, tvTrackerTotalExercises;
    private EditText etManualWorkoutTitle, etManualWorkoutMinutes, etManualWorkoutExercises;
    private RecyclerView rvTrackerWorkouts;
    private final List<WorkoutSession> workoutList = new ArrayList<>();
    private WorkoutHistoryAdapter workoutAdapter;

    // Goal Tracker Views
    private Spinner spinnerGoalCategory;
    private EditText etGoalTitle, etGoalStartVal, etGoalCurrentVal, etGoalTargetVal, etGoalUnit;
    private RecyclerView rvGoalTargets;
    private final List<FitnessGoalTarget> goalList = new ArrayList<>();
    private GoalAdapter goalAdapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trackers);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            finish();
            return;
        }
        uid = mAuth.getCurrentUser().getUid();

        SharedPreferences prefs = getSharedPreferences("FlexiBotPrefs", MODE_PRIVATE);
        dailyWaterGoalMl = prefs.getInt("daily_water_goal_ml", 2500);

        initViews();
        setupTabs();
        setupWeightTracker();
        setupWaterTracker();
        setupWorkoutTracker();
        setupGoalTracker();

        loadAllTrackerData();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBackTrackers);
        btnBack.setOnClickListener(v -> finish());

        tabWeight = findViewById(R.id.tabWeightTracker);
        tabWater = findViewById(R.id.tabWaterTracker);
        tabWorkout = findViewById(R.id.tabWorkoutTracker);
        tabGoal = findViewById(R.id.tabGoalTracker);

        sectionWeight = findViewById(R.id.sectionWeightTracker);
        sectionWater = findViewById(R.id.sectionWaterTracker);
        sectionWorkout = findViewById(R.id.sectionWorkoutTracker);
        sectionGoal = findViewById(R.id.sectionGoalTracker);
    }

    private void setupTabs() {
        tabWeight.setOnClickListener(v -> selectTab(0));
        tabWater.setOnClickListener(v -> selectTab(1));
        tabWorkout.setOnClickListener(v -> selectTab(2));
        tabGoal.setOnClickListener(v -> selectTab(3));
    }

    private void selectTab(int index) {
        TextView[] tabs = {tabWeight, tabWater, tabWorkout, tabGoal};
        LinearLayout[] sections = {sectionWeight, sectionWater, sectionWorkout, sectionGoal};

        for (int i = 0; i < tabs.length; i++) {
            boolean active = (i == index);
            tabs[i].setBackgroundResource(active ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            tabs[i].setTextColor(ContextCompat.getColor(this, active ? R.color.white : R.color.grey_text));
            sections[i].setVisibility(active ? View.VISIBLE : View.GONE);
        }

        if (index == 1 && ivWaterFlexyAvatar != null) {
            syncFlexyWaterBaselineState();
        }
    }

    // =========================================================================
    // A. WEIGHT TRACKER
    // =========================================================================
    private void setupWeightTracker() {
        tvCurrentWeightSummary = findViewById(R.id.tvCurrentWeightSummary);
        tvTotalWeightChange = findViewById(R.id.tvTotalWeightChange);
        etWeightInput = findViewById(R.id.etWeightInput);
        etWeightNote = findViewById(R.id.etWeightNote);
        btnPickWeightDate = findViewById(R.id.btnPickWeightDate);
        btnSaveWeight = findViewById(R.id.btnSaveWeight);
        rvWeightHistory = findViewById(R.id.rvWeightHistory);

        rvWeightHistory.setLayoutManager(new LinearLayoutManager(this));
        weightAdapter = new WeightHistoryAdapter(weightList);
        rvWeightHistory.setAdapter(weightAdapter);

        btnPickWeightDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedWeightTimestamp);
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, dayOfMonth);
                selectedWeightTimestamp = chosen.getTimeInMillis();
                SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                btnPickWeightDate.setText(fmt.format(new Date(selectedWeightTimestamp)));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSaveWeight.setOnClickListener(v -> saveWeightEntry());
    }

    private void saveWeightEntry() {
        String weightStr = etWeightInput.getText().toString().trim();
        if (TextUtils.isEmpty(weightStr)) {
            etWeightInput.setError("Enter weight in kg");
            return;
        }

        double weightKg;
        try {
            weightKg = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            etWeightInput.setError("Invalid number");
            return;
        }

        if (weightKg <= 0 || weightKg > 500) {
            etWeightInput.setError("Enter a valid weight");
            return;
        }

        String note = etWeightNote.getText().toString().trim();
        String entryId = db.collection("users").document(uid).collection("weightHistory").document().getId();
        WeightEntry entry = new WeightEntry(entryId, weightKg, note, selectedWeightTimestamp);

        btnSaveWeight.setEnabled(false);
        db.collection("users").document(uid).collection("weightHistory").document(entryId)
                .set(entry)
                .addOnSuccessListener(aVoid -> {
                    btnSaveWeight.setEnabled(true);
                    etWeightInput.setText("");
                    etWeightNote.setText("");
                    selectedWeightTimestamp = System.currentTimeMillis();
                    btnPickWeightDate.setText("Today");

                    db.collection("users").document(uid).update("weight", weightKg);

                    Toast.makeText(this, "Weight entry saved!", Toast.LENGTH_SHORT).show();
                    loadWeightHistory();
                })
                .addOnFailureListener(e -> {
                    btnSaveWeight.setEnabled(true);
                    Toast.makeText(this, "Failed to save weight: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadWeightHistory() {
        db.collection("users").document(uid).collection("weightHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    weightList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        WeightEntry entry = doc.toObject(WeightEntry.class);
                        if (entry != null) weightList.add(entry);
                    }
                    weightAdapter.notifyDataSetChanged();

                    if (!weightList.isEmpty()) {
                        double latest = weightList.get(0).getWeightKg();
                        double oldest = weightList.get(weightList.size() - 1).getWeightKg();
                        double netChange = latest - oldest;

                        tvCurrentWeightSummary.setText(String.format(Locale.getDefault(), "%.1f kg", latest));
                        if (Math.abs(netChange) < 0.05) {
                            tvTotalWeightChange.setText("0.0 kg");
                        } else if (netChange > 0) {
                            tvTotalWeightChange.setText(String.format(Locale.getDefault(), "+%.1f kg", netChange));
                        } else {
                            tvTotalWeightChange.setText(String.format(Locale.getDefault(), "%.1f kg", netChange));
                        }
                    } else {
                        tvCurrentWeightSummary.setText("-- kg");
                        tvTotalWeightChange.setText("0.0 kg");
                    }
                });
    }

    // =========================================================================
    // B. WATER TRACKER + LIVE FLEXY REACTIONS
    // =========================================================================
    private void setupWaterTracker() {
        tvWaterTodayTotal = findViewById(R.id.tvWaterTodayTotal);
        tvWaterPercentText = findViewById(R.id.tvWaterPercentText);
        pbTrackerWater = findViewById(R.id.pbTrackerWater);
        etCustomWaterMl = findViewById(R.id.etCustomWaterMl);

        ivWaterFlexyAvatar = findViewById(R.id.ivWaterFlexyAvatar);
        tvWaterFlexyBadge = findViewById(R.id.tvWaterFlexyBadge);
        tvWaterFlexySpeech = findViewById(R.id.tvWaterFlexySpeech);

        Button btn150 = findViewById(R.id.btnWater150);
        Button btn250 = findViewById(R.id.btnWater250);
        Button btn500 = findViewById(R.id.btnWater500);
        Button btnCustom = findViewById(R.id.btnAddCustomWater);
        Button btnEditGoal = findViewById(R.id.btnEditWaterGoal);
        Button btnResetToday = findViewById(R.id.btnResetTodayWater);

        btn150.setOnClickListener(v -> logWaterIntake(150));
        btn250.setOnClickListener(v -> logWaterIntake(250));
        btn500.setOnClickListener(v -> logWaterIntake(500));

        btnCustom.setOnClickListener(v -> {
            String val = etCustomWaterMl.getText().toString().trim();
            if (TextUtils.isEmpty(val)) {
                etCustomWaterMl.setError("Enter ml");
                return;
            }
            try {
                int ml = Integer.parseInt(val);
                if (ml > 0 && ml <= 5000) {
                    etCustomWaterMl.setText("");
                    logWaterIntake(ml);
                } else {
                    etCustomWaterMl.setError("Enter 1–5000 ml");
                }
            } catch (NumberFormatException e) {
                etCustomWaterMl.setError("Invalid number");
            }
        });

        btnEditGoal.setOnClickListener(v -> showEditWaterGoalDialog());
        btnResetToday.setOnClickListener(v -> resetTodayWater());
    }

    private String getTodayDateKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void logWaterIntake(int amountMl) {
        String todayKey = getTodayDateKey();
        String logId = db.collection("users").document(uid).collection("waterIntake").document().getId();
        WaterLog log = new WaterLog(logId, amountMl, todayKey, System.currentTimeMillis());

        db.collection("users").document(uid).collection("waterIntake").document(logId)
                .set(log)
                .addOnSuccessListener(aVoid -> {
                    todayWaterTotalMl += amountMl;
                    updateWaterUI();
                    triggerFlexyWaterReaction(amountMl);
                    loadTodayWaterIntake(false);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to log water: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void triggerFlexyWaterReaction(int addedMl) {
        if (ivWaterFlexyAvatar == null) return;

        FlexyBuddyHelper.BuddyState reaction =
                FlexyBuddyHelper.getWaterLoggedReaction(addedMl, todayWaterTotalMl, dailyWaterGoalMl);

        ivWaterFlexyAvatar.setImageResource(reaction.avatarResId);
        tvWaterFlexyBadge.setText(reaction.moodLabel);
        tvWaterFlexySpeech.setText(reaction.message);

        if (reaction.mood == FlexyBuddyHelper.Mood.CELEBRATING) {
            FlexyAnimator.playCelebratingSpin(ivWaterFlexyAvatar, true);
        } else {
            FlexyAnimator.playHappyBounce(ivWaterFlexyAvatar, true);
        }
    }

    private void syncFlexyWaterBaselineState() {
        if (ivWaterFlexyAvatar == null) return;

        FlexyBuddyHelper.BuddyState state = FlexyBuddyHelper.getCurrentBuddyState(
                "Champ",
                "Hydration",
                0,
                todayWaterTotalMl,
                dailyWaterGoalMl
        );

        ivWaterFlexyAvatar.setImageResource(state.avatarResId);
        tvWaterFlexyBadge.setText(state.moodLabel);
        tvWaterFlexySpeech.setText(state.message);

        switch (state.mood) {
            case CELEBRATING:
                FlexyAnimator.playCelebratingSpin(ivWaterFlexyAvatar, true);
                break;
            case ENCOURAGING:
                FlexyAnimator.playEncouragingNod(ivWaterFlexyAvatar, true);
                break;
            case HAPPY:
                FlexyAnimator.playHappyBounce(ivWaterFlexyAvatar, true);
                break;
            default:
                FlexyAnimator.startIdleAnimation(ivWaterFlexyAvatar);
                break;
        }
    }

    private void loadTodayWaterIntake(boolean updateFlexyState) {
        String todayKey = getTodayDateKey();
        db.collection("users").document(uid).collection("waterIntake")
                .whereEqualTo("dateKey", todayKey)
                .get()
                .addOnSuccessListener(snapshots -> {
                    todayWaterTotalMl = 0;
                    for (DocumentSnapshot doc : snapshots) {
                        WaterLog log = doc.toObject(WaterLog.class);
                        if (log != null) {
                            todayWaterTotalMl += log.getAmountMl();
                        }
                    }
                    updateWaterUI();
                    if (updateFlexyState) {
                        syncFlexyWaterBaselineState();
                    }
                });
    }

    private void updateWaterUI() {
        tvWaterTodayTotal.setText(String.format(Locale.getDefault(), "%,d / %,d ml", todayWaterTotalMl, dailyWaterGoalMl));
        int pct = dailyWaterGoalMl > 0 ? Math.min(100, (todayWaterTotalMl * 100) / dailyWaterGoalMl) : 0;
        pbTrackerWater.setProgress(pct);
        tvWaterPercentText.setText(pct + "% of daily goal • Resets automatically at midnight");
    }

    private void showEditWaterGoalDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Daily goal in ml (e.g. 2500)");
        input.setText(String.valueOf(dailyWaterGoalMl));

        new AlertDialog.Builder(this)
                .setTitle("Daily Water Goal (ml)")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String str = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(str)) {
                        try {
                            int newGoal = Integer.parseInt(str);
                            if (newGoal >= 500 && newGoal <= 10000) {
                                dailyWaterGoalMl = newGoal;
                                getSharedPreferences("FlexiBotPrefs", MODE_PRIVATE)
                                        .edit()
                                        .putInt("daily_water_goal_ml", dailyWaterGoalMl)
                                        .apply();
                                updateWaterUI();
                                syncFlexyWaterBaselineState();
                                Toast.makeText(this, "Water goal updated!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetTodayWater() {
        String todayKey = getTodayDateKey();
        db.collection("users").document(uid).collection("waterIntake")
                .whereEqualTo("dateKey", todayKey)
                .get()
                .addOnSuccessListener(snapshots -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : snapshots) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(aVoid -> {
                        todayWaterTotalMl = 0;
                        updateWaterUI();
                        syncFlexyWaterBaselineState();
                        Toast.makeText(this, "Today's water intake reset", Toast.LENGTH_SHORT).show();
                    });
                });
    }

    // =========================================================================
    // C. WORKOUT TRACKER
    // =========================================================================
    private void setupWorkoutTracker() {
        tvTrackerTotalSessions = findViewById(R.id.tvTrackerTotalSessions);
        tvTrackerTotalMinutes = findViewById(R.id.tvTrackerTotalMinutes);
        tvTrackerTotalExercises = findViewById(R.id.tvTrackerTotalExercises);
        etManualWorkoutTitle = findViewById(R.id.etManualWorkoutTitle);
        etManualWorkoutMinutes = findViewById(R.id.etManualWorkoutMinutes);
        etManualWorkoutExercises = findViewById(R.id.etManualWorkoutExercises);
        Button btnRecordManual = findViewById(R.id.btnRecordManualWorkout);
        rvTrackerWorkouts = findViewById(R.id.rvTrackerWorkouts);

        rvTrackerWorkouts.setLayoutManager(new LinearLayoutManager(this));
        workoutAdapter = new WorkoutHistoryAdapter(workoutList);
        rvTrackerWorkouts.setAdapter(workoutAdapter);

        btnRecordManual.setOnClickListener(v -> recordManualWorkout());
    }

    private void recordManualWorkout() {
        String title = etManualWorkoutTitle.getText().toString().trim();
        String minStr = etManualWorkoutMinutes.getText().toString().trim();
        String exStr = etManualWorkoutExercises.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            etManualWorkoutTitle.setError("Enter workout name");
            return;
        }
        if (TextUtils.isEmpty(minStr)) {
            etManualWorkoutMinutes.setError("Enter duration");
            return;
        }

        int mins = Integer.parseInt(minStr);
        int exDone = TextUtils.isEmpty(exStr) ? 1 : Integer.parseInt(exStr);
        int calories = mins * 7 + (exDone * 10);

        String sessionId = db.collection("users").document(uid).collection("workouts").document().getId();
        WorkoutSession session = new WorkoutSession(
                sessionId, title, "Custom Logged", mins, exDone, exDone, calories, System.currentTimeMillis()
        );

        db.collection("users").document(uid).collection("workouts").document(sessionId)
                .set(session)
                .addOnSuccessListener(aVoid -> {
                    etManualWorkoutTitle.setText("");
                    etManualWorkoutMinutes.setText("");
                    etManualWorkoutExercises.setText("");
                    Toast.makeText(this, "Workout recorded!", Toast.LENGTH_SHORT).show();
                    loadWorkoutTrackerHistory();
                });
    }

    private void loadWorkoutTrackerHistory() {
        db.collection("users").document(uid).collection("workouts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    workoutList.clear();
                    int totalMins = 0;
                    int totalExercises = 0;

                    for (DocumentSnapshot doc : snapshots) {
                        WorkoutSession s = doc.toObject(WorkoutSession.class);
                        if (s != null) {
                            workoutList.add(s);
                            totalMins += s.getDurationMinutes();
                            totalExercises += s.getExercisesCompleted();
                        }
                    }

                    workoutAdapter.notifyDataSetChanged();
                    tvTrackerTotalSessions.setText(String.valueOf(workoutList.size()));
                    tvTrackerTotalMinutes.setText(totalMins + " min");
                    tvTrackerTotalExercises.setText(String.valueOf(totalExercises));
                });
    }

    // =========================================================================
    // D. GOAL TRACKER
    // =========================================================================
    private void setupGoalTracker() {
        spinnerGoalCategory = findViewById(R.id.spinnerGoalTrackerCategory);
        etGoalTitle = findViewById(R.id.etGoalTitle);
        etGoalStartVal = findViewById(R.id.etGoalStartVal);
        etGoalCurrentVal = findViewById(R.id.etGoalCurrentVal);
        etGoalTargetVal = findViewById(R.id.etGoalTargetVal);
        etGoalUnit = findViewById(R.id.etGoalUnit);
        Button btnCreateGoal = findViewById(R.id.btnCreateGoal);
        rvGoalTargets = findViewById(R.id.rvGoalTargets);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.fitness_goals, R.layout.item_spinner);
        spinnerGoalCategory.setAdapter(adapter);

        rvGoalTargets.setLayoutManager(new LinearLayoutManager(this));
        goalAdapter = new GoalAdapter(goalList, new GoalAdapter.OnGoalActionListener() {
            @Override
            public void onUpdateProgressClicked(FitnessGoalTarget goal) {
                showUpdateGoalDialog(goal);
            }

            @Override
            public void onDeleteGoalClicked(FitnessGoalTarget goal) {
                deleteGoalTarget(goal);
            }
        });
        rvGoalTargets.setAdapter(goalAdapter);

        btnCreateGoal.setOnClickListener(v -> createNewGoalTarget());
    }

    private void createNewGoalTarget() {
        String category = spinnerGoalCategory.getSelectedItem() != null
                ? spinnerGoalCategory.getSelectedItem().toString() : "General fitness";
        if (category.contains("Select")) {
            category = "General fitness";
        }

        String title = etGoalTitle.getText().toString().trim();
        String startStr = etGoalStartVal.getText().toString().trim();
        String currentStr = etGoalCurrentVal.getText().toString().trim();
        String targetStr = etGoalTargetVal.getText().toString().trim();
        String unit = etGoalUnit.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(startStr) || TextUtils.isEmpty(currentStr) || TextUtils.isEmpty(targetStr)) {
            Toast.makeText(this, "Please fill in title, start, current, and target values", Toast.LENGTH_SHORT).show();
            return;
        }

        double startVal, currentVal, targetVal;
        try {
            startVal = Double.parseDouble(startStr);
            currentVal = Double.parseDouble(currentStr);
            targetVal = Double.parseDouble(targetStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(unit)) unit = "units";

        String goalId = db.collection("users").document(uid).collection("goals").document().getId();
        FitnessGoalTarget goal = new FitnessGoalTarget(
                goalId, category, title, unit, startVal, currentVal, targetVal, System.currentTimeMillis()
        );

        db.collection("users").document(uid).collection("goals").document(goalId)
                .set(goal)
                .addOnSuccessListener(aVoid -> {
                    etGoalTitle.setText("");
                    etGoalStartVal.setText("");
                    etGoalCurrentVal.setText("");
                    etGoalTargetVal.setText("");
                    etGoalUnit.setText("");
                    Toast.makeText(this, "Measurable goal created!", Toast.LENGTH_SHORT).show();
                    loadGoalTargets();
                });
    }

    private void showUpdateGoalDialog(FitnessGoalTarget goal) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(String.valueOf(goal.getCurrentValue()));

        new AlertDialog.Builder(this)
                .setTitle("Update Progress (" + goal.getUnit() + ")")
                .setMessage("Target: " + goal.getTargetValue() + " " + goal.getUnit())
                .setView(input)
                .setPositiveButton("Update", (dialog, which) -> {
                    String valStr = input.getText().toString().trim();
                    if (!TextUtils.isEmpty(valStr)) {
                        try {
                            double newCurrent = Double.parseDouble(valStr);
                            db.collection("users").document(uid).collection("goals").document(goal.getId())
                                    .update("currentValue", newCurrent, "updatedAt", System.currentTimeMillis())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Goal progress updated!", Toast.LENGTH_SHORT).show();
                                        loadGoalTargets();
                                    });
                        } catch (NumberFormatException ignored) {}
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteGoalTarget(FitnessGoalTarget goal) {
        db.collection("users").document(uid).collection("goals").document(goal.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Goal removed", Toast.LENGTH_SHORT).show();
                    loadGoalTargets();
                });
    }

    private void loadGoalTargets() {
        db.collection("users").document(uid).collection("goals")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    goalList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        FitnessGoalTarget g = doc.toObject(FitnessGoalTarget.class);
                        if (g != null) goalList.add(g);
                    }
                    goalAdapter.notifyDataSetChanged();
                });
    }

    private void loadAllTrackerData() {
        loadWeightHistory();
        loadTodayWaterIntake(true);
        loadWorkoutTrackerHistory();
        loadGoalTargets();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FlexyAnimator.stopAnimation(ivWaterFlexyAvatar);
    }

    @Override
    protected void onDestroy() {
        FlexyAnimator.stopAnimation(ivWaterFlexyAvatar);
        super.onDestroy();
    }
}