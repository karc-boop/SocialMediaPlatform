package com.socialmedia.utils;

import java.sql.SQLException;
import com.socialmedia.controllers.DatabaseController;

public class ErrorHandler {

    public static void handleSQLException(SQLException e, DatabaseController dbController) {
        System.err.println("SQL Error: " + e.getMessage());
        e.printStackTrace();
        try {
            if (dbController != null) {
                dbController.rollback();
                System.out.println("Transaction rolled back.");
            }
        } catch (SQLException rollbackEx) {
            System.err.println("Error during rollback: " + rollbackEx.getMessage());
            rollbackEx.printStackTrace();
        }
    }

    public static void logError(String message, Exception e) {
        System.err.println("Error: " + message);
        e.printStackTrace();
    }
} 