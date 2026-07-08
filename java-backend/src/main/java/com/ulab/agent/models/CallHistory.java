package com.ulab.agent.models;

import java.util.ArrayList;
import java.util.List;

public class CallHistory {
    private int totalCalls;
    private String lastCallDate;
    private List<Call> callList = new ArrayList<>();

    public int getTotalCalls() { return totalCalls; }
    public void setTotalCalls(int totalCalls) { this.totalCalls = totalCalls; }

    public String getLastCallDate() { return lastCallDate; }
    public void setLastCallDate(String lastCallDate) { this.lastCallDate = lastCallDate; }

    public List<Call> getCallList() {
        if (callList == null) callList = new ArrayList<>();
        return callList;
    }
    public void setCallList(List<Call> callList) { this.callList = callList; }

    public void addCall(Call call) {
        getCallList().add(call);
        totalCalls = getCallList().size();
        lastCallDate = call.getStartTime();
    }
}
