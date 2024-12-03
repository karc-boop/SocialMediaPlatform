package com.socialmedia.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class DialogFactory {
    public static void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    public static void showWarning(String title, String content) {
        showAlert(Alert.AlertType.WARNING, title, content);
    }

    public static void showError(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, content);
    }

    public static void showSuccess(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, content);
    }

    public static boolean isValidInput(String input, String fieldName, int minLength, int maxLength) {
        if (input == null || input.trim().isEmpty()) {
            showWarning("Invalid Input", fieldName + " cannot be empty");
            return false;
        }
        if (input.length() < minLength) {
            showWarning("Invalid Input", fieldName + " must be at least " + minLength + " characters");
            return false;
        }
        if (input.length() > maxLength) {
            showWarning("Invalid Input", fieldName + " cannot exceed " + maxLength + " characters");
            return false;
        }
        return true;
    }
} 