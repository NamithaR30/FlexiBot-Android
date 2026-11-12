package com.example.flexibot.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flexibot.R;
import com.example.flexibot.adapters.ExerciseAdapter;
import com.example.flexibot.models.Exercise;
import com.example.flexibot.models.WorkoutSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActiveWorkoutActivity extends AppCompatActivity {

    private TextView tvTitle, tvTimer, tvProgress;
    private Button btnPauseResume, btnFinish;
    private ProgressBar pbWorkout, progressSaving;
    private RecyclerView rvExercises;

    private List<Exercise> exercises;
    private String workoutTitle;
    private String workoutCategory;

    private int secondsElapsed = 0;
    private boolean isRunning = true;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                secondsElapsed++;
                int minutes = secondsElapsed / 60;
                int secs = secondsElapsed % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, secs));
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_workout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        workoutTitle = getIntent().getStringExtra("WORKOUT_TITLE");
        workoutCategory = getIntent().getStringExtra("WORKOUT_CATEGORY");
        @SuppressWarnings("unchecked")
        ArrayList<Exercise> passedList = (ArrayList<Exercise>) getIntent().getSerializableExtra("EXERCISES");
        exercises = passedList != null ? passedList : new ArrayList<>();

        tvTitle = findViewById(R.id.tvActiveWorkoutTitle);
        tvTimer = findViewById(R.id.tvWorkoutTimer);
        tvProgress = findViewById(R.id.tvActiveProgress);
        btnPauseResume = findViewById(R.id.btnPauseResume);
        btnFinish = findViewById(R.id.btnFinishWorkout);
        pbWorkout = findViewById(R.id.pbActiveWorkout);
        progressSaving = findViewById(R.id.progressSavingWorkout);
        rvExercises = findViewById(R.id.rvActiveExercises);

        tvTitle.setText(workoutTitle != null ? workoutTitle : "FlexiBot Workout");

        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        ExerciseAdapter adapter = new ExerciseAdapter(exercises, true, this::updateProgress);
        rvExercises.setAdapter(adapter);

        updateProgress();
        timerHandler.postDelayed(timerRunnable, 1000);

        btnPauseResume.setOnClickListener(v -> {
            isRunning = !isRunning;
            btnPauseResume.setText(isRunning ? "PAUSE" : "RESUME");
        });

        btnFinish.setOnClickListener(v -> confirmAndSaveWorkout());
    }

    private void updateProgress() {
        int completedCount = 0;
        for (Exercise e : exercises) {
            if (e.isCompleted()) completedCount++;
        }
        int total = exercises.size();
        tvProgress.setText("Exercises Completed: " + completedCount + " / " + total);
        int percent = total > 0 ? (completedCount * 100) / total : 0;
        pbWorkout.setProgress(percent);
    }

    private void confirmAndSaveWorkout() {
        int completedCount = 0;
        for (Exercise e : exercises) {
            if (e.isCompleted()) completedCount++;
        }

        if (completedCount < exercises.size()) {
            final int finalCompleted = completedCount;
            new AlertDialog.Builder(this)
                    .setTitle("Finish Early?")
                    .setMessage("You have completed " + finalCompleted + " of " + exercises.size() + " exercises. Save this workout anyway?")
                    .setPositiveButton("Save Workout", (dialog, which) -> saveWorkoutToFirestore(finalCompleted))
                    .setNegativeButton("Keep Going", null)
                    .show();
        } else {
            saveWorkoutToFirestore(completedCount);
        }
    }

    private void saveWorkoutToFirestore(int completedCount) {
        if (mAuth.getCurrentUser() == null) return;

        isRunning = false;
        btnFinish.setEnabled(false);
        progressSaving.setVisibility(View.VISIBLE);

        String uid = mAuth.getCurrentUser().getUid();
        String sessionId = db.collection("users").document(uid).collection("workouts").document().getId();

        int durationMinutes = Math.max(1, secondsElapsed / 60);
        int estimatedCalories = durationMinutes * 7 + (completedCount * 12);

        WorkoutSession session = new WorkoutSession(
                sessionId,
                workoutTitle != null ? workoutTitle : "Custom Workout",
                workoutCategory != null ? workoutCategory : "Full body",
                durationMinutes,
                completedCount,
                exercises.size(),
                estimatedCalories,
                System.currentTimeMillis()
        );

        db.collection("users").document(uid).collection("workouts").document(sessionId)
                .set(session)
                .addOnSuccessListener(aVoid -> {
                    progressSaving.setVisibility(View.GONE);
                    Toast.makeText(this, "Workout Completed & Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressSaving.setVisibility(View.GONE);
                    btnFinish.setEnabled(true);
                    Toast.makeText(this, "Failed to save workout: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
    }
}