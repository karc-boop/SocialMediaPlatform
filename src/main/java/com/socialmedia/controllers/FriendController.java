package com.socialmedia.controllers;

import com.socialmedia.models.User;
import com.socialmedia.models.FriendRequest;
import com.socialmedia.utils.ErrorHandler;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendController {
    private final DatabaseController dbController;
    private static FriendController instance;

    private FriendController() {
        dbController = DatabaseController.getInstance();
    }

    public static FriendController getInstance() {
        if (instance == null) {
            instance = new FriendController();
        }
        return instance;
    }

    public List<User> loadFriends(int userId) {
        List<User> friends = new ArrayList<>();
        try {
            String query = "SELECT u.UserID, u.Username, u.Email, u.Name, u.Bio, u.ProfilePicture " +
                    "FROM friendships f " +
                    "JOIN users u ON (u.UserID = f.UserID1 AND f.UserID2 = ?) " +
                    "OR (u.UserID = f.UserID2 AND f.UserID1 = ?)";

            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                friends.add(new User(
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("Email"),
                    rs.getString("Name"),
                    rs.getString("Bio"),
                    rs.getString("ProfilePicture")
                ));
            }
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
        }
        return friends;
    }

    public boolean addFriend(int userId1, int userId2) {
        try {
            String sql = "{CALL AddFriendship(?, ?)}";
            CallableStatement stmt = dbController.getConnection().prepareCall(sql);
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);

            stmt.execute();
            dbController.commit();
            return true;
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return false;
        }
    }

    public boolean removeFriend(int userId1, int userId2) {
        try {
            String sql = "{CALL DeleteFriendship(?, ?)}";
            CallableStatement stmt = dbController.getConnection().prepareCall(sql);
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                dbController.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return false;
        }
    }

    public boolean areFriends(int userId1, int userId2) {
        try {
            String query = "SELECT * FROM friendships WHERE " +
                    "(UserID1 = ? AND UserID2 = ?) OR (UserID1 = ? AND UserID2 = ?)";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, Math.min(userId1, userId2));
            stmt.setInt(2, Math.max(userId1, userId2));
            stmt.setInt(3, Math.max(userId1, userId2));
            stmt.setInt(4, Math.min(userId1, userId2));
            
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return false;
        }
    }

    public boolean sendFriendRequest(int senderId, int receiverId) {
        try {
            String checkSql = "SELECT * FROM friend_requests WHERE " +
                    "(SenderID = ? AND ReceiverID = ? AND Status = 'PENDING') OR " +
                    "(SenderID = ? AND ReceiverID = ? AND Status = 'PENDING')";
            PreparedStatement checkStmt = dbController.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, senderId);
            checkStmt.setInt(2, receiverId);
            checkStmt.setInt(3, receiverId);
            checkStmt.setInt(4, senderId);
            
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return false; // Request already exists
            }

            // Insert new request
            String sql = "INSERT INTO friend_requests (SenderID, ReceiverID, Status, Timestamp) " +
                        "VALUES (?, ?, 'PENDING', NOW())";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                dbController.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return false;
        }
    }

    public boolean respondToFriendRequest(int requestId, boolean accept) {
        try {
            // Start transaction
            dbController.getConnection().setAutoCommit(false);
            
            // Get request details
            String selectSql = "SELECT SenderID, ReceiverID FROM friend_requests WHERE RequestID = ?";
            PreparedStatement selectStmt = dbController.getConnection().prepareStatement(selectSql);
            selectStmt.setInt(1, requestId);
            ResultSet rs = selectStmt.executeQuery();
            
            if (!rs.next()) {
                return false;
            }
            
            int senderId = rs.getInt("SenderID");
            int receiverId = rs.getInt("ReceiverID");

            // Update request status
            String updateSql = "UPDATE friend_requests SET Status = ? WHERE RequestID = ?";
            PreparedStatement updateStmt = dbController.getConnection().prepareStatement(updateSql);
            updateStmt.setString(1, accept ? "ACCEPTED" : "DECLINED");
            updateStmt.setInt(2, requestId);
            updateStmt.executeUpdate();

            if (accept) {
                // Add a friend
                String addFriendSql = "INSERT INTO friendships (UserID1, UserID2) VALUES (?, ?)";
                PreparedStatement addFriendStmt = dbController.getConnection().prepareStatement(addFriendSql);
                addFriendStmt.setInt(1, Math.min(senderId, receiverId));
                addFriendStmt.setInt(2, Math.max(senderId, receiverId));
                addFriendStmt.executeUpdate();
            }

            dbController.commit();
            return true;
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return false;
        }
    }

    public List<FriendRequest> getPendingRequests(int userId) {
        List<FriendRequest> requests = new ArrayList<>();
        try {
            String query = "SELECT fr.*, u.Username as SenderUsername " +
                          "FROM friend_requests fr " +
                          "JOIN users u ON fr.SenderID = u.UserID " +
                          "WHERE fr.ReceiverID = ? AND fr.Status = 'PENDING' " +
                          "ORDER BY fr.Timestamp DESC";

            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FriendRequest request = new FriendRequest(
                    rs.getInt("RequestID"),
                    rs.getInt("SenderID"),
                    rs.getInt("ReceiverID"),
                    rs.getString("Status"),
                    rs.getTimestamp("Timestamp")
                );
                request.setSenderUsername(rs.getString("SenderUsername"));
                requests.add(request);
            }
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
        }
        return requests;
    }

    public int getSharedFriendsCount(int userId1, int userId2) {
        try {
            String query = """
                SELECT COUNT(*) as shared_count
                FROM friendships f1
                JOIN friendships f2 ON (
                    f1.UserID2 = f2.UserID2 OR 
                    f1.UserID2 = f2.UserID1 OR 
                    f1.UserID1 = f2.UserID2
                )
                WHERE 
                    ((f1.UserID1 = ? AND f2.UserID1 = ?) OR 
                     (f1.UserID1 = ? AND f2.UserID2 = ?))
                    AND f1.UserID2 != ? 
                    AND f1.UserID2 != ?
                """;
                
            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId1);
            stmt.setInt(4, userId2);
            stmt.setInt(5, userId1);
            stmt.setInt(6, userId2);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("shared_count");
            }
            return 0;
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return 0;
        }
    }

    public List<User> getFriends(int userId) {
        List<User> friends = new ArrayList<>();
        try {
            String query = """
                SELECT u.* FROM users u
                JOIN friendships f ON (u.UserID = f.UserID1 OR u.UserID = f.UserID2)
                WHERE (f.UserID1 = ? OR f.UserID2 = ?) AND u.UserID != ?
            """;
            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                friends.add(new User(
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("Email"),
                    rs.getString("Name"),
                    rs.getString("Bio"),
                    rs.getString("ProfilePicture")
                ));
            }
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
        }
        return friends;
    }
} 