package com.socialmedia.controllers;

import com.socialmedia.utils.ErrorHandler;
import com.socialmedia.models.Comment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentController {
    private final DatabaseController dbController;
    private static CommentController instance;

    private CommentController() {
        dbController = DatabaseController.getInstance();
    }

    public static CommentController getInstance() {
        if (instance == null) {
            instance = new CommentController();
        }
        return instance;
    }

    public boolean createComment(int postId, int userId, String content) {
        try {
            String sql = "{CALL AddComment(?, ?, ?)}";
            CallableStatement stmt = dbController.getConnection().prepareCall(sql);
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            stmt.setString(3, content);

            stmt.execute();
            dbController.commit();
            return true;
        } catch (SQLException e) {
            try {
                dbController.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }

    public List<Comment> loadComments(int postId) {
        List<Comment> comments = new ArrayList<>();
        try {
            String query = "SELECT c.CommentID, c.UserID, c.Content, c.Timestamp " +
                    "FROM comments c " +
                    "WHERE c.PostID = ? " +
                    "ORDER BY c.Timestamp ASC";

            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Comment comment = new Comment(
                    rs.getInt("CommentID"),
                    postId,
                    rs.getInt("UserID"),
                    rs.getString("Content"),
                    rs.getTimestamp("Timestamp")
                );
                comments.add(comment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public boolean deleteComment(int commentId) {
        try {
            String sql = "{CALL DeleteComment(?)}";
            CallableStatement stmt = dbController.getConnection().prepareCall(sql);
            stmt.setInt(1, commentId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                dbController.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            try {
                dbController.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
} 