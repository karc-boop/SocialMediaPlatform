package com.socialmedia.models;

import java.sql.Timestamp;

public class Message {
    private int messageId;
    private int senderId;
    private int receiverId;
    private String content;
    private Timestamp timestamp;
    private String senderUsername; // For display purposes

    public Message(int messageId, int senderId, int receiverId, String content, Timestamp timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters
    public int getMessageId() { return messageId; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getSenderUsername() { return senderUsername; }

    // Setter for sender username
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
} 