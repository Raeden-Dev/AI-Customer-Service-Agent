package com.ulab.agent.models;

public class AISettings {
    private String model;
    private String replyInstructions; // How the AI should respond to the user
    private String roleInstructions; // What role the AI should play in the conversation
    private String organizationInfo; // Information about the organization that the AI should use to answer questions

    public AISettings() {}

    public AISettings(String model, String replyInstructions, String roleInstructions, String organizationInfo) {
        this.model = model;
        this.replyInstructions = replyInstructions;
        this.roleInstructions = roleInstructions;
        this.organizationInfo = organizationInfo;
    }

    public static AISettings defaults() {
        return new AISettings(
                "gemini-2.5-flash",
                "Reply politely and concisely. If you are unsure, ask a clarifying question.",
                "You are a customer service agent. Assist customers with inquiries and provide accurate information.",
                ""
        );
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getReplyInstructions() { return replyInstructions; }
    public void setReplyInstructions(String replyInstructions) { this.replyInstructions = replyInstructions; }

    public String getRoleInstructions() { return roleInstructions; }
    public void setRoleInstructions(String roleInstructions) { this.roleInstructions = roleInstructions; }

    public String getOrganizationInfo() { return organizationInfo; }
    public void setOrganizationInfo(String organizationInfo) { this.organizationInfo = organizationInfo; }
}
