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
                    if (friendController.addFriend(
                            userController.getCurrentUser().getUserId(), 
                            friend.getUserId())) {
                        DialogFactory.showAlert(Alert.AlertType.INFORMATION, 
                            "Success", "Friend added successfully!");
                        refreshFriendsList(friendsListView);
                        friendUsernameField.clear();
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

        // Messages list
        ListView<String> messagesListView = new ListView<>();
        
        // Send message section
        TextField recipientField = new TextField();
        recipientField.setPromptText("Recipient username");
        
        TextArea messageContent = new TextArea();
        messageContent.setPromptText("Type your message");
        messageContent.setPrefRowCount(3);
        
        Button sendButton = new Button("Send Message");
        sendButton.setOnAction(e -> {
            String recipient = recipientField.getText().trim();
            String content = messageContent.getText().trim();
            
            if (!recipient.isEmpty() && !content.isEmpty()) {
                User recipientUser = userController.getUserByUsername(recipient);
                if (recipientUser != null) {
                    // TODO: Implement MessageController and add message sending logic
                    messageContent.clear();
                    recipientField.clear();
                    refreshMessages(messagesListView);
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

    private void refreshMessages(ListView<String> messagesListView) {
        // TODO: Implement MessageController and add message loading logic
        messagesListView.getItems().clear();
    }

    private VBox createNotificationsTab() {
        VBox notificationsLayout = new VBox(10);
        notificationsLayout.setPadding(new Insets(10));

        // Notifications list
        ListView<String> notificationsListView = new ListView<>();
        
        Button refreshButton = new Button("Refresh Notifications");
        refreshButton.setOnAction(e -> refreshNotifications(notificationsListView));

        Button markAllReadButton = new Button("Mark All as Read");
        markAllReadButton.setOnAction(e -> {
            // TODO: Implement NotificationController and add mark-as-read logic
            refreshNotifications(notificationsListView);
        });

        notificationsLayout.getChildren().addAll(
            new Label("Notifications"),
            notificationsListView,
            refreshButton,
            markAllReadButton
        );

        refreshNotifications(notificationsListView);
        return notificationsLayout;
    }

    private void refreshNotifications(ListView<String> notificationsListView) {
        // TODO: Implement NotificationController and add notification loading logic
        notificationsListView.getItems().clear();
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

    public static void main(String[] args) {
        launch(args);
    }
}
