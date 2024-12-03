package com.socialmedia.ui;

import com.socialmedia.controllers.UserController;
import com.socialmedia.models.UserSettings;
import com.socialmedia.models.PrivacyLevel;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class ProfileSettingsDialog extends Dialog<ButtonType> {
    private final UserController userController = UserController.getInstance();
    private final UserSettings settings;

    public ProfileSettingsDialog(int userId) {
        settings = userController.getUserSettings(userId);

        setTitle("Profile Settings");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        CheckBox notificationsCheckBox = new CheckBox("Enable Notifications");
        notificationsCheckBox.setSelected(settings.isNotificationsEnabled());

        ComboBox<PrivacyLevel> privacyComboBox = new ComboBox<>();
        privacyComboBox.getItems().setAll(PrivacyLevel.values());
        privacyComboBox.setValue(settings.getPrivacyLevel());

        grid.add(new Label("Notifications:"), 0, 0);
        grid.add(notificationsCheckBox, 1, 0);
        grid.add(new Label("Privacy Level:"), 0, 1);
        grid.add(privacyComboBox, 1, 1);

        getDialogPane().setContent(grid);

        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                settings.setNotificationsEnabled(notificationsCheckBox.isSelected());
                settings.setPrivacyLevel(privacyComboBox.getValue());
                userController.updateUserSettings(settings);
            }
            return null;
        });
    }
} 