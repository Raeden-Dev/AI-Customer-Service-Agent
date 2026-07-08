package com.ulab.agent.stt;

public class ChatMessageRequest {
    private String role;    // "user", "ai", "system"
    private String content;

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
