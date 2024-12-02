package com.socialmedia.ui;

import com.socialmedia.models.Comment;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.*;
import javafx.geometry.Insets;

public class CommentView extends VBox {
    private Comment comment;
    private Label likeCountLabel;
    private Runnable onDeleteAction;
    
    public CommentView(Comment comment, String currentUsername, String authorUsername, Runnable onDeleteAction) {
        this.comment = comment;
        this.onDeleteAction = onDeleteAction;
        setupUI(currentUsername, authorUsername);
    }

    private void setupUI(String currentUsername, String authorUsername) {
        setStyle("-fx-background-color: #f8f8f8; -fx-padding: 5;");
        
        // Header with username and delete button
        HBox header = new HBox(10);
        Label authorLabel = new Label(authorUsername);
        authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 0.9em;");
        
        if (currentUsername.equals(authorUsername)) {
            Button deleteButton = new Button("×");
            deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-cursor: hand;");
            deleteButton.setOnAction(e -> onDeleteAction.run());
            header.getChildren().addAll(authorLabel, deleteButton);
        } else {
            header.getChildren().add(authorLabel);
        }

        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);

        Label timeLabel = new Label(comment.getTimestamp().toString());
        timeLabel.setStyle("-fx-font-size: 0.8em; -fx-text-fill: #666666;");

        // Like section
        HBox likeBox = new HBox(10);
        likeCountLabel = new Label("Likes: " + comment.getLikeCount());
        Button likeButton = new Button("Like");
        likeBox.getChildren().addAll(likeButton, likeCountLabel);

        getChildren().addAll(header, contentLabel, timeLabel, likeBox);
    }

    public void updateLikeCount(int count) {
        likeCountLabel.setText("Likes: " + count);
    }
} 