package com.example.flexibot.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flexibot.R;
import com.example.flexibot.adapters.WorkoutHistoryAdapter;
import com.example.flexibot.models.WorkoutSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class WorkoutHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private ProgressBar progressHistory;
    private TextView tvEmpty;
    private List<WorkoutSession> sessionList;
    private WorkoutHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        ImageButton btnBack = findViewById(R.id.btnBackHistory);
        rvHistory = findViewById(R.id.rvWorkoutHistory);
        progressHistory = findViewById(R.id.progressHistory);
        tvEmpty = findViewById(R.id.tvEmptyHistory);

        btnBack.setOnClickListener(v -> finish());

        sessionList = new ArrayList<>();
        adapter = new WorkoutHistoryAdapter(sessionList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("workouts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressHistory.setVisibility(View.GONE);
                    sessionList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        WorkoutSession session = doc.toObject(WorkoutSession.class);
                        if (session != null) {
                            sessionList.add(session);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(sessionList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressHistory.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}