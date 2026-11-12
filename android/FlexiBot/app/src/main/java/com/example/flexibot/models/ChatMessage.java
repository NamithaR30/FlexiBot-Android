package com.example.flexibot.models;

public class ChatMessage {
    public static final String SENDER_USER = "user";
    public static final String SENDER_BOT = "bot";

    private String id;
    private String sender;
    private String message;
    private long timestamp;

    // Empty constructor required for Firestore
    public ChatMessage() {}

    public ChatMessage(String id, String sender, String message, long timestamp) {
        this.id = id;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getSender() { return sender; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}