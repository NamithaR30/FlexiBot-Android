package com.example.flexibot.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flexibot.R;
import com.example.flexibot.models.WeightEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeightHistoryAdapter extends RecyclerView.Adapter<WeightHistoryAdapter.WeightViewHolder> {

    private final List<WeightEntry> entries;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public WeightHistoryAdapter(List<WeightEntry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public WeightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weight_entry, parent, false);
        return new WeightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeightViewHolder holder, int position) {
        WeightEntry current = entries.get(position);
        holder.tvValue.setText(String.format(Locale.getDefault(), "%.1f kg", current.getWeightKg()));
        holder.tvDate.setText(dateFormat.format(new Date(current.getTimestamp())));

        if (!TextUtils.isEmpty(current.getNote())) {
            holder.tvNote.setVisibility(View.VISIBLE);
            holder.tvNote.setText(current.getNote());
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        // entries are sorted newest -> oldest, so position + 1 is the prior chronological entry
        if (position + 1 < entries.size()) {
            WeightEntry previous = entries.get(position + 1);
            double diff = current.getWeightKg() - previous.getWeightKg();
            if (Math.abs(diff) < 0.05) {
                holder.tvDelta.setText("0.0 kg");
            } else if (diff > 0) {
                holder.tvDelta.setText(String.format(Locale.getDefault(), "+%.1f kg", diff));
            } else {
                holder.tvDelta.setText(String.format(Locale.getDefault(), "%.1f kg", diff));
            }
        } else {
            holder.tvDelta.setText("Starting");
        }
    }

    @Override
    public int getItemCount() {
        return entries != null ? entries.size() : 0;
    }

    static class WeightViewHolder extends RecyclerView.ViewHolder {
        TextView tvValue, tvDate, tvNote, tvDelta;

        WeightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvValue = itemView.findViewById(R.id.tvWeightItemValue);
            tvDate = itemView.findViewById(R.id.tvWeightItemDate);
            tvNote = itemView.findViewById(R.id.tvWeightItemNote);
            tvDelta = itemView.findViewById(R.id.tvWeightItemDelta);
        }
    }
}