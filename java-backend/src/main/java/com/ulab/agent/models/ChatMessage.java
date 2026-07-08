package com.ulab.agent.models;

public class ChatMessage {
    public enum Role { USER, AI, SYSTEM }

    private Role role;
    private String content;
    private String timestamp;

    public ChatMessage() {}

    public ChatMessage(Role role, String content, String timestamp) {
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
