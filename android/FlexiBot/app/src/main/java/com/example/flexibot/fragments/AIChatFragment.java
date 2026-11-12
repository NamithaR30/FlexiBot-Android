package com.example.flexibot.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flexibot.R;
import com.example.flexibot.adapters.ChatAdapter;
import com.example.flexibot.models.ChatMessage;
import com.example.flexibot.models.User;
import com.example.flexibot.services.GeminiChatService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class AIChatFragment extends Fragment {

    private TextView tvChatProfileContext;
    private RecyclerView rvChatMessages;
    private LinearLayout layoutTypingIndicator;
    private EditText etChatInput;
    private ImageButton btnSendMessage, btnClearChat;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GeminiChatService chatService;

    private User currentUserProfile;
    private final List<ChatMessage> messageList = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private boolean isSending = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        chatService = new GeminiChatService();

        tvChatProfileContext = view.findViewById(R.id.tvChatProfileContext);
        rvChatMessages = view.findViewById(R.id.rvChatMessages);
        layoutTypingIndicator = view.findViewById(R.id.layoutTypingIndicator);
        etChatInput = view.findViewById(R.id.etChatInput);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);
        btnClearChat = view.findViewById(R.id.btnClearChat);

        TextView chip1 = view.findViewById(R.id.chipPrompt1);
        TextView chip2 = view.findViewById(R.id.chipPrompt2);
        TextView chip3 = view.findViewById(R.id.chipPrompt3);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(messageList);
        rvChatMessages.setAdapter(chatAdapter);

        btnSendMessage.setOnClickListener(v -> {
            String text = etChatInput.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                etChatInput.setText("");
                handleUserSend(text);
            }
        });

        chip1.setOnClickListener(v -> handleUserSend(chip1.getText().toString()));
        chip2.setOnClickListener(v -> handleUserSend(chip2.getText().toString()));
        chip3.setOnClickListener(v -> handleUserSend(chip3.getText().toString()));

        btnClearChat.setOnClickListener(v -> confirmClearConversation());

        loadProfileAndHistory();

        return view;
    }

    private void loadProfileAndHistory() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        // 1. Fetch user profile for personalization
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserProfile = documentSnapshot.toObject(User.class);
                    }
                    if (currentUserProfile != null) {
                        tvChatProfileContext.setText("Personalized for " + currentUserProfile.getName() +
                                " • " + currentUserProfile.getFitnessGoal());
                    } else {
                        tvChatProfileContext.setText("Personalized AI Coaching Active");
                    }
                    // 2. Load previous chat history
                    loadChatHistory(uid);
                })
                .addOnFailureListener(e -> {
                    tvChatProfileContext.setText("AI Coaching Ready");
                    loadChatHistory(uid);
                });
    }

    private void loadChatHistory(String uid) {
        db.collection("users").document(uid).collection("chatHistory")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    messageList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) {
                            messageList.add(msg);
                        }
                    }

                    if (messageList.isEmpty()) {
                        addWelcomeGreeting();
                    } else {
                        chatAdapter.notifyDataSetChanged();
                        scrollToBottom();
                    }
                })
                .addOnFailureListener(e -> {
                    if (messageList.isEmpty()) {
                        addWelcomeGreeting();
                    }
                });
    }

    private void addWelcomeGreeting() {
        String name = (currentUserProfile != null && currentUserProfile.getName() != null)
                ? currentUserProfile.getName() : "there";
        String goal = (currentUserProfile != null && currentUserProfile.getFitnessGoal() != null)
                ? currentUserProfile.getFitnessGoal() : "your fitness goals";
        String equip = (currentUserProfile != null && currentUserProfile.getEquipment() != null)
                ? currentUserProfile.getEquipment() : "your available setup";

        String greeting = "Hey " + name + "! I'm FlexiBot, your personal AI fitness coach. " +
                "I've loaded your profile (" + goal + " • " + equip + "). " +
                "Ask me to build a custom workout, calculate protein targets, or adjust any exercise!";

        ChatMessage welcomeMsg = new ChatMessage("welcome", ChatMessage.SENDER_BOT, greeting, System.currentTimeMillis());
        messageList.add(welcomeMsg);
        chatAdapter.notifyDataSetChanged();
    }

    private void handleUserSend(String userText) {
        if (isSending) {
            Toast.makeText(getContext(), "Please wait for FlexiBot's reply...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        isSending = true;
        btnSendMessage.setEnabled(false);

        // Snapshot prior messages BEFORE adding the new message (for clean multi-turn context)
        List<ChatMessage> priorHistory = new ArrayList<>();
        for (ChatMessage m : messageList) {
            if (!"welcome".equals(m.getId())) {
                priorHistory.add(m);
            }
        }

        // 1. Add user message to UI & save to Firestore
        String userMsgId = db.collection("users").document(uid).collection("chatHistory").document().getId();
        ChatMessage userMsg = new ChatMessage(userMsgId, ChatMessage.SENDER_USER, userText, System.currentTimeMillis());

        messageList.add(userMsg);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();

        db.collection("users").document(uid).collection("chatHistory").document(userMsgId).set(userMsg);

        // 2. Show typing indicator and call secure backend
        layoutTypingIndicator.setVisibility(View.VISIBLE);

        chatService.sendMessage(userText, currentUserProfile, priorHistory, new GeminiChatService.ChatCallback() {
            @Override
            public void onSuccess(String reply) {
                if (!isAdded()) return;
                isSending = false;
                btnSendMessage.setEnabled(true);
                layoutTypingIndicator.setVisibility(View.GONE);

                String botMsgId = db.collection("users").document(uid).collection("chatHistory").document().getId();
                ChatMessage botMsg = new ChatMessage(botMsgId, ChatMessage.SENDER_BOT, reply, System.currentTimeMillis());

                messageList.add(botMsg);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                scrollToBottom();

                db.collection("users").document(uid).collection("chatHistory").document(botMsgId).set(botMsg);
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                isSending = false;
                btnSendMessage.setEnabled(true);
                layoutTypingIndicator.setVisibility(View.GONE);

                ChatMessage errMsg = new ChatMessage(
                        "err_" + System.currentTimeMillis(),
                        ChatMessage.SENDER_BOT,
                        "Connection Notice: " + errorMessage,
                        System.currentTimeMillis()
                );
                messageList.add(errMsg);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                scrollToBottom();
            }
        });
    }

    private void confirmClearConversation() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Clear Chat History?")
                .setMessage("This will permanently delete your saved conversation with FlexiBot.")
                .setPositiveButton("Clear", (dialog, which) -> clearConversationHistory())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearConversationHistory() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("chatHistory").get()
                .addOnSuccessListener(snapshots -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : snapshots) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(aVoid -> {
                        messageList.clear();
                        addWelcomeGreeting();
                        Toast.makeText(getContext(), "Conversation cleared", Toast.LENGTH_SHORT).show();
                    });
                });
    }

    private void scrollToBottom() {
        if (!messageList.isEmpty()) {
            rvChatMessages.scrollToPosition(messageList.size() - 1);
        }
    }
}