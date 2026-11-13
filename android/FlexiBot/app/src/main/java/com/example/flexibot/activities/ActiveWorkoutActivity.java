package com.example.flexibot.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.flexibot.utils.FlexyAnimator;
import com.example.flexibot.utils.FlexyBuddyHelper;
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

    private ImageView ivWorkoutFlexyAvatar;
    private TextView tvWorkoutFlexyBadge, tvWorkoutFlexyTip;

    private List<Exercise> exercises;
    private String workoutTitle;
    private String workoutCategory;

    private int secondsElapsed = 0;
    private int lastCompletedCount = 0;
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
        ArrayList<Exercise> passedList =
                (ArrayList<Exercise>) getIntent().getSerializableExtra("EXERCISES");

        exercises = passedList != null ? passedList : new ArrayList<>();

        tvTitle = findViewById(R.id.tvActiveWorkoutTitle);
        tvTimer = findViewById(R.id.tvWorkoutTimer);
        tvProgress = findViewById(R.id.tvActiveProgress);

        btnPauseResume = findViewById(R.id.btnPauseResume);
        btnFinish = findViewById(R.id.btnFinishWorkout);

        pbWorkout = findViewById(R.id.pbActiveWorkout);
        progressSaving = findViewById(R.id.progressSavingWorkout);
        rvExercises = findViewById(R.id.rvActiveExercises);

        ivWorkoutFlexyAvatar = findViewById(R.id.ivWorkoutFlexyAvatar);
        tvWorkoutFlexyBadge = findViewById(R.id.tvWorkoutFlexyBadge);
        tvWorkoutFlexyTip = findViewById(R.id.tvWorkoutFlexyTip);

        tvTitle.setText(workoutTitle != null ? workoutTitle : "FlexiBot Workout");

        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        ExerciseAdapter adapter = new ExerciseAdapter(exercises, true, this::updateProgress);
        rvExercises.setAdapter(adapter);

        updateProgress();

        timerHandler.postDelayed(timerRunnable, 1000);

        btnPauseResume.setOnClickListener(v -> {
            isRunning = !isRunning;
            btnPauseResume.setText(isRunning ? "PAUSE" : "RESUME");
            String nextExercise = getNextIncompleteExerciseName();
            updateFlexyWorkoutBanner(nextExercise, !isRunning);
        });

        btnFinish.setOnClickListener(v -> confirmAndSaveWorkout());
    }

    private String getNextIncompleteExerciseName() {
        for (Exercise e : exercises) {
            if (!e.isCompleted() && e.getName() != null) {
                return e.getName();
            }
        }
        return exercises.isEmpty() ? "Workout" : exercises.get(exercises.size() - 1).getName();
    }

    private void updateFlexyWorkoutBanner(String exerciseName, boolean isResting) {
        if (ivWorkoutFlexyAvatar == null || tvWorkoutFlexyBadge == null || tvWorkoutFlexyTip == null) {
            return;
        }

        FlexyBuddyHelper.BuddyState cue =
                FlexyBuddyHelper.getWorkoutCoachingCue(exerciseName, isResting);

        ivWorkoutFlexyAvatar.setImageResource(cue.avatarResId);
        tvWorkoutFlexyBadge.setText(cue.moodLabel);
        tvWorkoutFlexyTip.setText(cue.message);

        if (isResting) {
            FlexyAnimator.playHappyBounce(ivWorkoutFlexyAvatar, true);
        } else {
            FlexyAnimator.playEncouragingNod(ivWorkoutFlexyAvatar, true);
        }
    }

    private void updateProgress() {
        int completedCount = 0;
        for (Exercise e : exercises) {
            if (e.isCompleted()) {
                completedCount++;
            }
        }

        int total = exercises.size();
        tvProgress.setText("Exercises Completed: " + completedCount + " / " + total);

        int percent = total > 0 ? (completedCount * 100) / total : 0;
        pbWorkout.setProgress(percent);

        if (ivWorkoutFlexyAvatar != null) {
            if (total > 0 && completedCount == total) {
                ivWorkoutFlexyAvatar.setImageResource(
                        FlexyBuddyHelper.getAvatarForMood(FlexyBuddyHelper.Mood.EXCITED)
                );
                tvWorkoutFlexyBadge.setText("FLEXY • ALL EXERCISES DONE!");
                tvWorkoutFlexyTip.setText("Every single exercise checked off! Tap COMPLETE WORKOUT to lock in your progress!");
                FlexyAnimator.playExcitedJump(ivWorkoutFlexyAvatar, true);
            } else if (completedCount > lastCompletedCount) {
                String nextName = getNextIncompleteExerciseName();
                ivWorkoutFlexyAvatar.setImageResource(
                        FlexyBuddyHelper.getAvatarForMood(FlexyBuddyHelper.Mood.HAPPY)
                );
                tvWorkoutFlexyBadge.setText("FLEXY • NICE SET!");
                tvWorkoutFlexyTip.setText("Great work! " + completedCount + " of " + total + " done. Up next: " + nextName + ".");
                FlexyAnimator.playHappyBounce(ivWorkoutFlexyAvatar, true);
            } else {
                updateFlexyWorkoutBanner(getNextIncompleteExerciseName(), !isRunning);
            }
        }

        lastCompletedCount = completedCount;
    }

    private void confirmAndSaveWorkout() {
        int completedCount = 0;
        for (Exercise e : exercises) {
            if (e.isCompleted()) {
                completedCount++;
            }
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
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

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

        db.collection("users")
                .document(uid)
                .collection("workouts")
                .document(sessionId)
                .set(session)
                .addOnSuccessListener(aVoid -> {
                    progressSaving.setVisibility(View.GONE);
                    if (ivWorkoutFlexyAvatar != null) {
                        FlexyBuddyHelper.BuddyState celebration =
                                FlexyBuddyHelper.getWorkoutCelebration("Champ", completedCount, durationMinutes);
                        ivWorkoutFlexyAvatar.setImageResource(
                                FlexyBuddyHelper.getAvatarForMood(FlexyBuddyHelper.Mood.CELEBRATING)
                        );
                        tvWorkoutFlexyBadge.setText("FLEXY • WORKOUT CELEBRATION!");
                        tvWorkoutFlexyTip.setText(celebration.message);
                        FlexyAnimator.playCelebratingSpin(ivWorkoutFlexyAvatar, false);
                    }
                    Toast.makeText(this, "Workout Completed & Saved!", Toast.LENGTH_SHORT).show();
                    timerHandler.postDelayed(this::finish, 650);
                })
                .addOnFailureListener(e -> {
                    progressSaving.setVisibility(View.GONE);
                    btnFinish.setEnabled(true);
                    Toast.makeText(this, "Failed to save workout: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ivWorkoutFlexyAvatar != null) {
            FlexyAnimator.startIdleAnimation(ivWorkoutFlexyAvatar);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FlexyAnimator.stopAnimation(ivWorkoutFlexyAvatar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FlexyAnimator.stopAnimation(ivWorkoutFlexyAvatar);
        timerHandler.removeCallbacks(timerRunnable);
    }
}