package com.socialmedia.ui;

import com.socialmedia.models.Post;
import com.socialmedia.models.Comment;
import com.socialmedia.controllers.CommentController;
import com.socialmedia.controllers.UserController;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import java.util.List;

public class PostView extends VBox {
    private Post post;
    private Label likeCountLabel;
    private final CommentController commentController = CommentController.getInstance();
    private final UserController userController = UserController.getInstance();
    private VBox commentsContainer;
    
    public PostView(Post post) {
        this.post = post;
        setupUI();
    }

    private void setupUI() {
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5;");
        setSpacing(5);
        
        // Create UI components
        Label authorLabel = new Label("From: " + post.getUserId());
        Label timeLabel = new Label("Time: " + post.getTimestamp().toString());
        Label contentLabel = new Label(post.getContent());
        contentLabel.setWrapText(true);
        
        // Like section
        likeCountLabel = new Label("Likes: " + post.getLikeCount());
        Button likeButton = new Button("Like");
        
        // Comment section
        commentsContainer = new VBox(5);
        commentsContainer.setStyle("-fx-padding: 0 0 0 20"); // Add left padding for comments
        
        // Comment input
        HBox commentInputBox = new HBox(5);
        TextField commentField = new TextField();
        commentField.setPromptText("Write a comment...");
        commentField.setPrefWidth(300);
        
        Button commentButton = new Button("Comment");
        commentButton.setOnAction(e -> {
            String content = commentField.getText().trim();
            if (!content.isEmpty()) {
                if (commentController.createComment(post.getPostId(), post.getUserId(), content)) {
                    commentField.clear();
                    refreshComments();
                }
            }
        });
        
        commentInputBox.getChildren().addAll(commentField, commentButton);
        
        // Add all components to view
        getChildren().addAll(
            authorLabel, 
            timeLabel, 
            contentLabel, 
            likeButton, 
            likeCountLabel,
            new Separator(), // Add a line between post and comments
            commentInputBox,
            commentsContainer
        );
        
        // Load existing comments
        refreshComments();
    }

    private void refreshComments() {
        commentsContainer.getChildren().clear();
        List<Comment> comments = commentController.loadComments(post.getPostId());
        
        if (comments.isEmpty()) {
            Label noCommentsLabel = new Label("No comments yet");
            noCommentsLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            commentsContainer.getChildren().add(noCommentsLabel);
        } else {
            for (Comment comment : comments) {
                HBox commentBox = new HBox(5);
                commentBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 5; -fx-background-radius: 3;");
                
                // Create username label
                String username = userController.getUserById(comment.getUserId()).getUsername();
                Label usernameLabel = new Label(username + ": ");
                usernameLabel.setStyle("-fx-font-weight: bold;");
                
                Label commentText = new Label(comment.getContent());
                commentText.setWrapText(true);
                
                commentBox.getChildren().addAll(usernameLabel, commentText);
                
                // Add context menu for right-click
                ContextMenu contextMenu = new ContextMenu();
                
                MenuItem replyItem = new MenuItem("Reply");
                replyItem.setOnAction(e -> showReplyField(comment, commentBox));
                
                MenuItem deleteItem = new MenuItem("Delete");
                // Only show delete option for comment owner
                if (comment.getUserId() == userController.getCurrentUser().getUserId()) {
                    deleteItem.setOnAction(e -> {
                        if (commentController.deleteComment(comment.getCommentId())) {
                            refreshComments();
                        }
                    });
                } else {
                    deleteItem.setDisable(true);
                }
                
                contextMenu.getItems().addAll(replyItem, deleteItem);
                commentBox.setOnContextMenuRequested(e -> 
                    contextMenu.show(commentBox, e.getScreenX(), e.getScreenY())
                );
                
                commentsContainer.getChildren().add(commentBox);
            }
        }
    }
    
    private void showReplyField(Comment parentComment, HBox parentCommentBox) {
        // Create reply input field
        HBox replyBox = new HBox(5);
        replyBox.setStyle("-fx-padding: 5 0 5 20;"); // Add left padding for indentation
        
        TextField replyField = new TextField();
        replyField.setPromptText("Write a reply...");
        replyField.setPrefWidth(250);
        
        Button replyButton = new Button("Reply");
        replyButton.setOnAction(e -> {
            String content = replyField.getText().trim();
            if (!content.isEmpty()) {
                if (commentController.createComment(
                        post.getPostId(),
                        userController.getCurrentUser().getUserId(),
                        content)) {
                    refreshComments();
                }
            }
        });
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            int index = commentsContainer.getChildren().indexOf(parentCommentBox);
            if (index >= 0 && index + 1 < commentsContainer.getChildren().size()) {
                commentsContainer.getChildren().remove(index + 1);
            }
        });
        
        replyBox.getChildren().addAll(replyField, replyButton, cancelButton);
        
        // Insert reply box after the parent comment
        int index = commentsContainer.getChildren().indexOf(parentCommentBox);
        if (index >= 0) {
            commentsContainer.getChildren().add(index + 1, replyBox);
            replyField.requestFocus();
        }
    }

    public void updateLikeCount(int count) {
        likeCountLabel.setText("Likes: " + count);
    }
} 