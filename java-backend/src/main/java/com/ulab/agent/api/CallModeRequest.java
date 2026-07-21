package com.ulab.agent.api;

/** Request body of POST /api/call-mode. */
public class CallModeRequest {
    private String mode;     // "WRONG_NUMBER" or "COMPLEX_REQUEST" (any CallMode name works)
    private String reason;   // who/why, e.g. "AI detected"

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
