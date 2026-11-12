package com.example.flexibot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flexibot.R;
import com.example.flexibot.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileSetupActivity extends AppCompatActivity {

    private ScrollView scrollForm;
    private ProgressBar progressProfileCheck;
    private EditText etName, etAge, etHeight, etWeight;
    private Spinner spinnerGender, spinnerGoal, spinnerLevel, spinnerEquipment;
    private Button btnSaveProfile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = mAuth.getCurrentUser().getUid();

        initViews();
        setupSpinners();
        checkExistingProfile();

        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void initViews() {
        scrollForm = findViewById(R.id.scrollForm);
        progressProfileCheck = findViewById(R.id.progressProfileCheck);
        etName = findViewById(R.id.etProfileName);
        etAge = findViewById(R.id.etProfileAge);
        etHeight = findViewById(R.id.etProfileHeight);
        etWeight = findViewById(R.id.etProfileWeight);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerGoal = findViewById(R.id.spinnerGoal);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        spinnerEquipment = findViewById(R.id.spinnerEquipment);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void setupSpinners() {
        // We use our custom item_spinner.xml for white text
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender_options, R.layout.item_spinner);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(this, R.array.fitness_goals, R.layout.item_spinner);
        spinnerGoal.setAdapter(goalAdapter);

        ArrayAdapter<CharSequence> levelAdapter = ArrayAdapter.createFromResource(this, R.array.fitness_levels, R.layout.item_spinner);
        spinnerLevel.setAdapter(levelAdapter);

        ArrayAdapter<CharSequence> equipmentAdapter = ArrayAdapter.createFromResource(this, R.array.equipment_options, R.layout.item_spinner);
        spinnerEquipment.setAdapter(equipmentAdapter);
    }

    private void checkExistingProfile() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressProfileCheck.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        // User already set up profile, go to Home
                        goToHome();
                    } else {
                        // No profile found, show the form
                        scrollForm.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressProfileCheck.setVisibility(View.GONE);
                    Toast.makeText(this, "Error checking profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    scrollForm.setVisibility(View.VISIBLE);
                });
    }

    private void saveProfileData() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        String gender = spinnerGender.getSelectedItem().toString();
        String goal = spinnerGoal.getSelectedItem().toString();
        String level = spinnerLevel.getSelectedItem().toString();
        String equipment = spinnerEquipment.getSelectedItem().toString();

        // Basic Validation
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ageStr) || TextUtils.isEmpty(heightStr) || TextUtils.isEmpty(weightStr)) {
            Toast.makeText(this, "Please fill in all text fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gender.contains("Select") || goal.contains("Select") || level.contains("Select") || equipment.contains("Select")) {
            Toast.makeText(this, "Please make a selection in all dropdowns", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);
        double height = Double.parseDouble(heightStr);
        double weight = Double.parseDouble(weightStr);
        String email = mAuth.getCurrentUser().getEmail();

        btnSaveProfile.setEnabled(false);
        progressProfileCheck.setVisibility(View.VISIBLE);
        scrollForm.setVisibility(View.GONE);

        User newUser = new User(name, email, age, gender, height, weight, goal, level, equipment, System.currentTimeMillis());

        db.collection("users").document(userId).set(newUser)
                .addOnSuccessListener(aVoid -> goToHome())
                .addOnFailureListener(e -> {
                    btnSaveProfile.setEnabled(true);
                    progressProfileCheck.setVisibility(View.GONE);
                    scrollForm.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void goToHome() {
        startActivity(new Intent(ProfileSetupActivity.this, MainActivity.class));
        finish();
    }
}