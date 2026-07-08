package com.ulab.agent.managers;

import com.ulab.agent.Main;
import com.ulab.agent.models.Business;
import com.ulab.agent.models.Call;
import com.ulab.agent.models.CallHistory;
import com.ulab.agent.models.ChatMessage;
import com.ulab.agent.utils.FileUtils;
import com.ulab.agent.utils.Lang;
import com.ulab.agent.utils.PathUtils;
import com.ulab.agent.utils.TimeUtils;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class CallManager {

    private final BusinessManager businessManager;
    // businessName -> its call history (loaded on demand).
    private final Map<String, CallHistory> historyCache = new HashMap<>();

    private Call activeCall;
    private Business callBusiness;      // Which business owns activeCall (for saving to the right dir).
    private Process sttProcess;

    public CallManager(BusinessManager businessManager) {
        this.businessManager = businessManager;
    }

    public void reload() {
        historyCache.clear();
        Business active = businessManager.getActiveBusiness();
        if (active != null) {
            CallHistory history = loadHistory(active.getBusinessName());
            Main.console.info(String.format(Lang.CALL_HISTORY_LOADED, history.getTotalCalls()));
        }
    }

    @PreDestroy
    public void shutdown() {
        if (isCallActive()) {
            Main.console.info(Lang.CALL_SHUTDOWN_ACTIVE);
            endCall();
        }
    }

    public synchronized boolean isCallActive() { return activeCall != null; }

    public synchronized Call getActiveCall() { return activeCall; }

    public synchronized CallHistory getActiveHistory() {
        Business active = businessManager.getActiveBusiness();
        if (active == null) return new CallHistory();
        return loadHistory(active.getBusinessName());
    }

    public synchronized Call startCall(String agentId, String customerId, String callType) {
        if (activeCall != null) {
            Main.console.warn(String.format(Lang.CALL_ALREADY_ACTIVE, activeCall.getCallId()));
            return activeCall;
        }
        Business active = businessManager.getActiveBusiness();
        if (active == null) {
            Main.console.warn(Lang.NO_ACTIVE_BUSINESS);
            return null;
        }
        callBusiness = active;

        Call call = new Call();
        call.setAgentId(agentId);
        call.setCustomerId(customerId);
        call.setCallType(callType);
        call.setStartTime(TimeUtils.getTimeNow());
        activeCall = call;

        Main.console.info(String.format(Lang.CALL_STARTED, call.getCallId()));
        launchSttProcess();
        return call;
    }

    public synchronized Call endCall() {
        if (activeCall == null) {
            Main.console.warn(Lang.CALL_NO_ACTIVE);
            return null;
        }

        stopSttProcess();

        activeCall.setEndTime(TimeUtils.getTimeNow());

        String businessName = callBusiness != null ? callBusiness.getBusinessName() : "unassigned";
        CallHistory history = loadHistory(businessName);
        history.addCall(activeCall);
        FileUtils.saveJsonFile("call-history.json",
                PathUtils.callHistoryFile(businessName), history, false);
        saveCallTranscript(activeCall, businessName);

        Call finished = activeCall;
        activeCall = null;
        callBusiness = null;
        Main.console.info(String.format(Lang.CALL_ENDED_LOG, finished.getCallId()));
        return finished;
    }

    public synchronized void handleTranscript(String transcript, String translated, boolean isFinal) {
        if (activeCall == null) {
            Main.console.warn(Lang.TRANSCRIPT_DROPPED);
            return;
        }
        if (isFinal) {
            if (transcript != null && !transcript.isBlank()) activeCall.setTranscript(transcript);
            if (translated != null) activeCall.setTranslated(translated);
            Main.console.info(String.format(Lang.TRANSCRIPT_FINAL, transcript));
        }
    }

    public synchronized void handleChatMessage(ChatMessage.Role role, String content) {
        if (content == null || content.isBlank()) return;
        String timestamp = TimeUtils.getTimeNow();
        if (activeCall != null) {
            activeCall.addMessage(new ChatMessage(role, content, timestamp));
            if (role == ChatMessage.Role.USER) {
                activeCall.appendTranscript(content);
            } else if (role == ChatMessage.Role.AI) {
                activeCall.appendAiTranscript(content);
            }
        }
        String label = switch (role) {
            case USER -> "[User]";
            case AI -> "[AI]  ";
            case SYSTEM -> "[Sys] ";
        };
        Main.console.chat(label + " " + content);
    }

    // ---- helpers ----

    private CallHistory loadHistory(String businessName) {
        CallHistory cached = historyCache.get(businessName.toLowerCase());
        if (cached != null) return cached;

        Path file = PathUtils.callHistoryFile(businessName);
        FileUtils.createDirectory(PathUtils.businessDir(businessName), false);
        CallHistory loaded = FileUtils.loadJsonFile("call-history.json", file, CallHistory.class, false);
        if (loaded == null) loaded = new CallHistory();
        historyCache.put(businessName.toLowerCase(), loaded);
        return loaded;
    }

    private void saveCallTranscript(Call call, String businessName) {
        Path dir = PathUtils.transcriptsDir(businessName);
        FileUtils.createDirectory(dir, false);
        Path file = dir.resolve(call.getCallId() + ".txt");

        StringBuilder sb = new StringBuilder();
        sb.append("Call ID:  ").append(call.getCallId()).append('\n');
        sb.append("Business: ").append(businessName).append('\n');
        sb.append("Started:  ").append(nullSafe(call.getStartTime())).append('\n');
        sb.append("Ended:    ").append(nullSafe(call.getEndTime())).append('\n');
        sb.append("Agent:    ").append(nullSafe(call.getAgentId())).append('\n');
        sb.append("Customer: ").append(nullSafe(call.getCustomerId())).append('\n');
        sb.append("Type:     ").append(nullSafe(call.getCallType())).append('\n');

        sb.append('\n').append("--- CONVERSATION ---\n");
        for (ChatMessage m : call.getMessages()) {
            String tag = switch (m.getRole()) {
                case USER -> "USER  ";
                case AI -> "AI    ";
                case SYSTEM -> "SYSTEM";
            };
            sb.append('[').append(nullSafe(m.getTimestamp())).append("] ")
              .append(tag).append(": ").append(nullSafe(m.getContent())).append('\n');
        }

        sb.append('\n').append("--- USER TRANSCRIPT ---\n").append(nullSafe(call.getTranscript())).append('\n');
        sb.append('\n').append("--- AI TRANSCRIPT ---\n").append(nullSafe(call.getAiTranscript())).append('\n');

        try {
            Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
            Main.console.info(String.format(Lang.TRANSCRIPT_WRITTEN, file.toAbsolutePath()));
        } catch (IOException e) {
            FileUtils.logError("saveCallTranscript", e);
            Main.console.error(String.format(Lang.TRANSCRIPT_WRITE_FAIL, e.getMessage()));
        }
    }

    // ---- Python STT subprocess ----

    private void launchSttProcess() {
        Path scriptsDir = resolvePythonScriptsDir();
        if (scriptsDir == null) {
            Main.console.warn(Lang.STT_DIR_MISSING);
            return;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(pythonExecutable(), "-u", "stt_sender.py");
            pb.directory(scriptsDir.toFile());
            pb.environment().put("PYTHONUNBUFFERED", "1");
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
            sttProcess = pb.start();
            Main.console.info(String.format(Lang.STT_STARTED, sttProcess.pid()));
            pipeSttOutput(sttProcess);
        } catch (IOException e) {
            FileUtils.logError("launchSttProcess", e);
            Main.console.error(String.format(Lang.STT_LAUNCH_FAILED, e.getMessage()));
        }
    }

    private void pipeSttOutput(Process process) {
        Thread t = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[python] " + line);
                }
            } catch (IOException e) {
                Main.console.warn("[python] stream closed: " + e.getMessage());
            }
            int code = -1;
            try {
                code = process.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Main.console.info(String.format(Lang.STT_EXITED, code));
        }, "stt-output-reader");
        t.setDaemon(true);
        t.start();
    }

    private void stopSttProcess() {
        if (sttProcess == null) return;
        try {
            sttProcess.destroy();
            if (!sttProcess.waitFor(3, TimeUnit.SECONDS)) {
                sttProcess.destroyForcibly();
            }
            Main.console.info(Lang.STT_STOPPED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            sttProcess = null;
        }
    }

    private Path resolvePythonScriptsDir() {
        Path[] candidates = new Path[]{
                Paths.get("python-scripts"),
                Paths.get("..", "python-scripts"),
                Paths.get("..", "..", "python-scripts")
        };
        for (Path p : candidates) {
            if (Files.isDirectory(p) && Files.exists(p.resolve("stt_sender.py"))) {
                return p.toAbsolutePath().normalize();
            }
        }
        return null;
    }

    private String pythonExecutable() {
        String envOverride = System.getenv("PYTHON_EXECUTABLE");
        if (envOverride != null && !envOverride.isBlank()) return envOverride;

        Path scriptsDir = resolvePythonScriptsDir();
        if (scriptsDir != null) {
            boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
            Path[] venvCandidates = new Path[]{
                    scriptsDir.resolve(windows ? ".venv/Scripts/python.exe" : ".venv/bin/python"),
                    scriptsDir.resolve(windows ? "venv/Scripts/python.exe" : "venv/bin/python"),
                    scriptsDir.getParent() == null ? null :
                            scriptsDir.getParent().resolve(windows ? ".venv/Scripts/python.exe" : ".venv/bin/python")
            };
            for (Path p : venvCandidates) {
                if (p != null && Files.isExecutable(p)) return p.toAbsolutePath().toString();
            }
        }
        return System.getProperty("os.name").toLowerCase().contains("win") ? "python" : "python3";
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}
