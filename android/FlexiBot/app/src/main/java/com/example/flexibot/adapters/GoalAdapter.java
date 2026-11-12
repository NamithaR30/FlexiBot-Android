package com.example.flexibot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flexibot.R;
import com.example.flexibot.models.FitnessGoalTarget;

import java.util.List;
import java.util.Locale;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    public interface OnGoalActionListener {
        void onUpdateProgressClicked(FitnessGoalTarget goal);
        void onDeleteGoalClicked(FitnessGoalTarget goal);
    }

    private final List<FitnessGoalTarget> goals;
    private final OnGoalActionListener listener;

    public GoalAdapter(List<FitnessGoalTarget> goals, OnGoalActionListener listener) {
        this.goals = goals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal_target, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        FitnessGoalTarget goal = goals.get(position);
        int pct = goal.calculateCompletionPercentage();

        holder.tvCategory.setText(goal.getGoalCategory());
        holder.tvTitle.setText(goal.getTitle());
        holder.tvPercent.setText(pct + "%");
        holder.pbGoal.setProgress(pct);

        String unit = goal.getUnit() != null ? goal.getUnit() : "";
        holder.tvStats.setText(String.format(
                Locale.getDefault(),
                "Start: %.1f %s  •  Current: %.1f %s  •  Target: %.1f %s",
                goal.getStartValue(), unit,
                goal.getCurrentValue(), unit,
                goal.getTargetValue(), unit
        ));

        holder.btnUpdate.setOnClickListener(v -> {
            if (listener != null) listener.onUpdateProgressClicked(goal);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteGoalClicked(goal);
        });
    }

    @Override
    public int getItemCount() {
        return goals != null ? goals.size() : 0;
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvTitle, tvPercent, tvStats;
        ProgressBar pbGoal;
        Button btnUpdate, btnDelete;

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvGoalItemCategory);
            tvTitle = itemView.findViewById(R.id.tvGoalItemTitle);
            tvPercent = itemView.findViewById(R.id.tvGoalItemPercent);
            tvStats = itemView.findViewById(R.id.tvGoalItemStats);
            pbGoal = itemView.findViewById(R.id.pbGoalItem);
            btnUpdate = itemView.findViewById(R.id.btnUpdateGoalProgress);
            btnDelete = itemView.findViewById(R.id.btnDeleteGoal);
        }
    }
}