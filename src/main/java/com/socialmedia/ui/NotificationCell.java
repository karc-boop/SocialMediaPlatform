package com.socialmedia.ui;

import com.socialmedia.controllers.NotificationController;
import com.socialmedia.models.Notification;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.time.format.DateTimeFormatter;

public class NotificationCell extends ListCell<Notification> {
    private final NotificationController notificationController = NotificationController.getInstance();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @Override
    protected void updateItem(Notification notification, boolean empty) {
        super.updateItem(notification, empty);

        if (empty || notification == null) {
            setText(null);
            setGraphic(null);
        } else {
            VBox container = new VBox(5);
            
            Text messageText = new Text(notification.getMessage());
            Text timeText = new Text(notification.getTimestamp().toLocalDateTime().format(formatter));
            timeText.setStyle("-fx-fill: gray;");
            
            if (!notification.isRead()) {
                container.setStyle("-fx-background-color: #f0f0f0;");
                setOnMouseClicked(e -> {
                    notificationController.markAsRead(notification.getNotificationId());
                    container.setStyle("");
                });
            }
            
            container.getChildren().addAll(messageText, timeText);
            setGraphic(container);
        }
    }
} 