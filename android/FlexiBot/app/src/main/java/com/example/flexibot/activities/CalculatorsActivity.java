package com.example.flexibot.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flexibot.R;
import com.example.flexibot.models.User;
import com.example.flexibot.utils.FitnessCalculatorUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Locale;

public class CalculatorsActivity extends AppCompatActivity {

    private EditText etWeight, etHeight, etAge;
    private Spinner spinnerGender, spinnerActivity, spinnerGoal;
    private Button btnSyncProfile, btnRecalculateAll;
    private ProgressBar progressCalculators;

    private TextView tvBmiValue, tvBmiCategory;
    private ProgressBar pbBmiIndicator;

    private TextView tvDailyCalories, tvBmrValue, tvCalorieGoalNote;
    private TextView tvProteinRange, tvProteinNote;
    private TextView tvWaterResult, tvWaterGlasses;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListenerRegistration profileListener;
    private User latestSavedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculators);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupSpinners();
        attachProfileListener();

        btnRecalculateAll.setOnClickListener(v -> performAllCalculations(true));

        btnSyncProfile.setOnClickListener(v -> {
            if (latestSavedUser != null) {
                applyUserProfileToInputs(latestSavedUser);
                performAllCalculations(false);
                Toast.makeText(this, "Synced with your saved profile", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Profile not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btnBackCalculators);
        btnBack.setOnClickListener(v -> finish());

        btnSyncProfile = findViewById(R.id.btnSyncProfile);
        btnRecalculateAll = findViewById(R.id.btnRecalculateAll);
        progressCalculators = findViewById(R.id.progressCalculators);

        etWeight = findViewById(R.id.etCalcWeight);
        etHeight = findViewById(R.id.etCalcHeight);
        etAge = findViewById(R.id.etCalcAge);

        spinnerGender = findViewById(R.id.spinnerCalcGender);
        spinnerActivity = findViewById(R.id.spinnerCalcActivity);
        spinnerGoal = findViewById(R.id.spinnerCalcGoal);

        tvBmiValue = findViewById(R.id.tvBmiValue);
        tvBmiCategory = findViewById(R.id.tvBmiCategory);
        pbBmiIndicator = findViewById(R.id.pbBmiIndicator);

        tvDailyCalories = findViewById(R.id.tvDailyCalories);
        tvBmrValue = findViewById(R.id.tvBmrValue);
        tvCalorieGoalNote = findViewById(R.id.tvCalorieGoalNote);

        tvProteinRange = findViewById(R.id.tvProteinRange);
        tvProteinNote = findViewById(R.id.tvProteinNote);

        tvWaterResult = findViewById(R.id.tvWaterResult);
        tvWaterGlasses = findViewById(R.id.tvWaterGlasses);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.gender_options, R.layout.item_spinner);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(
                this, R.array.activity_level_options, R.layout.item_spinner);
        spinnerActivity.setAdapter(activityAdapter);

        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(
                this, R.array.fitness_goals, R.layout.item_spinner);
        spinnerGoal.setAdapter(goalAdapter);

        AdapterView.OnItemSelectedListener autoCalcListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performAllCalculations(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerGender.setOnItemSelectedListener(autoCalcListener);
        spinnerActivity.setOnItemSelectedListener(autoCalcListener);
        spinnerGoal.setOnItemSelectedListener(autoCalcListener);
    }

    private void attachProfileListener() {
        if (mAuth.getCurrentUser() == null) {
            progressCalculators.setVisibility(View.GONE);
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();

        // Automatically recalculates whenever Firestore profile changes
        profileListener = db.collection("users").document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    progressCalculators.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(this, "Error syncing profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        if (user != null) {
                            latestSavedUser = user;
                            applyUserProfileToInputs(user);
                            performAllCalculations(false);
                        }
                    }
                });
    }

    private void applyUserProfileToInputs(User user) {
        etWeight.setText(String.valueOf(user.getWeight()));
        etHeight.setText(String.valueOf(user.getHeight()));
        etAge.setText(String.valueOf(user.getAge()));

        selectSpinnerValue(spinnerGender, user.getGender());
        selectSpinnerValue(spinnerGoal, user.getFitnessGoal());

        String defaultActivity = FitnessCalculatorUtils.mapFitnessLevelToActivity(user.getFitnessLevel());
        selectSpinnerValue(spinnerActivity, defaultActivity);
    }

    private void selectSpinnerValue(Spinner spinner, String value) {
        if (value == null || spinner.getAdapter() == null) return;
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            if (value.equalsIgnoreCase(spinner.getAdapter().getItem(i).toString())) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void performAllCalculations(boolean showValidationToast) {
        String weightStr = etWeight.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();

        if (TextUtils.isEmpty(weightStr) || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(ageStr)) {
            if (showValidationToast) {
                Toast.makeText(this, "Please enter valid weight, height, and age.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        double weightKg;
        double heightCm;
        int age;
        try {
            weightKg = Double.parseDouble(weightStr);
            heightCm = Double.parseDouble(heightStr);
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            if (showValidationToast) {
                Toast.makeText(this, "Please enter valid numeric values.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (weightKg <= 0 || heightCm <= 0 || age <= 0) {
            if (showValidationToast) {
                Toast.makeText(this, "Values must be greater than zero.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String gender = spinnerGender.getSelectedItem() != null ? spinnerGender.getSelectedItem().toString() : "Male";
        String activityLevel = spinnerActivity.getSelectedItem() != null ? spinnerActivity.getSelectedItem().toString() : "Moderately Active (3–5 days/week)";
        String goal = spinnerGoal.getSelectedItem() != null ? spinnerGoal.getSelectedItem().toString() : "General fitness";

        // A. BMI
        double bmi = FitnessCalculatorUtils.calculateBMI(weightKg, heightCm);
        String bmiCategory = FitnessCalculatorUtils.getBMICategory(bmi);
        int bmiProgress = FitnessCalculatorUtils.getBMIIndicatorProgress(bmi);

        tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));
        tvBmiCategory.setText(bmiCategory);
        pbBmiIndicator.setProgress(bmiProgress);

        // B. Daily Calories (Mifflin-St Jeor)
        double bmr = FitnessCalculatorUtils.calculateBMR(weightKg, heightCm, age, gender);
        int dailyCalories = FitnessCalculatorUtils.calculateDailyCalories(bmr, activityLevel, goal);
        String calorieNote = FitnessCalculatorUtils.getCalorieGoalExplanation(goal);

        tvDailyCalories.setText(String.format(Locale.getDefault(), "%,d kcal / day", dailyCalories));
        tvBmrValue.setText(String.format(Locale.getDefault(), "Estimated Resting BMR: %,d kcal/day", Math.round(bmr)));
        tvCalorieGoalNote.setText(calorieNote);

        // C. Protein
        int[] proteinRange = FitnessCalculatorUtils.calculateProteinRange(weightKg, goal, activityLevel);
        tvProteinRange.setText(String.format(Locale.getDefault(), "%dg – %dg / day", proteinRange[0], proteinRange[1]));
        int perMeal = Math.round(((proteinRange[0] + proteinRange[1]) / 2f) / 4f);
        tvProteinNote.setText(String.format(Locale.getDefault(), "Approx. ~%dg per meal across 4 meals (Estimate)", perMeal));

        // D. Water Intake
        int waterMl = FitnessCalculatorUtils.calculateWaterIntakeMl(weightKg, activityLevel);
        double waterLitres = waterMl / 1000.0;
        int glasses = Math.max(1, Math.round(waterMl / 250f));

        tvWaterResult.setText(String.format(Locale.getDefault(), "%.2f L  (%,d ml)", waterLitres, waterMl));
        tvWaterGlasses.setText(String.format(Locale.getDefault(), "Approximately %d glasses (250 ml each) • Synced to Water Tracker", glasses));

        // Persist personalized daily water goal in SharedPreferences for Water Tracker & Home
        SharedPreferences prefs = getSharedPreferences("FlexiBotPrefs", MODE_PRIVATE);
        prefs.edit().putInt("daily_water_goal_ml", waterMl).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileListener != null) {
            profileListener.remove();
        }
    }
}