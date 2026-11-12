package com.example.flexibot.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flexibot.R;
import com.example.flexibot.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;

    private final List<ChatMessage> messages;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (ChatMessage.SENDER_USER.equals(msg.getSender())) {
            return VIEW_TYPE_USER;
        }
        return VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_bot, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        String formattedTime = timeFormat.format(new Date(msg.getTimestamp()));

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).tvMessage.setText(msg.getMessage());
            ((UserMessageViewHolder) holder).tvTime.setText(formattedTime);
        } else if (holder instanceof BotMessageViewHolder) {
            ((BotMessageViewHolder) holder).tvMessage.setText(msg.getMessage());
            ((BotMessageViewHolder) holder).tvTime.setText(formattedTime);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvUserMessage);
            tvTime = itemView.findViewById(R.id.tvUserTime);
        }
    }

    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvBotMessage);
            tvTime = itemView.findViewById(R.id.tvBotTime);
        }
    }
}