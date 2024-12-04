package com.socialmedia;

import com.socialmedia.controllers.*;
import com.socialmedia.models.*;
import com.socialmedia.ui.*;
import com.socialmedia.utils.ErrorHandler;
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
    private final FriendController friendController = FriendController.getInstance();

    private User currentUser;  // Add at class level

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
                currentUser = userController.getCurrentUser();  // Set current user
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
        
        // For Messages tab
        Tab messagesTab = new Tab("Messages");
        messagesTab.setContent(createMessagesContent());
        
        Tab notificationsTab = new Tab("Notifications", createNotificationsTab());
        
        // For Account Management tab
        Tab accountManagementTab = new Tab("Account Management");
        accountManagementTab.setContent(createAccountManagementContent());

        // Rest of the code remains the same
        tabPane.getTabs().addAll(profileTab, postsTab, friendsTab, messagesTab, notificationsTab, accountManagementTab);
        tabPane.getTabs().forEach(tab -> tab.setClosable(false));

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

        Button settingsButton = new Button("Settings");
        settingsButton.setOnAction(e -> new ProfileSettingsDialog(userController.getCurrentUser().getUserId()).showAndWait());

        profileLayout.getChildren().addAll(
            new Label("Profile Information"),
            nameLabel,
            emailLabel,
            bioLabel,
            updateProfileButton,
            settingsButton
        );

        return profileLayout;
    }

    private VBox createPostsTab() {
        VBox postsLayout = new VBox(10);
        postsLayout.setPadding(new Insets(10));

        // Posts list with scroll functionality
        VBox postsListView = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(postsListView);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent;");

        // Create post section
        TextArea newPostContent = new TextArea();
        newPostContent.setPromptText("What's on your mind?");
        newPostContent.setPrefRowCount(3);
        newPostContent.setPrefWidth(600);
        
        Button createPostButton = new Button("Create Post");
        createPostButton.setOnAction(e -> {
            String content = newPostContent.getText().trim();
            if (!content.isEmpty()) {
                if (postController.createPost(userController.getCurrentUser().getUserId(), content)) {
                    newPostContent.clear();  // Clear the input
                    refreshPosts(postsListView);  // Refresh the posts list
                    DialogFactory.showAlert(Alert.AlertType.INFORMATION, 
                        "Success", "Post created successfully!");
                } else {
                    DialogFactory.showAlert(Alert.AlertType.ERROR, 
                        "Error", "Failed to create post!");
                }
            } else {
                DialogFactory.showAlert(Alert.AlertType.WARNING, 
                    "Warning", "Post content cannot be empty!");
            }
        });

        Button refreshButton = new Button("Refresh Posts");
        refreshButton.setOnAction(e -> refreshPosts(postsListView));

        // Add hashtag search
        HBox searchBox = new HBox(10);
        TextField hashtagField = new TextField();
        hashtagField.setPromptText("Search by hashtag (without #)");
        Button searchButton = new Button("Search");
        Button clearSearchButton = new Button("Clear Search");
        
        searchButton.setOnAction(e -> {
            String tag = hashtagField.getText().trim();
            if (!tag.isEmpty()) {
                postsListView.getChildren().clear();
                List<Post> posts = postController.searchPostsByHashtag(tag);
                for (Post post : posts) {
                    PostView postView = new PostView(post);
                    postsListView.getChildren().add(postView);
                }
            }
        });
        
        clearSearchButton.setOnAction(e -> {
            hashtagField.clear();
            refreshPosts(postsListView);
        });
        
        searchBox.getChildren().addAll(hashtagField, searchButton, clearSearchButton);

        postsLayout.getChildren().addAll(
            new Label("Create New Post"),
            newPostContent,
            createPostButton,
            new Separator(),
            new Label("Search Posts"),
            searchBox,
            new Separator(),
            new Label("Recent Posts"),
            scrollPane,
            refreshButton
        );

        refreshPosts(postsListView);
        return postsLayout;
    }

    private void refreshPosts(VBox postsListView) {
        postsListView.getChildren().clear();
        List<Post> posts = postController.loadPosts(userController.getCurrentUser().getUserId());
        
        for (Post post : posts) {
            PostView postView = new PostView(post);
            postsListView.getChildren().add(postView);
        }
    }

    private VBox createFriendsTab() {
        VBox friendsLayout = new VBox(10);
        friendsLayout.setPadding(new Insets(10));
        
        // Add friend section
        HBox addFriendBox = new HBox(10);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username to add");
        Button addFriendButton = new Button("Add Friend");
        addFriendBox.getChildren().addAll(usernameField, addFriendButton);
        
        // Add friend button action
        addFriendButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                User userToAdd = userController.getUserByUsername(username);
                if (userToAdd != null) {
                    if (friendController.sendFriendRequest(
                        userController.getCurrentUser().getUserId(), 
                        userToAdd.getUserId())) {
                        DialogFactory.showAlert(Alert.AlertType.INFORMATION, 
                            "Success", "Friend request sent!");
                        usernameField.clear();
                    } else {
                        DialogFactory.showAlert(Alert.AlertType.ERROR, 
                            "Error", "Failed to send friend request!");
                    }
                } else {
                    DialogFactory.showAlert(Alert.AlertType.ERROR, 
                        "Error", "User not found!");
                }
            }
        });
        
        // Friend requests section
        ListView<FriendRequest> requestsListView = new ListView<>();
        requestsListView.setCellFactory(lv -> new FriendRequestCell());
        
        // Load pending friend requests
        List<FriendRequest> pendingRequests = friendController.getPendingRequests(
            userController.getCurrentUser().getUserId()
        );
        requestsListView.getItems().addAll(pendingRequests);
        
        // Friends list view
        ListView<User> friendsListView = new ListView<>();
        friendsListView.setCellFactory(lv -> new FriendListCell());
        
        // Load friends
        List<User> friends = friendController.getFriends(userController.getCurrentUser().getUserId());
        friendsListView.getItems().addAll(friends);
        
        // Add refresh button
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            friendsListView.getItems().clear();
            friendsListView.getItems().addAll(
                friendController.getFriends(userController.getCurrentUser().getUserId())
            );
            requestsListView.getItems().clear();
            requestsListView.getItems().addAll(
                friendController.getPendingRequests(userController.getCurrentUser().getUserId())
            );
        });
        
        friendsLayout.getChildren().addAll(
            new Label("Add Friend"),
            addFriendBox,
            new Separator(),
            new Label("Friend Requests"),
            requestsListView,
            new Separator(),
            new Label("Your Friends"),
            friendsListView,
            refreshButton
        );
        
        return friendsLayout;
    }

    private VBox createMessagesContent() {
        VBox messagesContent = new VBox(10);
        messagesContent.setPadding(new Insets(10));

        // Message composition area
        ComboBox<String> recipientComboBox = new ComboBox<>();
        TextArea messageContent = new TextArea();
        messageContent.setPromptText("Type your message here...");
        messageContent.setPrefRowCount(3);
        
        Button sendButton = new Button("Send Message");
        
        // Load recipients (all users except current user)
        UserController userController = UserController.getInstance();
        List<User> friends = userController.getFriends(currentUser.getUserId());
        for (User friend : friends) {
            recipientComboBox.getItems().add(friend.getUsername());
        }

        // Message sending action
        sendButton.setOnAction(e -> {
            String recipientName = recipientComboBox.getValue();
            String content = messageContent.getText();
            
            if (recipientName != null && !content.trim().isEmpty()) {
                User recipient = userController.getUserByUsername(recipientName);
                MessageController messageController = MessageController.getInstance();
                
                if (messageController.sendMessage(currentUser.getUserId(), recipient.getUserId(), content)) {
                    messageContent.clear();
                    refreshMessages();  // Refresh the message list
                    DialogFactory.showInformation("Success", "Message sent successfully!");
                } else {
                    DialogFactory.showError("Error", "Failed to send message.");
                }
            } else {
                DialogFactory.showError("Error", "Please select a recipient and enter a message.");
            }
        });

        // Message display area
        VBox messageList = new VBox(5);
        ScrollPane scrollPane = new ScrollPane(messageList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(300);

        // Refresh button
        Button refreshButton = new Button("Refresh Messages");
        refreshButton.setOnAction(e -> refreshMessages());

        // Add all components
        messagesContent.getChildren().addAll(
            new Label("Select Recipient:"),
            recipientComboBox,
            new Label("Message:"),
            messageContent,
            sendButton,
            refreshButton,
            new Label("Messages:"),
            scrollPane
        );

        return messagesContent;
    }

    private void refreshMessages() {
        MessageController messageController = MessageController.getInstance();
        List<Message> messages = messageController.getMessages(currentUser.getUserId());
        
        VBox messageList = new VBox(5);
        UserController userController = UserController.getInstance();

        for (Message message : messages) {
            String senderName = userController.getUserById(message.getSenderId()).getUsername();
            String receiverName = userController.getUserById(message.getReceiverId()).getUsername();
            
            VBox messageBox = new VBox(2);
            messageBox.setStyle("-fx-padding: 5; -fx-border-color: #cccccc; -fx-border-radius: 5;");
            
            Label headerLabel = new Label(String.format("From: %s To: %s", senderName, receiverName));
            headerLabel.setStyle("-fx-font-weight: bold;");
            
            messageBox.getChildren().addAll(
                headerLabel,
                new Label(message.getContent()),
                new Label("Sent: " + message.getTimestamp().toString())
            );
            
            messageList.getChildren().add(messageBox);
        }

        // Find the ScrollPane in the messages tab and update its content
        Tab messagesTab = findTab("Messages");
        if (messagesTab != null) {
            VBox content = (VBox) messagesTab.getContent();
            ScrollPane scrollPane = (ScrollPane) content.getChildren().get(7); // Adjust index if needed
            scrollPane.setContent(messageList);
        }
    }

    private Tab findTab(String tabName) {
        VBox root = (VBox) mainScene.getRoot();
        TabPane tabPane = (TabPane) root.getChildren().get(0);
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText().equals(tabName)) {
                return tab;
            }
        }
        return null;
    }

    private VBox createNotificationsTab() {
        VBox notificationsLayout = new VBox(10);
        notificationsLayout.setPadding(new Insets(10));

        ListView<Notification> notificationsListView = new ListView<>();
        notificationsListView.setCellFactory(lv -> new NotificationCell());
        
        Button refreshButton = new Button("Refresh Notifications");
        refreshButton.setOnAction(e -> refreshNotifications(notificationsListView));

        Button markAllReadButton = new Button("Mark All as Read");
        markAllReadButton.setOnAction(e -> {
            NotificationController.getInstance().markAllAsRead(
                userController.getCurrentUser().getUserId()
            );
            refreshNotifications(notificationsListView);
        });

        notificationsLayout.getChildren().addAll(
            new Label("Notifications"),
            notificationsListView,
            markAllReadButton,
            refreshButton
        );

        refreshNotifications(notificationsListView);
        return notificationsLayout;
    }

    private void refreshNotifications(ListView<Notification> notificationsListView) {
        notificationsListView.getItems().clear();
        notificationsListView.getItems().addAll(
            NotificationController.getInstance().getAllNotifications(
                userController.getCurrentUser().getUserId()
            )
        );
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
                    createProfileTab();
                }
            }
        });
    }

    private VBox createAccountManagementContent() {
        VBox accountManagementContent = new VBox(10);
        accountManagementContent.setPadding(new Insets(10));

        // Add Delete Account button
        Button deleteAccountButton = new Button("Delete Account");
        deleteAccountButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
        
        deleteAccountButton.setOnAction(e -> {
            boolean confirm = DialogFactory.showConfirmation(
                "Delete Account",
                "Delete Account Confirmation",
                "Are you sure you want to delete your account? This action cannot be undone."
            );
            
            if (confirm) {
                UserController userController = UserController.getInstance();
                if (userController.deleteAccount(currentUser.getUserId())) {
                    DialogFactory.showInformation(
                        "Account Deleted",
                        "Your account has been successfully deleted."
                    );
                    logout();  // Return to login screen
                } else {
                    DialogFactory.showError(
                        "Error",
                        "Failed to delete account. Please try again."
                    );
                }
            }
        });

        accountManagementContent.getChildren().addAll(
            new Separator(),
            deleteAccountButton
        );

        return accountManagementContent;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
