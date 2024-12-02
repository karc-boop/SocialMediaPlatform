package com.socialmedia.models;

public class User {
    private int userId;
    private String username;
    private String email;
    private String name;
    private String bio;
    private String profilePicture;

    // Constructor
    public User(int userId, String username, String email, String name, String bio, String profilePicture) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.name = name;
        this.bio = bio;
        this.profilePicture = profilePicture;
    }

    // Getters and setters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getBio() { return bio; }
    public String getProfilePicture() { return profilePicture; }
} 