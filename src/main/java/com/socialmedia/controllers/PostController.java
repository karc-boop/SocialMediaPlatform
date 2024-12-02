package com.socialmedia.controllers;

import com.socialmedia.models.Post;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostController {
    private final DatabaseController dbController;
    private static PostController instance;

    private PostController() {
        dbController = DatabaseController.getInstance();
    }

    public static PostController getInstance() {
        if (instance == null) {
            instance = new PostController();
        }
        return instance;
    }

    public boolean createPost(int userId, String content) {
        try {
            String sql = "{CALL AddPost(?, ?, ?, ?)}";
            CallableStatement stmt = dbController.getConnection().prepareCall(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, content);
            stmt.setString(3, "text");
            stmt.setString(4, null);

            stmt.execute();
            dbController.commit();
            return true;
        } catch (SQLException e) {
            dbController.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public List<Post> loadPosts(int currentUserId) {
        List<Post> posts = new ArrayList<>();
        try {
            String query = "SELECT p.PostID, p.UserID, p.Content, p.Timestamp " +
                    "FROM posts p " +
                    "LEFT JOIN friendships f ON (f.UserID1 = ? AND f.UserID2 = p.UserID) " +
                    "OR (f.UserID2 = ? AND f.UserID1 = p.UserID) " +
                    "WHERE p.UserID = ? OR f.UserID1 IS NOT NULL OR f.UserID2 IS NOT NULL " +
                    "ORDER BY p.Timestamp DESC";

            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            stmt.setInt(3, currentUserId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Post post = new Post(
                    rs.getInt("PostID"),
                    rs.getInt("UserID"),
                    rs.getString("Content"),
                    rs.getTimestamp("Timestamp")
                );
                updatePostLikeCount(post);
                posts.add(post);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    public boolean likePost(int postId, int userId) {
        try {
            String sql = "{CALL AddPostLike(?, ?)}";
            CallableStatement stmt = dbController.getConnection().prepareCall(sql);
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);

            stmt.execute();
            dbController.commit();
            return true;
        } catch (SQLException e) {
            dbController.rollback();
            e.printStackTrace();
            return false;
        }
    }

    private void updatePostLikeCount(Post post) throws SQLException {
        String query = "SELECT COUNT(*) as LikeCount FROM post_likes WHERE PostID = ?";
        PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
        stmt.setInt(1, post.getPostId());
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            post.setLikeCount(rs.getInt("LikeCount"));
        }
    }

    public boolean isPostOwner(int postId, int userId) {
        try {
            String query = "SELECT UserID FROM posts WHERE PostID = ?";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("UserID") == userId;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePost(int postId) {
        try {
            String sql = "{CALL DeletePost(?)}";
            CallableStatement stmt = dbController.getConnection().prepareCall(sql);
            stmt.setInt(1, postId);

            int rowsAffected = stmt.executeUpdate();
            dbController.commit();
            return rowsAffected > 0;
        } catch (SQLException e) {
            dbController.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePost(int postId, String content) {
        try {
            String sql = "{CALL UpdatePost(?, ?, ?, ?)}";
            CallableStatement stmt = dbController.getConnection().prepareCall(sql);
            stmt.setInt(1, postId);
            stmt.setString(2, content);
            stmt.setString(3, "text");  // Default to text type
            stmt.setString(4, null);    // No media URL

            stmt.execute();
            dbController.commit();
            return true;
        } catch (SQLException e) {
            dbController.rollback();
            e.printStackTrace();
            return false;
        }
    }
} 