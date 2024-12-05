package com.socialmedia.models;

public class UserSettings {
    private int settingId;
    private int userId;
    private boolean notificationsEnabled;
    private PrivacyLevel privacyLevel;

    public UserSettings(int settingId, int userId, boolean notificationsEnabled, PrivacyLevel privacyLevel) {
        this.settingId = settingId;
        this.userId = userId;
        this.notificationsEnabled = notificationsEnabled;
        this.privacyLevel = privacyLevel;
    }

    // Getters and setters
    public int getSettingId() { 
        return settingId; 
    }
    public int getUserId() { 
        return userId; 
    }
    public boolean isNotificationsEnabled() { 
        return notificationsEnabled; 
    }
    public PrivacyLevel getPrivacyLevel() { 
        return privacyLevel; 
    }
    public void setNotificationsEnabled(boolean notificationsEnabled) { 
        this.notificationsEnabled = notificationsEnabled; 
    }
    public void setPrivacyLevel(PrivacyLevel privacyLevel) { 
        this.privacyLevel = privacyLevel; 
    }
} 