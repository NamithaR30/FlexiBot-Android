package com.example.flexibot.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flexibot.R;
import com.example.flexibot.adapters.ChatAdapter;
import com.example.flexibot.models.ChatMessage;
import com.example.flexibot.models.User;
import com.example.flexibot.services.GeminiChatService;
import com.example.flexibot.utils.FlexyAnimator;
import com.example.flexibot.utils.FlexyBuddyHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AIChatFragment extends Fragment {

    private RecyclerView rvChatMessages;
    private EditText etChatMessage;
    private Button btnSendChat;
    private TextView tvChatTyping, tvChatFlexyStatus;
    private ImageView ivChatHeaderFlexy;

    private final List<ChatMessage> messageList = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private User userProfile;
    private GeminiChatService geminiChatService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);

        geminiChatService = new GeminiChatService();

        rvChatMessages = view.findViewById(R.id.rvChatMessages);
        etChatMessage = view.findViewById(R.id.etChatMessage);
        btnSendChat = view.findViewById(R.id.btnSendChat);
        tvChatTyping = view.findViewById(R.id.tvChatTyping);
        tvChatFlexyStatus = view.findViewById(R.id.tvChatFlexyStatus);
        ivChatHeaderFlexy = view.findViewById(R.id.ivChatHeaderFlexy);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(messageList);
        rvChatMessages.setAdapter(chatAdapter);

        if (messageList.isEmpty()) {
            addMessage(new ChatMessage(
                    "Hey! I'm FLEXY, your personal kettlebell AI buddy. Tap Workout, Macros, or Recovery below—or ask me anything!",
                    false
            ));
        }

        setupQuickPromptChips(view);
        loadUserProfile();

        btnSendChat.setOnClickListener(v -> {
            String text = etChatMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                sendMessageToFlexy(text);
            }
        });

        return view;
    }

    private void setupQuickPromptChips(View view) {
        TextView chipWorkout = view.findViewById(R.id.chipPromptWorkout);
        TextView chipMacros = view.findViewById(R.id.chipPromptMacros);
        TextView chipRecovery = view.findViewById(R.id.chipPromptRecovery);

        chipWorkout.setOnClickListener(v -> sendMessageToFlexy("Build a personalized workout for me today based on my equipment and goal."));
        chipMacros.setOnClickListener(v -> sendMessageToFlexy("Estimate my daily calorie and protein targets for my fitness goal."));
        chipRecovery.setOnClickListener(v -> sendMessageToFlexy("Give me a quick 10-minute post-workout stretch routine."));
    }

    private void loadUserProfile() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;
                    if (doc.exists()) {
                        userProfile = doc.toObject(User.class);
                        if (userProfile != null && userProfile.getName() != null) {
                            tvChatFlexyStatus.setText("Online • Coaching " + userProfile.getName());
                        }
                    }
                });
    }

    private void sendMessageToFlexy(String userText) {
        etChatMessage.setText("");
        addMessage(new ChatMessage(userText, true));

        // Transition FLEXY to THINKING mood & animation
        tvChatTyping.setVisibility(View.VISIBLE);
        tvChatFlexyStatus.setText("Thinking...");
        btnSendChat.setEnabled(false);
        ivChatHeaderFlexy.setImageResource(FlexyBuddyHelper.getAvatarForMood(FlexyBuddyHelper.Mood.THINKING));
        FlexyAnimator.playThinkingAnimation(ivChatHeaderFlexy);

        geminiChatService.sendMessage(userText, userProfile, messageList, new GeminiChatService.ChatCallback() {
            @Override
            public void onSuccess(String reply) {
                if (!isAdded()) return;
                tvChatTyping.setVisibility(View.GONE);
                btnSendChat.setEnabled(true);
                tvChatFlexyStatus.setText(
                        (userProfile != null && userProfile.getName() != null)
                                ? "Online • Coaching " + userProfile.getName()
                                : "Online • Ready to coach"
                );
                ivChatHeaderFlexy.setImageResource(FlexyBuddyHelper.getAvatarForMood(FlexyBuddyHelper.Mood.HAPPY));
                FlexyAnimator.playHappyBounce(ivChatHeaderFlexy, true);
                addMessage(new ChatMessage(reply, false));
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                tvChatTyping.setVisibility(View.GONE);
                btnSendChat.setEnabled(true);
                tvChatFlexyStatus.setText("Connection issue • Tap Send to retry");
                ivChatHeaderFlexy.setImageResource(FlexyBuddyHelper.getAvatarForMood(FlexyBuddyHelper.Mood.ENCOURAGING));
                FlexyAnimator.playEncouragingNod(ivChatHeaderFlexy, true);
                addMessage(new ChatMessage("FLEXY hit a snag: " + errorMessage, false));
            }
        });
    }

    private void addMessage(ChatMessage message) {
        messageList.add(message);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChatMessages.scrollToPosition(messageList.size() - 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ivChatHeaderFlexy != null) {
            FlexyAnimator.startIdleAnimation(ivChatHeaderFlexy);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        FlexyAnimator.stopAnimation(ivChatHeaderFlexy);
    }

    @Override
    public void onDestroyView() {
        FlexyAnimator.stopAnimation(ivChatHeaderFlexy);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (geminiChatService != null) {
            geminiChatService.shutdown();
        }
    }
}