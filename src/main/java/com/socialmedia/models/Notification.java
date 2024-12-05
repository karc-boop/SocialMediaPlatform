package com.socialmedia.models;

import java.sql.Timestamp;

public class Notification {
    private int notificationId;
    private int userId;
    private String message;
    private String type; // e.g., "FRIEND_REQUEST", "POST_LIKE", "COMMENT"
    private boolean isRead;
    private Timestamp timestamp;

    public Notification(int notificationId, int userId, String message, String type, boolean isRead, Timestamp timestamp) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    // Getters
    public int getNotificationId() { 
        return notificationId; 
    }
    public int getUserId() { 
        return userId; 
    }
    public String getMessage() { 
        return message; 
    }
    public String getType() { 
        return type; 
    }
    public boolean isRead() { 
        return isRead; 
    }
    public Timestamp getTimestamp() { 
        return timestamp; 
    }

    // Setters
    public void setRead(boolean read) { isRead = read; }
} 