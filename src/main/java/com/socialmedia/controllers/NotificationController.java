package com.socialmedia.controllers;

import com.socialmedia.models.Notification;
import com.socialmedia.models.UserSettings;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationController {
    private final DatabaseController dbController;
    private final UserController userController;
    private static NotificationController instance;

    private NotificationController() {
        dbController = DatabaseController.getInstance();
        userController = UserController.getInstance();
    }

    public static NotificationController getInstance() {
        if (instance == null) {
            instance = new NotificationController();
        }
        return instance;
    }

    public void createNotification(int userId, String message, String type) {
        UserSettings settings = userController.getUserSettings(userId);
        if (settings == null || !settings.isNotificationsEnabled()) {
            return; // Don't create notification if disabled
        }

        try {
            String sql = "INSERT INTO notifications (UserID, Message, Type, IsRead, Timestamp) VALUES (?, ?, ?, false, NOW())";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, message);
            stmt.setString(3, type);
            
            stmt.executeUpdate();
            dbController.commit();
        } catch (SQLException e) {
            dbController.rollback();
            e.printStackTrace();
        }
    }

    public List<Notification> getUnreadNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        try {
            String query = "SELECT * FROM notifications WHERE UserID = ? AND IsRead = false ORDER BY Timestamp DESC";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(new Notification(
                    rs.getInt("NotificationID"),
                    rs.getInt("UserID"),
                    rs.getString("Message"),
                    rs.getString("Type"),
                    rs.getBoolean("IsRead"),
                    rs.getTimestamp("Timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public List<Notification> getAllNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        try {
            String query = "SELECT * FROM notifications WHERE UserID = ? ORDER BY Timestamp DESC";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                notifications.add(new Notification(
                    rs.getInt("NotificationID"),
                    rs.getInt("UserID"),
                    rs.getString("Message"),
                    rs.getString("Type"),
                    rs.getBoolean("IsRead"),
                    rs.getTimestamp("Timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }

    public boolean markAsRead(int notificationId) {
        try {
            String sql = "UPDATE notifications SET IsRead = true WHERE NotificationID = ?";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, notificationId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                dbController.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            dbController.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean markAllAsRead(int userId) {
        try {
            String sql = "UPDATE notifications SET IsRead = true WHERE UserID = ?";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, userId);
            
            stmt.executeUpdate();
            dbController.commit();
            return true;
        } catch (SQLException e) {
            dbController.rollback();
            e.printStackTrace();
            return false;
        }
    }
} 