package com.ulab.agent.utils;

import java.util.ArrayList;
import java.util.List;

// Debug levels:
//   0 = only chat (user/AI/system speech shown to the operator)
//   1 = chat + warnings + errors
//   2 = everything, including init logs and info-level chatter
public class Console {

    public static final int LEVEL_NONE = 0;
    public static final int LEVEL_ERROR = 1;
    public static final int LEVEL_INFO = 2;

    private List<String> sessionLogs = new ArrayList<>();
    private int debugLevel = LEVEL_INFO;

    public int getDebugLevel() { return debugLevel; }
    public void setDebugLevel(int debugLevel) {
        if (debugLevel < 0) debugLevel = 0;
        if (debugLevel > 2) debugLevel = 2;
        this.debugLevel = debugLevel;
    }

    public void info(String message) {
        if (debugLevel >= LEVEL_INFO) print(TimeUtils.getTimeNow() + " " + message);
    }

    public void warn(String message) {
        if (debugLevel >= LEVEL_ERROR) print(TimeUtils.getTimeNow() + " [WARN]  " + message);
    }

    public void error(String message) {
        if (debugLevel >= LEVEL_ERROR) print(TimeUtils.getTimeNow() + " [ERROR] " + message);
    }

    // Chat lines (user speech, AI reply, system announcements) are always shown.
    public void chat(String message) {
        print(message);
    }

    private void print(String message) {
        sessionLogs.add(message);
        System.out.println(message);
    }

    public List<String> getSessionLogs() {
        return sessionLogs;
    }

    public String getLastMessage() {
        if (sessionLogs.isEmpty()) return null;
        return sessionLogs.get(sessionLogs.size() - 1);
    }
}
