package com.example.flexibot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flexibot.R;
import com.example.flexibot.models.Exercise;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    public interface OnExerciseCheckedListener {
        void onExerciseCheckedChanged();
    }

    private List<Exercise> exerciseList;
    private final boolean isInteractiveMode;
    private final OnExerciseCheckedListener listener;

    public ExerciseAdapter(List<Exercise> exerciseList, boolean isInteractiveMode, OnExerciseCheckedListener listener) {
        this.exerciseList = exerciseList;
        this.isInteractiveMode = isInteractiveMode;
        this.listener = listener;
    }

    public void updateExercises(List<Exercise> newExercises) {
        this.exerciseList = newExercises;
        notifyDataSetChanged();
    }

    public List<Exercise> getExercises() {
        return exerciseList;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);

        holder.tvMuscle.setText(exercise.getMuscleGroup());
        holder.tvName.setText(exercise.getName());
        holder.tvSetsReps.setText(exercise.getSets() + " Sets × " + exercise.getReps());
        holder.tvRest.setText("Rest: " + exercise.getRestSeconds() + "s");
        holder.tvDesc.setText(exercise.getDescription());

        if (isInteractiveMode) {
            holder.cbCompleted.setVisibility(View.VISIBLE);
            holder.cbCompleted.setOnCheckedChangeListener(null);
            holder.cbCompleted.setChecked(exercise.isCompleted());

            holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                exercise.setCompleted(isChecked);
                if (listener != null) {
                    listener.onExerciseCheckedChanged();
                }
            });

            holder.itemView.setOnClickListener(v -> holder.cbCompleted.setChecked(!holder.cbCompleted.isChecked()));
        } else {
            holder.cbCompleted.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return exerciseList != null ? exerciseList.size() : 0;
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView tvMuscle, tvName, tvSetsReps, tvRest, tvDesc;
        CheckBox cbCompleted;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMuscle = itemView.findViewById(R.id.tvExerciseMuscle);
            tvName = itemView.findViewById(R.id.tvExerciseName);
            tvSetsReps = itemView.findViewById(R.id.tvExerciseSetsReps);
            tvRest = itemView.findViewById(R.id.tvExerciseRest);
            tvDesc = itemView.findViewById(R.id.tvExerciseDesc);
            cbCompleted = itemView.findViewById(R.id.cbExerciseCompleted);
        }
    }
}