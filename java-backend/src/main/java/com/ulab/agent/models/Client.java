package com.ulab.agent.models;

import java.util.ArrayList;
import java.util.List;

/**
 * One client (customer) of a business.
 *
 * Client records live in data/businesses/<name>/clients.json.
 * The clientId is the short id you type in the console:  start-call C001
 * Everything in this record is given to the AI as context when that
 * client calls, so the AI can greet them by name and know their history.
 */
public class Client {

    private String clientId;              // short id, e.g. "C001"
    private String name;
    private String phoneNumber;
    private String email;
    private String accountType;           // e.g. "regular", "premium"
    private String joinDate;
    private String notes;                 // free text the AI should know about this client
    private List<String> pastIssues = new ArrayList<>();  // short summaries of earlier calls/problems

    public Client() {}

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<String> getPastIssues() {
        if (pastIssues == null) pastIssues = new ArrayList<>();
        return pastIssues;
    }
    public void setPastIssues(List<String> pastIssues) { this.pastIssues = pastIssues; }
}
