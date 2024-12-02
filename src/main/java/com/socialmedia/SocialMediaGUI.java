package com.socialmedia;

import com.socialmedia.controllers.*;
import com.socialmedia.models.*;
import com.socialmedia.ui.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

public class SocialMediaGUI extends Application {
    private Stage primaryStage;
    private Scene loginScene;
    private Scene mainScene;
    
    // Controllers
    private final DatabaseController dbController = DatabaseController.getInstance();
    private final UserController userController = UserController.getInstance();
    private final PostController postController = PostController.getInstance();
    private final CommentController commentController = CommentController.getInstance();
    private final FriendController friendController = FriendController.getInstance();
    private final MessageController messageController = MessageController.getInstance();

    private VBox postsContainer;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Social Media Application");

        createLoginScene();
        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void createLoginScene() {
        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(20));

        // Database connection fields
        TextField dbUserField = new TextField();
        dbUserField.setPromptText("Database Username");
        PasswordField dbPasswordField = new PasswordField();
        dbPasswordField.setPromptText("Database Password");
        Button connectButton = new Button("Connect to Database");

        // Login/Register fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");

        // Initially disable login controls
        usernameField.setDisable(true);
        passwordField.setDisable(true);
        loginButton.setDisable(true);
        registerButton.setDisable(true);

        // Connect button action
        connectButton.setOnAction(e -> {
            if (dbController.connect(dbUserField.getText(), dbPasswordField.getText())) {
                usernameField.setDisable(false);
                passwordField.setDisable(false);
                loginButton.setDisable(false);
                registerButton.setDisable(false);
                DialogFactory.showAlert(Alert.AlertType.INFORMATION, "Success", "Connected to database successfully!");
            }
        });

        // Login button action
        loginButton.setOnAction(e -> {
            if (userController.login(usernameField.getText(), passwordField.getText())) {
                createMainScene();
                primaryStage.setScene(mainScene);
            } else {
                DialogFactory.showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password");
            }
        });

        // Register button action
        registerButton.setOnAction(e -> showRegistrationDialog());

        loginLayout.getChildren().addAll(
            new Label("Database Connection"),
            dbUserField,
            dbPasswordField,
            connectButton,
            new Separator(),
            new Label("User Login"),
            usernameField,
            passwordField,
            loginButton,
            registerButton
        );

