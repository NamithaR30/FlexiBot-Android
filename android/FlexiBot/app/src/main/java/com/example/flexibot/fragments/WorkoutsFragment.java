package com.example.flexibot.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flexibot.R;
import com.example.flexibot.activities.ActiveWorkoutActivity;
import com.example.flexibot.activities.WorkoutHistoryActivity;
import com.example.flexibot.adapters.ExerciseAdapter;
import com.example.flexibot.models.Exercise;
import com.example.flexibot.models.User;
import com.example.flexibot.utils.WorkoutGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutsFragment extends Fragment {

    private TextView tvProfileSummary, tvPlanTitle, tvPlanMeta;
    private LinearLayout layoutWeeklyDays, layoutCategories;
    private Button btnStartWorkout;
    private ImageButton btnViewHistory;
    private ProgressBar progressWorkouts;
    private RecyclerView rvExercises;

    private User currentUser;
    private List<Exercise> currentExercises = new ArrayList<>();
    private ExerciseAdapter exerciseAdapter;

    private String selectedDay = "Mon";
    private String selectedCategory = "Full body";

    private final String[] daysOfWeek = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private final String[] categories = {"Full body", "Upper body", "Lower body", "Core", "Cardio", "Strength training"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workouts, container, false);

        tvProfileSummary = view.findViewById(R.id.tvWorkoutProfileSummary);
        tvPlanTitle = view.findViewById(R.id.tvPlanTitle);
        tvPlanMeta = view.findViewById(R.id.tvPlanMeta);
        layoutWeeklyDays = view.findViewById(R.id.layoutWeeklyDays);
        layoutCategories = view.findViewById(R.id.layoutCategories);
        btnStartWorkout = view.findViewById(R.id.btnStartWorkout);
        btnViewHistory = view.findViewById(R.id.btnViewHistory);
        progressWorkouts = view.findViewById(R.id.progressWorkouts);
        rvExercises = view.findViewById(R.id.rvExercises);

        // Default selected day to today
        String todayShort = new SimpleDateFormat("EEE", Locale.ENGLISH).format(new Date());
        for (String d : daysOfWeek) {
            if (d.equalsIgnoreCase(todayShort)) {
                selectedDay = d;
                break;
            }
        }

        rvExercises.setLayoutManager(new LinearLayoutManager(getContext()));
        exerciseAdapter = new ExerciseAdapter(currentExercises, false, null);
        rvExercises.setAdapter(exerciseAdapter);

        btnViewHistory.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), WorkoutHistoryActivity.class)));

        btnStartWorkout.setOnClickListener(v -> {
            if (currentExercises.isEmpty()) {
                Toast.makeText(getContext(), "No exercises loaded yet.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(getActivity(), ActiveWorkoutActivity.class);
            intent.putExtra("WORKOUT_TITLE", tvPlanTitle.getText().toString());
            intent.putExtra("WORKOUT_CATEGORY", selectedCategory);
            intent.putExtra("EXERCISES", new ArrayList<>(currentExercises));
            startActivity(intent);
        });

        loadUserProfileAndGenerate();

        return view;
    }

    private void loadUserProfileAndGenerate() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressWorkouts.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                    }
                    if (currentUser != null) {
                        tvProfileSummary.setText(currentUser.getFitnessGoal() + " • " +
                                currentUser.getFitnessLevel() + " • " + currentUser.getEquipment());
                        selectedCategory = WorkoutGenerator.getCategoryForDay(selectedDay, currentUser.getFitnessGoal());
                    } else {
                        tvProfileSummary.setText("General fitness • Beginner • No equipment");
                    }

                    buildDayChips();
                    buildCategoryChips();
                    refreshWorkoutPlan();
                })
                .addOnFailureListener(e -> {
                    progressWorkouts.setVisibility(View.GONE);
                    tvProfileSummary.setText("Standard Plan");
                    buildDayChips();
                    buildCategoryChips();
                    refreshWorkoutPlan();
                });
    }

    private void buildDayChips() {
        if (getContext() == null) return;
        layoutWeeklyDays.removeAllViews();

        for (String day : daysOfWeek) {
            TextView chip = createChipView(day, day.equalsIgnoreCase(selectedDay));
            chip.setOnClickListener(v -> {
                selectedDay = day;
                String goal = currentUser != null ? currentUser.getFitnessGoal() : "General fitness";
                selectedCategory = WorkoutGenerator.getCategoryForDay(selectedDay, goal);
                buildDayChips();
                buildCategoryChips();
                refreshWorkoutPlan();
            });
            layoutWeeklyDays.addView(chip);
        }
    }

    private void buildCategoryChips() {
        if (getContext() == null) return;
        layoutCategories.removeAllViews();

        for (String category : categories) {
            TextView chip = createChipView(category, category.equalsIgnoreCase(selectedCategory));
            chip.setOnClickListener(v -> {
                selectedCategory = category;
                buildCategoryChips();
                refreshWorkoutPlan();
            });
            layoutCategories.addView(chip);
        }
    }

    private TextView createChipView(String label, boolean isSelected) {
        TextView tv = new TextView(getContext());
        tv.setText(label);
        tv.setTextSize(13f);
        tv.setPadding(36, 18, 36, 18);
        tv.setTextColor(ContextCompat.getColor(requireContext(), isSelected ? R.color.white : R.color.grey_text));
        tv.setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(16);
        tv.setLayoutParams(params);
        return tv;
    }

    private void refreshWorkoutPlan() {
        currentExercises = WorkoutGenerator.generateWorkout(currentUser, selectedCategory);
        exerciseAdapter.updateExercises(currentExercises);

        int duration = WorkoutGenerator.estimateDurationMinutes(currentExercises);
        tvPlanTitle.setText(selectedDay + " • " + selectedCategory);
        tvPlanMeta.setText(currentExercises.size() + " Exercises • ~" + duration + " mins");
    }
}