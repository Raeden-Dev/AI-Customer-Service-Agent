package com.ulab.agent.ai;

/**
 * The four call scenarios the AI can handle.
 *
 * A call always starts in one of the first two modes:
 *   - NEW_CUSTOMER      -> "start-call" with no client id
 *   - EXISTING_CUSTOMER -> "start-call <client_id>"
 *
 * The other two modes are switched to DURING a call:
 *   - The AI can switch by itself: it puts a tag like [MODE:WRONG_NUMBER]
 *     at the end of a reply, and the Python side reports it back to Java.
 *   - The operator can also force a mode with the "set-mode" command.
 *
 * Each mode carries the extra instructions that get pasted into the AI prompt.
 */
public enum CallMode {

    NEW_CUSTOMER("New Customer",
            "The caller is a NEW customer that the business has no record of. "
                    + "Greet them warmly, ask for their name, and find out what they need. "
                    + "Use the business info below to explain services, prices and hours. "
                    + "Do not invent services or facts that are not in the business info."),

    EXISTING_CUSTOMER("Existing Customer",
            "The caller is a KNOWN client of the business. Their profile is in the client info below. "
                    + "Greet them by name, and use their notes and past issues to give personal help. "
                    + "Do not ask for information the profile already contains."),

    WRONG_NUMBER("Wrong / Scam Number",
            "This call looks like a wrong number or a scam call. "
                    + "Stay calm and polite. Do NOT share any business or client details. "
                    + "Briefly explain that they may have the wrong number and end the call politely. "
                    + "If the caller is abusive or clearly a scammer, say goodbye and stop engaging."),

    COMPLEX_REQUEST("Complex Request",
            "The caller's request is too complex for you to solve (refunds you cannot approve, "
                    + "legal issues, angry escalations, anything needing human judgement). "
                    + "Tell the caller you are transferring them to a human agent and ask them to hold. "
                    + "Keep your replies short; a human will take over from here.");

    /** Short human-readable name, shown in the console and in transcripts. */
    private final String displayName;

    /** Extra instructions pasted into the AI prompt while this mode is active. */
    private final String aiInstructions;

    CallMode(String displayName, String aiInstructions) {
        this.displayName = displayName;
        this.aiInstructions = aiInstructions;
    }

    public String getDisplayName() { return displayName; }

    public String getAiInstructions() { return aiInstructions; }

    /**
     * Instructions that are ALWAYS in the prompt, telling the AI how to switch
     * modes by itself. The Python side looks for these tags in every reply,
     * strips them out before speaking, and reports the switch to Java.
     */
    public static final String MODE_SWITCH_INSTRUCTIONS =
            "You can flag two special situations by adding a tag at the END of your reply. "
                    + "If you realize the caller dialed the wrong number, or the call is a scam, "
                    + "add the tag [MODE:WRONG_NUMBER]. "
                    + "If the caller needs something too complex for you and a human should take over, "
                    + "add the tag [MODE:COMPLEX_REQUEST]. "
                    + "Use a tag at most once per call and never mention the tags out loud.";

    /**
     * Safe parse: returns the matching mode or null instead of throwing.
     * Accepts "WRONG_NUMBER", "wrong_number", "wrong-number" etc.
     */
    public static CallMode fromString(String raw) {
        if (raw == null) return null;
        String cleaned = raw.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        for (CallMode mode : values()) {
            if (mode.name().equals(cleaned)) return mode;
        }
        return null;
    }
}
