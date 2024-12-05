package com.socialmedia.controllers;

import com.socialmedia.models.Notification;
import java.sql.*;
import com.socialmedia.utils.ErrorHandler;
import java.util.ArrayList;
import java.util.List;

public class NotificationController {
    private final DatabaseController dbController;
    private static NotificationController instance;

    private NotificationController() {
        dbController = DatabaseController.getInstance();
    }

    public static NotificationController getInstance() {
        if (instance == null) {
            instance = new NotificationController();
        }
        return instance;
    }

    public void createNotification(int userId, String message, String type) {
        try {
            String sql = "INSERT INTO notifications (UserID, Message, Type, IsRead, Timestamp) VALUES (?, ?, ?, false, NOW())";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, message);
            stmt.setString(3, type);
            
            stmt.executeUpdate();
            dbController.commit();
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
        }
    }

    public List<Notification> getAllNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        try {
            String sql = "SELECT * FROM notifications WHERE UserID = ? ORDER BY Timestamp DESC";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(new Notification(
                    rs.getInt("NotificationID"), rs.getInt("UserID"),
                    rs.getString("Message"),rs.getString("Type"), rs.getBoolean("IsRead"),rs.getTimestamp("Timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public void markAllAsRead(int userId) {
        try {
            String sql = "UPDATE notifications SET IsRead = true WHERE UserID = ?";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            dbController.commit();
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
        }
    }

    public void markAsRead(int notificationId) {
        try {
            String sql = "UPDATE notifications SET IsRead = true WHERE NotificationID = ?";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
            dbController.commit();
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
        }
    }
}