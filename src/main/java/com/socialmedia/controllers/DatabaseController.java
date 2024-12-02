package com.socialmedia.controllers;

import java.sql.*;

public class DatabaseController {
    private static DatabaseController instance;
    private Connection conn;

    private DatabaseController() {}

    public static DatabaseController getInstance() {
        if (instance == null) {
            instance = new DatabaseController();
        }
        return instance;
    }

    public boolean connect(String dbUser, String dbPassword) {
        try {
            String dbUrl = "jdbc:mysql://localhost:3306/social_media?useSSL=false&serverTimezone=UTC";
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            conn.setAutoCommit(false);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public void commit() throws SQLException {
        if (conn != null && !conn.getAutoCommit()) {
            conn.commit();
        }
    }

    public void rollback() {
        try {
            if (conn != null && !conn.getAutoCommit()) {
                conn.rollback();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 