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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiChatService {

    // Uses localhost (127.0.0.1) bridged directly over your USB cable via `adb reverse tcp:3000 tcp:3000`.
    // (If you disconnect USB and test over Wi-Fi later, replace 127.0.0.1 with your PC's Wi-Fi IPv4 address.)
    private static final String BACKEND_URL = "http://127.0.0.1:3000/api/chat";

    public interface ChatCallback {
        void onSuccess(String reply);
        void onError(String errorMessage);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void sendMessage(String userMessage, User userProfile, List<ChatMessage> recentHistory, ChatCallback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BACKEND_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(60000);
                connection.setDoOutput(true);

                // Build JSON payload
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

                // Include up to the last 12 messages for conversational memory
                JSONArray historyArray = new JSONArray();
                if (recentHistory != null && !recentHistory.isEmpty()) {
                    int startIdx = Math.max(0, recentHistory.size() - 12);
                    for (int i = startIdx; i < recentHistory.size(); i++) {
                        ChatMessage msg = recentHistory.get(i);
                        JSONObject turn = new JSONObject();
                        turn.put("role", ChatMessage.SENDER_USER.equals(msg.getSender()) ? "user" : "model");
                        turn.put("text", msg.getMessage());
                        historyArray.put(turn);
                    }
                }
                payload.put("history", historyArray);

                // Send request
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                InputStream stream = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                StringBuilder responseBuilder = new StringBuilder();
                if (stream != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                    }
                }

                JSONObject responseJson = new JSONObject(responseBuilder.toString());

                if (responseCode >= 200 && responseCode < 300 && responseJson.has("reply")) {
                    String reply = responseJson.getString("reply");
                    mainHandler.post(() -> callback.onSuccess(reply));
                } else {
                    String err = responseJson.optString("error", "Server returned error code: " + responseCode);
                    mainHandler.post(() -> callback.onError(err));
                }

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(
                        "Cannot reach FlexiBot AI server. Make sure your Node.js backend is running (" + e.getMessage() + ")"
                ));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }
}