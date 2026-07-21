package com.ulab.agent.api;

/** Request body of POST /api/transcript. */
public class TranscriptRequest {
    private String transcript;
    private String translatedTranscript;
    private boolean isFinal;

    public String getTranscript() { return transcript;}
    public void setTranscript(String transcript) { this.transcript = transcript; }

    public boolean isFinal() { return isFinal; }
    public void setFinal(boolean isFinal) { this.isFinal = isFinal; }

    public String getTranslatedTranscript() {
        return translatedTranscript;
    }

    public void setTranslatedTranscript(String translatedTranscript) {
        this.translatedTranscript = translatedTranscript;
    }
}
