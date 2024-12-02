package com.socialmedia.models;

import java.sql.Timestamp;

public class Post {
    private int postId;
    private int userId;
    private String content;
    private Timestamp timestamp;
    private int likeCount;

    // Constructor
    public Post(int postId, int userId, String content, Timestamp timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public int getPostId() { return postId; }
    public int getUserId() { return userId; }
    public String getContent() { return content; }
    public Timestamp getTimestamp() { return timestamp; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public void setContent(String content) {
        this.content = content;
    }
} 