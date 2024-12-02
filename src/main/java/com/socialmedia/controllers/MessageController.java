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
            String sql = "INSERT INTO messages (SenderID, ReceiverID, Content, Timestamp) VALUES (?, ?, ?, NOW())";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, content);

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

    public List<Message> loadMessages(int userId) {
        List<Message> messages = new ArrayList<>();
        try {
            String query = "SELECT m.MessageID, m.SenderID, m.ReceiverID, m.Content, m.Timestamp, " +
                          "u.Username as SenderUsername " +
                          "FROM messages m " +
                          "JOIN users u ON m.SenderID = u.UserID " +
                          "WHERE m.ReceiverID = ? " +
                          "ORDER BY m.Timestamp DESC";

            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Message message = new Message(
                    rs.getInt("MessageID"),
                    rs.getInt("SenderID"),
                    rs.getInt("ReceiverID"),
                    rs.getString("Content"),
                    rs.getTimestamp("Timestamp")
                );
                message.setSenderUsername(rs.getString("SenderUsername"));
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
} 