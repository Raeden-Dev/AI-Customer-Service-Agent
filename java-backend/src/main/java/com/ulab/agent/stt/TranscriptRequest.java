package com.ulab.agent.stt;

public class TranscriptRequest {
    private String transcript;
    private boolean isFinal;

    public String getTranscript() { return transcript;}
    public void setTranscript(String transcript) { this.transcript = transcript; }

    public boolean isFinal() { return isFinal; }
    public void setFinal(boolean isFinal) { this.isFinal = isFinal; }
}
