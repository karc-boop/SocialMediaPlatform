package com.socialmedia.ui;

import com.socialmedia.models.Post;
import com.socialmedia.models.Comment;
import com.socialmedia.controllers.CommentController;
import com.socialmedia.controllers.UserController;
import com.socialmedia.controllers.PostController;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import java.util.List;
import java.sql.SQLException;

public class PostView extends VBox {
    private Post post;
    private Label likeCountLabel;
    private final CommentController commentController = CommentController.getInstance();
    private final UserController userController = UserController.getInstance();
    private final PostController postController = PostController.getInstance();
    private VBox commentsContainer;
    
    public PostView(Post post) {
        this.post = post;
        setupUI();
    }

    private void setupUI() {
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-color: white;");
        setSpacing(8);
        
        // Create header box for username and timestamp
        HBox headerBox = new HBox(10); // 10 pixels spacing between elements
        
        String username = userController.getUserById(post.getUserId()).getUsername();
        Label authorLabel = new Label("From: " + username);
        authorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label timeLabel = new Label("• " + post.getTimestamp().toString());
        timeLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        
        headerBox.getChildren().addAll(authorLabel, timeLabel);
        
        // Enhanced post content with shadow
        Label contentLabel = new Label(post.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-padding: 15; " +
            "-fx-background-color: white; " +
            "-fx-background-radius: 5; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        contentLabel.setPrefWidth(550);
        
        // Add edit/delete buttons if user owns the post
        HBox buttonBox = new HBox(5);
        if (postController.isPostOwner(post.getPostId(), userController.getCurrentUser().getUserId())) {
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            
            editButton.setOnAction(e -> showEditDialog());
            deleteButton.setOnAction(e -> {
                if (DialogFactory.showConfirmation("Delete Post", 
                        "Delete Post", 
                        "Are you sure you want to delete this post?")) {
                    if (postController.deletePost(post.getPostId())) {
                        ((VBox) this.getParent()).getChildren().remove(this);
                    }
                }
            });
            
            buttonBox.getChildren().addAll(editButton, deleteButton);
        }
        
        // Like section
        likeCountLabel = new Label("Likes: " + post.getLikeCount());
        Button likeButton = new Button("Like");
        likeButton.setOnAction(e -> {
            if (postController.likePost(post.getPostId(), userController.getCurrentUser().getUserId())) {
                // Update like count immediately after successful like
                try {
                    postController.updatePostLikeCount(post);
                    likeCountLabel.setText("Likes: " + post.getLikeCount());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    DialogFactory.showAlert(Alert.AlertType.ERROR, 
                        "Error", "Failed to update like count");
                }
            } else {
                DialogFactory.showAlert(Alert.AlertType.WARNING, 
                    "Warning", "You have already liked this post");
            }
        });
        
        // Share section
        Button shareButton = new Button("Share");
        Label shareCountLabel = new Label("Shares: 0");
        try {
            shareCountLabel.setText("Shares: " + postController.getShareCount(post.getPostId()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        shareButton.setOnAction(e -> {
            if (postController.sharePost(post.getPostId(), userController.getCurrentUser().getUserId())) {
                try {
                    shareCountLabel.setText("Shares: " + postController.getShareCount(post.getPostId()));
                    DialogFactory.showAlert(Alert.AlertType.INFORMATION, 
                        "Success", "Post shared successfully!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    DialogFactory.showAlert(Alert.AlertType.ERROR, 
                        "Error", "Failed to update share count");
                }
            } else {
                DialogFactory.showAlert(Alert.AlertType.ERROR, 
                    "Error", "Failed to share post");
            }
        });
        
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
            headerBox,
            buttonBox,
            contentLabel,
            likeButton, 
            likeCountLabel,
            shareButton,
            shareCountLabel,
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

    private void showEditDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Post");
        dialog.setHeaderText("Edit your post content:");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the content field
        TextArea contentField = new TextArea(post.getContent());
        contentField.setWrapText(true);
        dialog.getDialogPane().setContent(contentField);

        // Convert the result to post content when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return contentField.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(content -> {
            if (postController.updatePost(post.getPostId(), content)) {
                // Update the content label
                for (javafx.scene.Node node : getChildren()) {
                    if (node instanceof Label && ((Label) node).getText().equals(post.getContent())) {
                        ((Label) node).setText(content);
                        post.setContent(content);
                        break;
                    }
                }
            }
        });
    }
} 