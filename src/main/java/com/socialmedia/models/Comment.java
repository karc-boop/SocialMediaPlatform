package com.socialmedia.models;

import java.sql.Timestamp;

public class Comment {
    private int commentId;
    private int postId;
    private int userId;
    private String content;
    private Timestamp timestamp;
    private int likeCount;

    // Constructor
    public Comment(int commentId, int postId, int userId, String content, Timestamp timestamp) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public int getCommentId() { return commentId; }
    public int getPostId() { return postId; }
    public int getUserId() { return userId; }
    public String getContent() { return content; }
    public Timestamp getTimestamp() { return timestamp; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
} 