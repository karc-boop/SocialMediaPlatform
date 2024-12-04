package com.socialmedia.controllers;

import com.socialmedia.models.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageController {
    private final DatabaseController dbController;
    private static MessageController instance;

    private MessageController() {
        dbController = DatabaseController.getInstance();
    }

    public static MessageController getInstance() {
        if (instance == null) {
            instance = new MessageController();
        }
        return instance;
    }

    public boolean sendMessage(int senderId, int receiverId, String content) {
        try {
            System.out.println("Attempting to send message - From: " + senderId + " To: " + receiverId);
            String sql = "INSERT INTO messages (SenderID, ReceiverID, Content, Timestamp) VALUES (?, ?, ?, NOW())";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, content);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                System.out.println("Committing transaction...");
                dbController.commit();
                return true;
            }
            System.out.println("No rows affected, message not sent");
            return false;
        } catch (SQLException e) {
            System.out.println("SQL Exception occurred: " + e.getMessage());
            dbController.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public List<Message> getMessages(int userId) {
        List<Message> messages = new ArrayList<>();
        try {
            System.out.println("Fetching messages for user: " + userId);
            String sql = "SELECT * FROM messages WHERE SenderID = ? OR ReceiverID = ? ORDER BY Timestamp DESC";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(new Message(
                    rs.getInt("MessageID"),
                    rs.getInt("SenderID"),
                    rs.getInt("ReceiverID"),
                    rs.getString("Content"),
                    rs.getTimestamp("Timestamp")
                ));
            }
            System.out.println("Found " + messages.size() + " messages");
        } catch (SQLException e) {
            System.out.println("Error fetching messages: " + e.getMessage());
            e.printStackTrace();
        }
        return messages;
    }
} 