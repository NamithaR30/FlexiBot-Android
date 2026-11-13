package com.example.flexibot.services;

import android.os.Handler;
import android.os.Looper;

import com.example.flexibot.models.ChatMessage;
import com.example.flexibot.models.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiChatService {

    private static final String BACKEND_CHAT_URL = "http://localhost:3000/api/chat";
    private static final int MAX_HISTORY_MESSAGES = 8;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ChatCallback {
        void onSuccess(String reply);
        void onError(String errorMessage);
    }

    public void sendMessage(String userMessage, User userProfile, ChatCallback callback) {
        sendMessage(userMessage, userProfile, null, callback);
    }

    public void sendMessage(
            String userMessage,
            User userProfile,
            List<ChatMessage> history,
            ChatCallback callback
    ) {
        final List<ChatMessage> historySnapshot = history != null
                ? new ArrayList<>(history)
                : new ArrayList<>();

        executorService.execute(() -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("message", userMessage);

                if (userProfile != null) {
                    JSONObject profileJson = new JSONObject();
                    profileJson.put("name", userProfile.getName());
                    profileJson.put("age", userProfile.getAge());
                    profileJson.put("gender", userProfile.getGender());
                    profileJson.put("height", userProfile.getHeight());
                    profileJson.put("weight", userProfile.getWeight());
                    profileJson.put("fitnessGoal", userProfile.getFitnessGoal());
                    profileJson.put("fitnessLevel", userProfile.getFitnessLevel());
                    profileJson.put("equipment", userProfile.getEquipment());
                    payload.put("userProfile", profileJson);
                }

                JSONArray historyArray = buildHistoryJsonArray(historySnapshot, userMessage);
                payload.put("history", historyArray);

                URL url = new URL(BACKEND_CHAT_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
                }

                int statusCode = conn.getResponseCode();
                InputStream responseStream = (statusCode >= 200 && statusCode < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                StringBuilder responseBuilder = new StringBuilder();
                if (responseStream != null) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                    }
                }

                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());

                if (statusCode >= 200 && statusCode < 300) {
                    String reply = jsonResponse.optString(
                            "reply",
                            "FLEXY couldn't generate a response right now. Please try again."
                    );
                    postSuccess(callback, reply);
                } else {
                    String error = jsonResponse.optString(
                            "error",
                            "Server returned error code " + statusCode
                    );
                    postError(callback, error);
                }
            } catch (Exception e) {
                postError(
                        callback,
                        "Unable to reach FLEXY backend. Ensure Node.js is running and adb reverse tcp:3000 tcp:3000 is active."
                );
            }
        });
    }

    private JSONArray buildHistoryJsonArray(List<ChatMessage> history, String latestUserMessage) {
        JSONArray historyArray = new JSONArray();
        if (history == null || history.isEmpty()) {
            return historyArray;
        }

        int endIndex = history.size();
        ChatMessage lastItem = history.get(endIndex - 1);
        if (lastItem != null
                && lastItem.isUser()
                && latestUserMessage != null
                && latestUserMessage.equals(lastItem.getText())) {
            endIndex--;
        }

        int startIndex = Math.max(0, endIndex - MAX_HISTORY_MESSAGES);
        for (int i = startIndex; i < endIndex; i++) {
            ChatMessage msg = history.get(i);
            if (msg == null || msg.getText() == null || msg.getText().trim().isEmpty()) {
                continue;
            }
            try {
                JSONObject itemJson = new JSONObject();
                itemJson.put("role", msg.isUser() ? "user" : "model");
                itemJson.put("text", msg.getText());
                historyArray.put(itemJson);
            } catch (Exception ignored) {
            }
        }

        return historyArray;
    }

    private void postSuccess(ChatCallback callback, String reply) {
        if (callback == null) return;
        mainHandler.post(() -> callback.onSuccess(reply));
    }

    private void postError(ChatCallback callback, String error) {
        if (callback == null) return;
        mainHandler.post(() -> callback.onError(error));
    }

    public void shutdown() {
        executorService.shutdownNow();
    }
}