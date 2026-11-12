package com.example.flexibot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flexibot.R;
import com.example.flexibot.models.WorkoutSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutHistoryAdapter extends RecyclerView.Adapter<WorkoutHistoryAdapter.HistoryViewHolder> {

    private final List<WorkoutSession> sessionList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());

    public WorkoutHistoryAdapter(List<WorkoutSession> sessionList) {
        this.sessionList = sessionList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        WorkoutSession session = sessionList.get(position);
        holder.tvCategory.setText(session.getCategory());
        holder.tvTitle.setText(session.getWorkoutTitle());
        holder.tvDate.setText(dateFormat.format(new Date(session.getTimestamp())));
        holder.tvDuration.setText("Duration: " + session.getDurationMinutes() + " min");
        holder.tvExercises.setText("Completed: " + session.getExercisesCompleted() + "/" + session.getTotalExercises());
    }

    @Override
    public int getItemCount() {
        return sessionList != null ? sessionList.size() : 0;
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvTitle, tvDate, tvDuration, tvExercises;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvHistoryCategory);
            tvTitle = itemView.findViewById(R.id.tvHistoryTitle);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            tvDuration = itemView.findViewById(R.id.tvHistoryDuration);
            tvExercises = itemView.findViewById(R.id.tvHistoryExercises);
        }
    }
}