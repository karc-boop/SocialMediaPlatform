package com.socialmedia.ui;

import com.socialmedia.models.User;
import com.socialmedia.controllers.FriendController;
import com.socialmedia.controllers.UserController;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;

public class FriendListCell extends ListCell<User> {
    private final FriendController friendController = FriendController.getInstance();
    private final UserController userController = UserController.getInstance();
    
    @Override
    protected void updateItem(User friend, boolean empty) {
        super.updateItem(friend, empty);
        
        if (empty || friend == null) {
            setText(null);
            setGraphic(null);
        } else {
            VBox container = new VBox(5);
            container.setPadding(new Insets(5));
            container.setStyle("-fx-background-color: white; -fx-border-color: #eee; -fx-border-radius: 5;");
            
            // Friend name
            Label nameLabel = new Label(friend.getUsername());
            nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            // Shared friends count
            int sharedCount = friendController.getSharedFriendsCount(
                userController.getCurrentUser().getUserId(),
                friend.getUserId()
            );
            
            Label sharedLabel = new Label(sharedCount + " shared friends");
            sharedLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
            
            container.getChildren().addAll(nameLabel, sharedLabel);
            
            setGraphic(container);
        }
    }
} 