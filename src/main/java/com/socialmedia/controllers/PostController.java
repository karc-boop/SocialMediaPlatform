package com.socialmedia.controllers;

import com.socialmedia.models.Hashtag;
import com.socialmedia.models.Post;
import com.socialmedia.utils.ErrorHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }
        if (content.length() > 1000) {  // post length limit
            throw new IllegalArgumentException("Post content exceeds 1000 characters");
        }
        
        try {
            dbController.getConnection().setAutoCommit(false);
            
            // Create post
            String sql = "INSERT INTO posts (UserID, Content, MediaType, MediaURL, Timestamp) " +
                        "VALUES (?, ?, ?, ?, NOW())";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql, 
                Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, userId);
            stmt.setString(2, content);
            stmt.setString(3, "text");
            stmt.setString(4, null);

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Get the generated post ID
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int postId = rs.getInt(1);
                    
                    // Extract and link hashtags
                    List<Hashtag> hashtags = extractHashtags(content);
                    if (!hashtags.isEmpty()) {
                        linkHashtagsToPost(postId, hashtags);
                    }
                    
                    dbController.commit();
                    return true;
                }
            }
            dbController.rollback();
            return false;
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return false;
        }
    }


    public List<Post> loadPosts(int currentUserId) {
        List<Post> posts = new ArrayList<>();
        try {
            // load posts from friends and current user
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
                    rs.getInt("PostID"),rs.getInt("UserID"),rs.getString("Content"),
                    rs.getTimestamp("Timestamp")
                );
                updatePostLikeCount(post);
                posts.add(post);
            }
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
        }
        return posts;
    }

    public boolean togglePostLike(int postId, int userId) {
        try {
            // Check if like exists
            String checkSql = "SELECT LikeID FROM post_likes WHERE PostID = ? AND UserID = ?";
            PreparedStatement checkStmt = dbController.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, postId);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Unlike: remove existing like
                String deleteSql = "DELETE FROM post_likes WHERE PostID = ? AND UserID = ?";
                PreparedStatement deleteStmt = dbController.getConnection().prepareStatement(deleteSql);
                deleteStmt.setInt(1, postId);
                deleteStmt.setInt(2, userId);
                deleteStmt.executeUpdate();
            } else {
                // Like: add new like
                String insertSql = "INSERT INTO post_likes (PostID, UserID) VALUES (?, ?)";
                PreparedStatement insertStmt = dbController.getConnection().prepareStatement(insertSql);
                insertStmt.setInt(1, postId);
                insertStmt.setInt(2, userId);
                insertStmt.executeUpdate();

                // Create notification for post owner
                int postOwnerId = getPostOwner(postId);
                if (postOwnerId != userId) { // Don't notify if user likes their own post
                    NotificationController.getInstance().createNotification(
                        postOwnerId,
                        "Someone liked your post",
                        "POST_LIKE"
                    );
                }
            }

            dbController.commit();
            return true;
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return false;
        }
    }

    public int getPostLikeCount(int postId) {
        try {
            String sql = "SELECT COUNT(*) as LikeCount FROM post_likes WHERE PostID = ?";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("LikeCount");
            }
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
        }
        return 0;
    }

    public boolean isPostLikedByUser(int postId, int userId) {
        try {
            String sql = "SELECT LikeID FROM post_likes WHERE PostID = ? AND UserID = ?";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return false;
        }
    }

    private int getPostOwner(int postId) {
        try {
            String sql = "SELECT UserID FROM posts WHERE PostID = ?";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("UserID");
            }
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
        }
        return -1;
    }

    public void updatePostLikeCount(Post post) throws SQLException {
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
            ErrorHandler.handleSQLException(e, dbController);
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
            ErrorHandler.handleSQLException(e, dbController);
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
            stmt.setString(4, null);    // default no media URL

            stmt.execute();
            dbController.commit();
            return true;
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return false;
        }
    }

    public List<Hashtag> extractHashtags(String content) {
        List<Hashtag> hashtags = new ArrayList<>();
        // Match #word pattern
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String tag = matcher.group(1);
            int hashtagId = getOrCreateHashtag(tag);
            if (hashtagId != -1) {
                hashtags.add(new Hashtag(hashtagId, tag));
            }
        }
        return hashtags;
    }

    private int getOrCreateHashtag(String tagName) {
        try {
            dbController.getConnection().setAutoCommit(false);
            
            // First try to get existing hashtag
            String selectSql = "SELECT HashtagID FROM hashtags WHERE TagName = ?";
            PreparedStatement selectStmt = dbController.getConnection().prepareStatement(selectSql);
            selectStmt.setString(1, tagName.toLowerCase());  // Store hashtags in lowercase
            ResultSet rs = selectStmt.executeQuery();
            
            if (rs.next()) {
                dbController.commit();
                return rs.getInt("HashtagID");
            }

            // If not exists, create new hashtag
            String insertSql = "INSERT INTO hashtags (TagName) VALUES (?)";
            PreparedStatement insertStmt = dbController.getConnection().prepareStatement(
                insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, tagName.toLowerCase());
            insertStmt.executeUpdate();

            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int hashtagId = generatedKeys.getInt(1);
                dbController.commit();
                return hashtagId;
            }
            
            dbController.rollback();
            return -1;
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return -1;
        }
    }

    public void linkHashtagsToPost(int postId, List<Hashtag> hashtags) {
        try {
            dbController.getConnection().setAutoCommit(false);
            
            String sql = "INSERT INTO posthashtags (PostID, HashtagID) VALUES (?, ?)";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            
            for (Hashtag hashtag : hashtags) {
                stmt.setInt(1, postId);
                stmt.setInt(2, hashtag.getHashtagId());
                stmt.executeUpdate();  // Execute one by one instead of batch
            }
            
            dbController.commit();
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
        }
    }

    public List<Post> searchPostsByHashtag(String tagName) {
        List<Post> posts = new ArrayList<>();
        try {
            String query = "SELECT p.* FROM posts p " +
                          "JOIN posthashtags ph ON p.PostID = ph.PostID " +
                          "JOIN hashtags h ON ph.HashtagID = h.HashtagID " +
                          "WHERE h.TagName = ? " +
                          "ORDER BY p.Timestamp DESC";
            
            PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
            stmt.setString(1, tagName);
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
            ErrorHandler.handleSQLException(e, dbController);
        }
        return posts;
    }

    public boolean sharePost(int postId, int userId) {
        try {
            // First check if post exists
            String checkSql = "SELECT PostID FROM posts WHERE PostID = ?";
            PreparedStatement checkStmt = dbController.getConnection().prepareStatement(checkSql);
            checkStmt.setInt(1, postId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                throw new IllegalArgumentException("Post does not exist");
            }
            
            // Check if already shared
            String checkShareSql = "SELECT ShareID FROM shares WHERE PostID = ? AND UserID = ?";
            PreparedStatement checkShareStmt = dbController.getConnection().prepareStatement(checkShareSql);
            checkShareStmt.setInt(1, postId);
            checkShareStmt.setInt(2, userId);
            ResultSet shareRs = checkShareStmt.executeQuery();
            
            if (shareRs.next()) {
                throw new IllegalStateException("You have already shared this post");
            }

            String sql = "{CALL SharePost(?, ?)}";
            CallableStatement stmt = dbController.getConnection().prepareCall(sql);
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);

            stmt.execute();
            dbController.commit();
            return true;
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            throw new RuntimeException("Error sharing post: " + e.getMessage());
        }
    }

    public int getShareCount(int postId) throws SQLException {
        String query = "SELECT COUNT(*) as ShareCount FROM shares WHERE PostID = ?";
        PreparedStatement stmt = dbController.getConnection().prepareStatement(query);
        stmt.setInt(1, postId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("ShareCount");
        }
        return 0;
    }

    public boolean likePost(int userId, int postId) {
        try {
            String sql = "INSERT INTO likes (UserID, PostID) VALUES (?, ?)";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, postId);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                int postOwnerId = getPostOwner(postId);
                if (postOwnerId != userId) {
                    String username = UserController.getInstance().getUserById(userId).getUsername();
                    NotificationController.getInstance().createNotification(
                        postOwnerId,
                        username + " liked your post",
                        "LIKE"
                    );
                }
                dbController.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return false;
        }
    }

    public boolean addComment(int userId, int postId, String content) {
        try {
            String sql = "INSERT INTO comments (UserID, PostID, Content, Timestamp) VALUES (?, ?, ?, NOW())";
            PreparedStatement stmt = dbController.getConnection().prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, postId);
            stmt.setString(3, content);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                // Get post owner's ID
                int postOwnerId = getPostOwner(postId);
                if (postOwnerId != userId) {  // Don't notify if user comments on their own post
                    // Create notification
                    String username = UserController.getInstance().getUserById(userId).getUsername();
                    NotificationController.getInstance().createNotification(
                        postOwnerId,
                        username + " commented on your post",
                        "COMMENT"
                    );
                }
                dbController.commit();
                return true;
            }
            return false;
        } catch (SQLException e) {
            ErrorHandler.handleSQLException(e, dbController);
            return false;
        }
    }
} 