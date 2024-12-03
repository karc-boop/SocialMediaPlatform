package com.socialmedia.ui;

import com.socialmedia.controllers.FriendController;
import com.socialmedia.models.FriendRequest;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

public class FriendRequestCell extends ListCell<FriendRequest> {
    private final FriendController friendController = FriendController.getInstance();

    @Override
    protected void updateItem(FriendRequest request, boolean empty) {
        super.updateItem(request, empty);

        if (empty || request == null) {
            setText(null);
            setGraphic(null);
        } else {
            HBox container = new HBox(10);
            
            Text nameText = new Text(request.getSenderUsername());
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button acceptButton = new Button("Accept");
            Button rejectButton = new Button("Reject");
            
            acceptButton.setOnAction(e -> {
                if (friendController.respondToFriendRequest(request.getRequestId(), true)) {
                    getListView().getItems().remove(request);
                }
            });
            
            rejectButton.setOnAction(e -> {
                if (friendController.respondToFriendRequest(request.getRequestId(), false)) {
                    getListView().getItems().remove(request);
                }
            });
            
            container.getChildren().addAll(nameText, spacer, acceptButton, rejectButton);
            setGraphic(container);
        }
    }
} 