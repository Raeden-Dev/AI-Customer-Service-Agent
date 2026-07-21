package com.ulab.agent.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One phone call: who called, when, the full message list, and the transcripts.
 * Lives in memory while the call is active; saved into call-history.json when it ends.
 */
public class Call {
    private String callId = UUID.randomUUID().toString();
    private String agentId;
    private String customerId;   // clientId for existing customers, "unknown" for new callers
    private String callMode;     // which CallMode the call is in (see ai/CallMode.java)
    private String startTime;
    private String endTime;
    private String transcript;
    private String aiTranscript;
    private String translated;
    private String sentiment;
    private String callContext;
    private String callType;
    private List<ChatMessage> messages = new ArrayList<>();

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCallMode() {
        return callMode;
    }

    public void setCallMode(String callMode) {
        this.callMode = callMode;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getCallContext() {
        return callContext;
    }

    public void setCallContext(String callContext) {
        this.callContext = callContext;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public String getTranslated() {
        return translated;
    }

    public void setTranslated(String translated) {
        this.translated = translated;
    }

    public void appendTranscript(String chunk) {
        if (chunk == null || chunk.isBlank()) return;
        if (transcript == null || transcript.isBlank()) {
            transcript = chunk;
        } else {
            transcript = transcript + " " + chunk;
        }
    }

    public String getAiTranscript() { return aiTranscript; }
    public void setAiTranscript(String aiTranscript) { this.aiTranscript = aiTranscript; }

    public void appendAiTranscript(String chunk) {
        if (chunk == null || chunk.isBlank()) return;
        if (aiTranscript == null || aiTranscript.isBlank()) {
            aiTranscript = chunk;
        } else {
            aiTranscript = aiTranscript + " " + chunk;
        }
    }

    public void appendTranslated(String chunk) {
        if (chunk == null || chunk.isBlank()) return;
        if (translated == null || translated.isBlank()) {
            translated = chunk;
        } else {
            translated = translated + " " + chunk;
        }
    }

    public List<ChatMessage> getMessages() {
        if (messages == null) messages = new ArrayList<>();
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) { this.messages = messages; }

    public void addMessage(ChatMessage message) {
        getMessages().add(message);
    }
}
