package com.socialmedia.models;

import java.sql.Timestamp;

public class FriendRequest {
    private int requestId;
    private int senderId;
    private int receiverId;
    private String status; // "PENDING", "ACCEPTED", "DECLINED"
    private Timestamp timestamp;
    private String senderUsername; // For display purposes
    
    public FriendRequest(int requestId, int senderId, int receiverId, String status, Timestamp timestamp) {
        this.requestId = requestId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.timestamp = timestamp;
    }
    
    // Getters
    public int getRequestId() { return requestId; }
    public int getSenderId() { return senderId; }
    public int getReceiverId() { return receiverId; }
    public String getStatus() { return status; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getSenderUsername() { return senderUsername; }
    
    // Setter for sender username
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
} 