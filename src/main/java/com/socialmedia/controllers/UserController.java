package com.socialmedia.controllers;

import com.socialmedia.models.User;
import java.sql.*;

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
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashPassword(password));
            stmt.setString(4, name);
            stmt.setString(5, bio);
            stmt.setString(6, profilePicture);

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
}