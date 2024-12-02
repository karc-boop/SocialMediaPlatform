package com.socialmedia.ui;

import com.socialmedia.models.Post;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.geometry.Insets;

public class PostView extends VBox {
    private Post post;
    private Label likeCountLabel;
    
    public PostView(Post post) {
        this.post = post;
        setupUI();
    }

    private void setupUI() {
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        // Create UI components
        Label authorLabel = new Label("From: " + post.getUserId());
        Label timeLabel = new Label("Time: " + post.getTimestamp().toString());
        Label contentLabel = new Label(post.getContent());
        contentLabel.setWrapText(true);
        
        // Like section
        likeCountLabel = new Label("Likes: " + post.getLikeCount());
        Button likeButton = new Button("Like");
        
        // Add components to view
        getChildren().addAll(authorLabel, timeLabel, contentLabel, likeButton, likeCountLabel);
    }

    public void updateLikeCount(int count) {
        likeCountLabel.setText("Likes: " + count);
    }
} 