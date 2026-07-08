package com.ulab.agent.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Global runtime config, mirrored to Python at startup via /api/config.
public class Config {

    // --- General ---
    private int debugLevel = 2;
    private String apiKey = "";
    private String aiModel = "gemini-2.5-flash";
    private boolean allowAiResponse = true;

    // --- STT / speech recognition ---
    private String sttLanguage = "en-US";
    private double pauseThreshold = 1.5;
    private double phraseThreshold = 0.3;
    private double nonSpeakingDuration = 0.5;
    private double ambientNoiseAdjustment = 3.0;

    // --- Language switching (spoken by user, matched by Python) ---
    private Map<String, LanguageOption> supportedLanguages = new LinkedHashMap<>();

    public static Config defaults() {
        Config c = new Config();
        c.supportedLanguages.put("bn-BD", new LanguageOption(
                "Bangla",
                List.of("bangla", "bengali", "বাংলা", "বাংলায়"),
                "Please wait a few seconds while I switch to Bangla...",
                "Ready. আপনি এখন বাংলায় কথা বলতে পারেন।"
        ));
        c.supportedLanguages.put("en-US", new LanguageOption(
                "English",
                List.of("english", "ইংরেজি", "ইংরাজি"),
                "Please wait a few seconds while I switch to English...",
                "Ready. You can now speak in English."
        ));
        return c;
    }

    public int getDebugLevel() { return debugLevel; }
    public void setDebugLevel(int debugLevel) { this.debugLevel = debugLevel; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }

    public boolean isAllowAiResponse() { return allowAiResponse; }
    public void setAllowAiResponse(boolean allowAiResponse) { this.allowAiResponse = allowAiResponse; }

    public String getSttLanguage() { return sttLanguage; }
    public void setSttLanguage(String sttLanguage) { this.sttLanguage = sttLanguage; }

    public double getPauseThreshold() { return pauseThreshold; }
    public void setPauseThreshold(double pauseThreshold) { this.pauseThreshold = pauseThreshold; }

    public double getPhraseThreshold() { return phraseThreshold; }
    public void setPhraseThreshold(double phraseThreshold) { this.phraseThreshold = phraseThreshold; }

    public double getNonSpeakingDuration() { return nonSpeakingDuration; }
    public void setNonSpeakingDuration(double nonSpeakingDuration) { this.nonSpeakingDuration = nonSpeakingDuration; }

    public double getAmbientNoiseAdjustment() { return ambientNoiseAdjustment; }
    public void setAmbientNoiseAdjustment(double ambientNoiseAdjustment) { this.ambientNoiseAdjustment = ambientNoiseAdjustment; }

    public Map<String, LanguageOption> getSupportedLanguages() { return supportedLanguages; }
    public void setSupportedLanguages(Map<String, LanguageOption> supportedLanguages) { this.supportedLanguages = supportedLanguages; }

    public static class LanguageOption {
        private String name;
        private List<String> aliases;
        private String pleaseWait;
        private String ready;

        public LanguageOption() {}
        public LanguageOption(String name, List<String> aliases, String pleaseWait, String ready) {
            this.name = name;
            this.aliases = aliases;
            this.pleaseWait = pleaseWait;
            this.ready = ready;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public List<String> getAliases() { return aliases; }
        public void setAliases(List<String> aliases) { this.aliases = aliases; }

        public String getPleaseWait() { return pleaseWait; }
        public void setPleaseWait(String pleaseWait) { this.pleaseWait = pleaseWait; }

        public String getReady() { return ready; }
        public void setReady(String ready) { this.ready = ready; }
    }
}
