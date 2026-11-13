
package com.example.flexibot.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.flexibot.R;
import com.example.flexibot.activities.LoginActivity;
import com.example.flexibot.models.User;
import com.example.flexibot.utils.FlexyAnimator;
import com.example.flexibot.utils.FlexyBuddyHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvProfileEmail, tvProfileFlexySpeech;
    private ImageView ivProfileFlexyAvatar;

    private EditText etProfileName, etProfileAge;
    private EditText etProfileWeight, etProfileHeight;

    private Spinner spinnerGoal, spinnerEquipment;

    private Button btnSaveProfile, btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileFlexySpeech = view.findViewById(R.id.tvProfileFlexySpeech);
        ivProfileFlexyAvatar = view.findViewById(R.id.ivProfileFlexyAvatar);

        etProfileName = view.findViewById(R.id.etProfileName);
        etProfileAge = view.findViewById(R.id.etProfileAge);
        etProfileWeight = view.findViewById(R.id.etProfileWeight);
        etProfileHeight = view.findViewById(R.id.etProfileHeight);

        spinnerGoal = view.findViewById(R.id.spinnerGoal);
        spinnerEquipment = view.findViewById(R.id.spinnerEquipment);

        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnLogout = view.findViewById(R.id.btnLogout);

        setupSpinners();

        if (mAuth.getCurrentUser() != null) {
            tvProfileEmail.setText(mAuth.getCurrentUser().getEmail());
            loadUserProfile();
        }

        btnSaveProfile.setOnClickListener(v -> saveUserProfile());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
        });

        return view;
    }

    private void setupSpinners() {

        ArrayAdapter<CharSequence> goalAdapter =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.fitness_goals,
                        R.layout.item_spinner
                );

        spinnerGoal.setAdapter(goalAdapter);

        ArrayAdapter<CharSequence> equipmentAdapter =
                ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.equipment_options,
                        R.layout.item_spinner
                );

        spinnerEquipment.setAdapter(equipmentAdapter);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {

        if (value == null || spinner.getAdapter() == null) return;

        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {

            String option = spinner.getAdapter().getItem(i).toString();

            if (option.trim().equalsIgnoreCase(value.trim())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void loadUserProfile() {

        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!isAdded()) return;

                    if (documentSnapshot.exists()) {

                        User user = documentSnapshot.toObject(User.class);

                        if (user != null) {

                            etProfileName.setText(user.getName());
                            etProfileAge.setText(String.valueOf(user.getAge()));
                            etProfileWeight.setText(String.valueOf(user.getWeight()));
                            etProfileHeight.setText(String.valueOf(user.getHeight()));

                            setSpinnerSelection(
                                    spinnerGoal,
                                    user.getFitnessGoal()
                            );

                            setSpinnerSelection(
                                    spinnerEquipment,
                                    user.getEquipment()
                            );

                            tvProfileFlexySpeech.setText(
                                    "Welcome back, " + user.getName() +
                                            "! Your fitness profile is fully synced."
                            );
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    if (isAdded() && getContext() != null) {
                        Toast.makeText(
                                getContext(),
                                "Failed to load profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void saveUserProfile() {

        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        String name = etProfileName.getText().toString().trim();
        String ageStr = etProfileAge.getText().toString().trim();
        String weightStr = etProfileWeight.getText().toString().trim();
        String heightStr = etProfileHeight.getText().toString().trim();

        String goal = spinnerGoal.getSelectedItem().toString();
        String equipment = spinnerEquipment.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(ageStr) ||
                TextUtils.isEmpty(weightStr) ||
                TextUtils.isEmpty(heightStr)) {

            Toast.makeText(
                    getContext(),
                    "Please fill in all required fields.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        int age;
        double weight, height;

        try {
            age = Integer.parseInt(ageStr);
            weight = Double.parseDouble(weightStr);
            height = Double.parseDouble(heightStr);

        } catch (NumberFormatException e) {

            Toast.makeText(
                    getContext(),
                    "Please enter valid numbers for age, weight, and height.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnSaveProfile.setEnabled(false);

        db.collection("users").document(uid)
                .update(
                        "name", name,
                        "age", age,
                        "weight", weight,
                        "height", height,
                        "fitnessGoal", goal,
                        "equipment", equipment
                )
                .addOnSuccessListener(aVoid -> {

                    if (!isAdded()) return;

                    btnSaveProfile.setEnabled(true);

                    ivProfileFlexyAvatar.setImageResource(
                            FlexyBuddyHelper.getAvatarForMood(
                                    FlexyBuddyHelper.Mood.HAPPY
                            )
                    );

                    tvProfileFlexySpeech.setText(
                            "Profile updated successfully, " + name + "!"
                    );

                    FlexyAnimator.playHappyBounce(
                            ivProfileFlexyAvatar,
                            true
                    );

                    Toast.makeText(
                            getContext(),
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    if (!isAdded()) return;

                    btnSaveProfile.setEnabled(true);

                    Toast.makeText(
                            getContext(),
                            "Failed to update profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ivProfileFlexyAvatar != null) {
            FlexyAnimator.startIdleAnimation(ivProfileFlexyAvatar);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        FlexyAnimator.stopAnimation(ivProfileFlexyAvatar);
    }

    @Override
    public void onDestroyView() {
        FlexyAnimator.stopAnimation(ivProfileFlexyAvatar);
        super.onDestroyView();
    }
}
