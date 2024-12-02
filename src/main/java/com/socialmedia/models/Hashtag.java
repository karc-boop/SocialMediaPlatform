package com.socialmedia.models;

public class Hashtag {
    private int hashtagId;
    private String tagName;

    public Hashtag(int hashtagId, String tagName) {
        this.hashtagId = hashtagId;
        this.tagName = tagName;
    }

    public int getHashtagId() { return hashtagId; }
    public String getTagName() { return tagName; }
} 