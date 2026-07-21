package com.ulab.agent.api;

/**
 * Response body of GET /api/call-context.
 * Everything the Python AI needs to know about the current call, already
 * rendered as plain text blocks so Python can paste them straight into
 * the prompt without understanding our data models.
 */
public class CallContextResponse {

    private boolean callActive;
    private String mode;                    // e.g. "NEW_CUSTOMER"
    private String modeName;                // e.g. "New Customer"
    private String modeInstructions;        // what the AI should do in this mode
    private String modeSwitchInstructions;  // how the AI can flag WRONG_NUMBER / COMPLEX_REQUEST
    private String businessName;
    private String businessInfo;            // text block from intelligence.json
    private String clientInfo;              // text block from clients.json ("" when caller is unknown)
    private String greeting;                // opening line the AI speaks at call start

    public boolean isCallActive() { return callActive; }
    public void setCallActive(boolean callActive) { this.callActive = callActive; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getModeName() { return modeName; }
    public void setModeName(String modeName) { this.modeName = modeName; }

    public String getModeInstructions() { return modeInstructions; }
    public void setModeInstructions(String modeInstructions) { this.modeInstructions = modeInstructions; }

    public String getModeSwitchInstructions() { return modeSwitchInstructions; }
    public void setModeSwitchInstructions(String modeSwitchInstructions) { this.modeSwitchInstructions = modeSwitchInstructions; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessInfo() { return businessInfo; }
    public void setBusinessInfo(String businessInfo) { this.businessInfo = businessInfo; }

    public String getClientInfo() { return clientInfo; }
    public void setClientInfo(String clientInfo) { this.clientInfo = clientInfo; }

    public String getGreeting() { return greeting; }
    public void setGreeting(String greeting) { this.greeting = greeting; }
}
