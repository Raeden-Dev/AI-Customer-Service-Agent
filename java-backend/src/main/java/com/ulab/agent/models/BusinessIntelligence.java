package com.ulab.agent.models;

import java.util.ArrayList;
import java.util.List;

/**
 * The knowledge base of one business — everything the AI is allowed to tell
 * customers about the business.
 *
 * Stored as data/businesses/<name>/intelligence.json. Edit that file to teach
 * the AI about the business; it is re-read every time a call starts.
 */
public class BusinessIntelligence {

    private String about = "";                          // one paragraph: what this business does
    private List<String> services = new ArrayList<>();  // services / products, e.g. "Haircut - 500 BDT"
    private List<String> policies = new ArrayList<>();  // rules, e.g. "Refunds within 7 days with receipt"
    private List<FaqEntry> faqs = new ArrayList<>();    // common question/answer pairs

    /** A starter template so a new business gets a file the owner can fill in. */
    public static BusinessIntelligence template() {
        BusinessIntelligence bi = new BusinessIntelligence();
        bi.about = "Describe the business here (what it does, where it is, opening hours).";
        bi.services.add("Example service - example price");
        bi.policies.add("Example policy, e.g. refunds within 7 days with receipt.");
        FaqEntry faq = new FaqEntry();
        faq.setQuestion("Example question customers often ask?");
        faq.setAnswer("The answer the AI should give.");
        bi.faqs.add(faq);
        return bi;
    }

    public String getAbout() { return about == null ? "" : about; }
    public void setAbout(String about) { this.about = about; }

    public List<String> getServices() {
        if (services == null) services = new ArrayList<>();
        return services;
    }
    public void setServices(List<String> services) { this.services = services; }

    public List<String> getPolicies() {
        if (policies == null) policies = new ArrayList<>();
        return policies;
    }
    public void setPolicies(List<String> policies) { this.policies = policies; }

    public List<FaqEntry> getFaqs() {
        if (faqs == null) faqs = new ArrayList<>();
        return faqs;
    }
    public void setFaqs(List<FaqEntry> faqs) { this.faqs = faqs; }

    /** One frequently-asked question and the answer the AI should give. */
    public static class FaqEntry {
        private String question;
        private String answer;

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }
}