        loginScene = new Scene(loginLayout, 300, 400);
    }

    private void createMainScene() {
        TabPane tabPane = new TabPane();
        
        // Create tabs
        Tab profileTab = new Tab("Profile", createProfileTab());
        Tab postsTab = new Tab("Posts", createPostsTab());
        Tab friendsTab = new Tab("Friends", createFriendsTab());
        Tab messagesTab = new Tab("Messages", createMessagesTab());
        Tab notificationsTab = new Tab("Notifications", createNotificationsTab());

        // Prevent tabs from being closed
        tabPane.getTabs().addAll(profileTab, postsTab, friendsTab, messagesTab, notificationsTab);
        tabPane.getTabs().forEach(tab -> tab.setClosable(false));

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> logout());

        VBox mainLayout = new VBox(10);
        mainLayout.getChildren().addAll(tabPane, logoutButton);

        mainScene = new Scene(mainLayout, 800, 600);
    }

    private VBox createProfileTab() {
        User currentUser = userController.getCurrentUser();
        VBox profileLayout = new VBox(10);
        profileLayout.setPadding(new Insets(10));

        Label nameLabel = new Label("Name: " + currentUser.getName());
        Label emailLabel = new Label("Email: " + currentUser.getEmail());
        Label bioLabel = new Label("Bio: " + currentUser.getBio());
        
        Button updateProfileButton = new Button("Update Profile");
        updateProfileButton.setOnAction(e -> showUpdateProfileDialog());

        profileLayout.getChildren().addAll(
            new Label("Profile Information"),
            nameLabel,
            emailLabel,
            bioLabel,
            updateProfileButton
        );

        return profileLayout;
    }

    private VBox createPostsTab() {
        VBox postsLayout = new VBox(10);
        postsLayout.setPadding(new Insets(10));

        // Create post section
        TextArea newPostContent = new TextArea();
        newPostContent.setPromptText("What's on your mind?");
        newPostContent.setPrefRowCount(3);
        newPostContent.setPrefWidth(600); // Set preferred width
        newPostContent.setWrapText(true); // Enable text wrapping
        
        Button createPostButton = new Button("Create Post");
        createPostButton.setOnAction(e -> {
            String content = newPostContent.getText().trim();
            if (!content.isEmpty()) {
                if (postController.createPost(userController.getCurrentUser().getUserId(), content)) {
                    newPostContent.clear();
                    refreshPosts(postsContainer); // Use the class field instead
                }
            } else {
                DialogFactory.showAlert(Alert.AlertType.WARNING, 
                    "Warning", "Post content cannot be empty!");
            }
        });

        // Create a container for posts that will persist
        postsContainer = new VBox(10); // Make this a class field
        ScrollPane scrollPane = new ScrollPane(postsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400); // Set a preferred height
        
        // Refresh button
        Button refreshButton = new Button("Refresh Posts");
        refreshButton.setOnAction(e -> refreshPosts(postsContainer));

        // Add all components to the layout
        postsLayout.getChildren().addAll(
            new Label("Create New Post"),
            newPostContent,
            createPostButton,
            new Separator(),
            new Label("Recent Posts"),
            scrollPane,
            refreshButton
        );

        // Initial load of posts
        refreshPosts(postsContainer);
        return postsLayout;
    }

    private void refreshPosts(VBox postsListView) {
        postsListView.getChildren().clear();
        List<Post> posts = postController.loadPosts(userController.getCurrentUser().getUserId());
        
        if (posts.isEmpty()) {
            Label noPostsLabel = new Label("No posts to show");
            noPostsLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            postsListView.getChildren().add(noPostsLabel);
        } else {
            for (Post post : posts) {
                PostView postView = new PostView(post);
                postView.setMaxWidth(Double.MAX_VALUE); // Make post view use full width
                postsListView.getChildren().add(postView);
            }
        }
    }

    private VBox createFriendsTab() {
        VBox friendsLayout = new VBox(10);
        friendsLayout.setPadding(new Insets(10));

        // Friends list
        ListView<String> friendsListView = new ListView<>();

        // Add friend section
        TextField friendUsernameField = new TextField();
        friendUsernameField.setPromptText("Enter username to add");
        Button addFriendButton = new Button("Add Friend");

        addFriendButton.setOnAction(e -> {
            String friendUsername = friendUsernameField.getText().trim();
            if (!friendUsername.isEmpty()) {
                User friend = userController.getUserByUsername(friendUsername);
                if (friend != null) {
                    if (friendController.sendFriendRequest(
                            userController.getCurrentUser().getUserId(), 
                            friend.getUserId())) {
                        DialogFactory.showAlert(Alert.AlertType.INFORMATION, 
                            "Success", "Friend request sent successfully!");
                        friendUsernameField.clear();
                    } else {
                        DialogFactory.showAlert(Alert.AlertType.ERROR, 
                            "Error", "Friend request already sent or other error occurred!");
                    }
                } else {
                    DialogFactory.showAlert(Alert.AlertType.ERROR, 
                        "Error", "User not found!");
                }
            }
        });

        // Remove friend section
        Button removeFriendButton = new Button("Remove Selected Friend");
        removeFriendButton.setDisable(true);

        friendsListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> removeFriendButton.setDisable(newValue == null)
        );

        removeFriendButton.setOnAction(e -> {
            String selectedFriend = friendsListView.getSelectionModel().getSelectedItem();
            if (selectedFriend != null) {
                String username = selectedFriend.split(" \\(")[0];
                User friend = userController.getUserByUsername(username);
                
                if (DialogFactory.showConfirmation("Remove Friend", 
                        "Remove Friend", 
                        "Are you sure you want to remove " + username + " from your friends list?")) {
                    if (friendController.removeFriend(
                            userController.getCurrentUser().getUserId(), 
                            friend.getUserId())) {
                        refreshFriendsList(friendsListView);
                    }
                }
            }
        });

        // Refresh button
        Button refreshButton = new Button("Refresh Friends List");
        refreshButton.setOnAction(e -> refreshFriendsList(friendsListView));

        // Status label
        Label statusLabel = new Label();

        friendsLayout.getChildren().addAll(
            new Label("Add Friend"),
            friendUsernameField,
            addFriendButton,
            new Separator(),
            new Label("My Friends"),
            friendsListView,
            removeFriendButton,
            refreshButton,
            statusLabel
        );

        refreshFriendsList(friendsListView);
        return friendsLayout;
    }

    private void refreshFriendsList(ListView<String> friendsListView) {
        friendsListView.getItems().clear();
        List<User> friends = friendController.loadFriends(userController.getCurrentUser().getUserId());
        
        for (User friend : friends) {
            String friendDisplay = String.format("%s (%s)", friend.getUsername(), friend.getName());
            friendsListView.getItems().add(friendDisplay);
        }
    }

    private VBox createMessagesTab() {
        VBox messagesLayout = new VBox(10);
        messagesLayout.setPadding(new Insets(10));

        // Messages list with better formatting
        ListView<VBox> messagesListView = new ListView<>();
        messagesListView.setPrefHeight(300);
        
        // Send message section
        TextField recipientField = new TextField();
        recipientField.setPromptText("Recipient username");
        
        TextArea messageContent = new TextArea();
        messageContent.setPromptText("Type your message");
        messageContent.setPrefRowCount(3);
        messageContent.setWrapText(true);
        
        Button sendButton = new Button("Send Message");
        sendButton.setOnAction(e -> {
            String recipient = recipientField.getText().trim();
            String content = messageContent.getText().trim();
            
            if (!recipient.isEmpty() && !content.isEmpty()) {
                User recipientUser = userController.getUserByUsername(recipient);
                if (recipientUser != null) {
                    if (messageController.sendMessage(
                            userController.getCurrentUser().getUserId(),
                            recipientUser.getUserId(),
                            content)) {
                        DialogFactory.showAlert(Alert.AlertType.INFORMATION,
                            "Success", "Message sent successfully!");
                        messageContent.clear();
                        recipientField.clear();
                        refreshMessages(messagesListView);
                    } else {
                        DialogFactory.showAlert(Alert.AlertType.ERROR,
                            "Error", "Failed to send message!");
                    }
                } else {
                    DialogFactory.showAlert(Alert.AlertType.ERROR,
                        "Error", "Recipient not found!");
                }
            }
        });

        Button refreshButton = new Button("Refresh Messages");
        refreshButton.setOnAction(e -> refreshMessages(messagesListView));

        messagesLayout.getChildren().addAll(
            new Label("Send New Message"),
            recipientField,
            messageContent,
            sendButton,
            new Separator(),
            new Label("Messages"),
            messagesListView,
            refreshButton
        );

        refreshMessages(messagesListView);
        return messagesLayout;
    }

    private void refreshMessages(ListView<VBox> messagesListView) {
        messagesListView.getItems().clear();
        List<Message> messages = messageController.loadMessages(userController.getCurrentUser().getUserId());
        
        if (messages.isEmpty()) {
            VBox noMessagesBox = new VBox();
            Label noMessagesLabel = new Label("No messages to show");
            noMessagesLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            noMessagesBox.getChildren().add(noMessagesLabel);
            messagesListView.getItems().add(noMessagesBox);
        } else {
            for (Message message : messages) {
                VBox messageBox = new VBox(5);
                messageBox.setStyle("-fx-padding: 5; -fx-background-color: #f8f8f8;");
                
                Label senderLabel = new Label("From: " + message.getSenderUsername());
                senderLabel.setStyle("-fx-font-weight: bold");
                
                Label timeLabel = new Label(message.getTimestamp().toString());
                timeLabel.setStyle("-fx-font-size: 0.8em; -fx-text-fill: #666666;");
                
                Label contentLabel = new Label(message.getContent());
                contentLabel.setWrapText(true);
                
                messageBox.getChildren().addAll(senderLabel, timeLabel, contentLabel);
                messagesListView.getItems().add(messageBox);
            }
        }
    }

    private VBox createNotificationsTab() {
        VBox notificationsLayout = new VBox(10);
        notificationsLayout.setPadding(new Insets(10));

        // Friend requests list
        ListView<VBox> requestsListView = new ListView<>();
        requestsListView.setPrefHeight(300);

        Button refreshButton = new Button("Refresh Notifications");
        refreshButton.setOnAction(e -> refreshNotifications(requestsListView));

        notificationsLayout.getChildren().addAll(
            new Label("Friend Requests"),
            requestsListView,
            refreshButton
        );

        refreshNotifications(requestsListView);
        return notificationsLayout;
    }

    private void refreshNotifications(ListView<VBox> requestsListView) {
        requestsListView.getItems().clear();
        List<FriendRequest> requests = friendController.getPendingRequests(
            userController.getCurrentUser().getUserId());
        
        if (requests.isEmpty()) {
            VBox noRequestsBox = new VBox();
            Label noRequestsLabel = new Label("No pending friend requests");
            noRequestsLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            noRequestsBox.getChildren().add(noRequestsLabel);
            requestsListView.getItems().add(noRequestsBox);
        } else {
            for (FriendRequest request : requests) {
                VBox requestBox = createFriendRequestBox(request, requestsListView);
                requestsListView.getItems().add(requestBox);
            }
        }
    }

    private VBox createFriendRequestBox(FriendRequest request, ListView<VBox> requestsListView) {
        VBox requestBox = new VBox(5);
        requestBox.setStyle("-fx-padding: 10; -fx-background-color: #f8f8f8;");

        Label senderLabel = new Label("Friend request from: " + request.getSenderUsername());
        senderLabel.setStyle("-fx-font-weight: bold");

        Label timeLabel = new Label(request.getTimestamp().toString());
        timeLabel.setStyle("-fx-font-size: 0.8em; -fx-text-fill: #666666;");

        HBox buttonBox = new HBox(10);
        Button acceptButton = new Button("Accept");
        Button declineButton = new Button("Decline");

        acceptButton.setOnAction(e -> {
            if (friendController.respondToFriendRequest(request.getRequestId(), true)) {
                DialogFactory.showAlert(Alert.AlertType.INFORMATION,
                    "Success", "Friend request accepted!");
                refreshNotifications(requestsListView);
            }
        });

        declineButton.setOnAction(e -> {
            if (friendController.respondToFriendRequest(request.getRequestId(), false)) {
                DialogFactory.showAlert(Alert.AlertType.INFORMATION,
                    "Success", "Friend request declined!");
                refreshNotifications(requestsListView);
            }
        });

        buttonBox.getChildren().addAll(acceptButton, declineButton);
        requestBox.getChildren().addAll(senderLabel, timeLabel, buttonBox);
        return requestBox;
    }

    private void logout() {
        userController.logout();
        primaryStage.setScene(loginScene);
    }

    private void showRegistrationDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Register New User");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        TextField email = new TextField();
        PasswordField password = new PasswordField();
        TextField name = new TextField();
        TextArea bio = new TextArea();
        TextField profilePicture = new TextField();

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(email, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(password, 1, 2);
        grid.add(new Label("Name:"), 0, 3);
        grid.add(name, 1, 3);
        grid.add(new Label("Bio:"), 0, 4);
        grid.add(bio, 1, 4);
        grid.add(new Label("Profile Picture URL:"), 0, 5);
        grid.add(profilePicture, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (userController.registerUser(
                        username.getText(), 
                        email.getText(), 
                        password.getText(),
                        name.getText(), 
                        bio.getText(), 
                        profilePicture.getText())) {
                    DialogFactory.showAlert(Alert.AlertType.INFORMATION, 
                        "Success", "Registration successful!");
                }
            }
        });
    }

    private void showUpdateProfileDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Profile");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        User currentUser = userController.getCurrentUser();

        TextField email = new TextField(currentUser.getEmail());
        PasswordField password = new PasswordField();
        password.setPromptText("Leave empty to keep current password");
        TextField name = new TextField(currentUser.getName());
        TextArea bio = new TextArea(currentUser.getBio());
        TextField profilePicture = new TextField(currentUser.getProfilePicture());

        grid.add(new Label("Email:"), 0, 0);
        grid.add(email, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label("Name:"), 0, 2);
        grid.add(name, 1, 2);
        grid.add(new Label("Bio:"), 0, 3);
        grid.add(bio, 1, 3);
        grid.add(new Label("Profile Picture URL:"), 0, 4);
        grid.add(profilePicture, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (userController.updateProfile(
                        currentUser.getUserId(),
                        email.getText(),
                        password.getText(),
                        name.getText(),
                        bio.getText(),
                        profilePicture.getText())) {
                    DialogFactory.showAlert(Alert.AlertType.INFORMATION,
                        "Success", "Profile updated successfully!");
                    // Refresh the profile tab
                    TabPane tabPane = (TabPane) mainScene.getRoot().getChildrenUnmodifiable().get(0);
                    Tab profileTab = tabPane.getTabs().get(0);
                    profileTab.setContent(createProfileTab());
                } else {
                    DialogFactory.showAlert(Alert.AlertType.ERROR,
                        "Error", "Failed to update profile!");
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
