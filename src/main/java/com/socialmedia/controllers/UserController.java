package com.socialmedia.controllers;

import com.socialmedia.models.User;
import com.socialmedia.models.UserSettings;
import com.socialmedia.models.PrivacyLevel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserController {
    private final DatabaseController dbController;
    private static UserController instance;
    private User currentUser;

    private UserController() {
        dbController = DatabaseController.getInstance();
    }

    public static UserController getInstance() {
        if (instance == null) {
            instance = new UserController();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        try {
            String passwordHash = hashPassword(password);
            String query = "SELECT UserID, Email, Name, Bio, ProfilePicture FROM users WHERE Username = ? AND PasswordHash = ?";
            
            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentUser = new User(
                    rs.getInt("UserID"),
                    username,
                    rs.getString("Email"),
                    rs.getString("Name"),
                    rs.getString("Bio"),
                    rs.getString("ProfilePicture")
                );
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerUser(String username, String email, String password, String name, String bio, String profilePicture) {
        try {
            String sql = "INSERT INTO users (Username, Email, PasswordHash, Name, Bio, ProfilePicture) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashPassword(password));
            stmt.setString(4, name);
            stmt.setString(5, bio);
            stmt.setString(6, profilePicture);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int userId = generatedKeys.getInt(1);
                    String settingsSql = "INSERT INTO user_settings (UserID, NotificationsEnabled, PrivacyLevel) VALUES (?, 1, 'PUBLIC')";
                    PreparedStatement settingsStmt = dbController.getConnection().prepareStatement(settingsSql);
                    settingsStmt.setInt(1, userId);
                    settingsStmt.executeUpdate();
                }
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

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public User getUserByUsername(String username) {
        try {
            String query = "SELECT UserID, Username, Email, Name, Bio, ProfilePicture FROM users WHERE Username = ?";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("Email"),
                    rs.getString("Name"),
                    rs.getString("Bio"),
                    rs.getString("ProfilePicture")
                );
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateProfile(int userId, String email, String password, String name, String bio, String profilePicture) {
        try {
            String sql;
            PreparedStatement stmt;

            if (password != null && !password.isEmpty()) {
                sql = "UPDATE users SET Email = ?, PasswordHash = ?, Name = ?, Bio = ?, ProfilePicture = ? WHERE UserID = ?";
                stmt = dbController.getConnection().prepareStatement(sql);
                stmt.setString(1, email);
                stmt.setString(2, hashPassword(password));
                stmt.setString(3, name);
                stmt.setString(4, bio);
                stmt.setString(5, profilePicture);
                stmt.setInt(6, userId);
            } else {
                sql = "UPDATE users SET Email = ?, Name = ?, Bio = ?, ProfilePicture = ? WHERE UserID = ?";
                stmt = dbController.getConnection().prepareStatement(sql);
                stmt.setString(1, email);
                stmt.setString(2, name);
                stmt.setString(3, bio);
                stmt.setString(4, profilePicture);
                stmt.setInt(5, userId);
            }

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                dbController.commit();
                currentUser = new User(userId, currentUser.getUsername(), email, name, bio, profilePicture);
                return true;
            }
            return false;
        } catch (SQLException e) {
            dbController.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public User getUserById(int userId) {
        try {
            String query = "SELECT UserID, Username, Email, Name, Bio, ProfilePicture FROM users WHERE UserID = ?";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("Email"),
                    rs.getString("Name"),
                    rs.getString("Bio"),
                    rs.getString("ProfilePicture")
                );
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public UserSettings getUserSettings(int userId) {
        try {
            String query = "SELECT * FROM user_settings WHERE UserID = ?";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new UserSettings(
                    rs.getInt("SettingID"),
                    rs.getInt("UserID"),
                    rs.getBoolean("NotificationsEnabled"),
                    PrivacyLevel.valueOf(rs.getString("PrivacyLevel"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateUserSettings(UserSettings settings) {
        try {
            String query = "UPDATE user_settings SET NotificationsEnabled = ?, PrivacyLevel = ? WHERE UserID = ?";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setBoolean(1, settings.isNotificationsEnabled());
            stmt.setString(2, settings.getPrivacyLevel().name());
            stmt.setInt(3, settings.getUserId());

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

    public boolean deleteAccount(int userId) {
        try {
            // Delete related friend requests
            String deleteFriendRequestsSql = "DELETE FROM friend_requests WHERE SenderID = ? OR ReceiverID = ?";
            PreparedStatement deleteFriendRequestsStmt = dbController.getConnection().prepareStatement(deleteFriendRequestsSql);
            deleteFriendRequestsStmt.setInt(1, userId);
            deleteFriendRequestsStmt.setInt(2, userId);
            deleteFriendRequestsStmt.executeUpdate();

            // Now delete the user
            String deleteUserSql = "DELETE FROM users WHERE UserID = ?";
            PreparedStatement deleteUserStmt = dbController.getConnection().prepareStatement(deleteUserSql);
            deleteUserStmt.setInt(1, userId);
            
            int rowsAffected = deleteUserStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                dbController.commit();
                System.out.println("Account deleted successfully.");
                return true;
            }
            return false;
        } catch (SQLException e) {
            dbController.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getFriends(int userId) {
        List<User> friends = new ArrayList<>();
        try {
            String query = "SELECT u.* FROM users u " +
                          "JOIN friend_requests f ON (f.SenderID = u.UserID OR f.ReceiverID = u.UserID) " +
                          "WHERE (f.SenderID = ? OR f.ReceiverID = ?) " +
                          "AND u.UserID != ? " +
                          "AND f.Status = 'ACCEPTED'";
            
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
            e.printStackTrace();
        }
        return friends;
    }
}