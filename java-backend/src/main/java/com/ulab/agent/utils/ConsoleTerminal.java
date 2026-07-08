package com.ulab.agent.utils;

import com.ulab.agent.Main;
import com.ulab.agent.managers.AISettingsManager;
import com.ulab.agent.managers.BusinessManager;
import com.ulab.agent.managers.CallManager;
import com.ulab.agent.managers.ConfigManager;
import com.ulab.agent.models.AISettings;
import com.ulab.agent.models.Business;
import com.ulab.agent.models.BusinessDetails;
import com.ulab.agent.models.Call;
import com.ulab.agent.models.Config;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;

// Interactive terminal for the operator.
// Boot Spring first, then hand the context here so we can pull the managers out.
public class ConsoleTerminal {

    private final ConfigurableApplicationContext ctx;
    private final BusinessManager businesses;
    private final CallManager calls;
    private final AISettingsManager aiSettings;
    private final ConfigManager configs;

    public ConsoleTerminal(ConfigurableApplicationContext ctx) {
        this.ctx = ctx;
        this.businesses = ctx.getBean(BusinessManager.class);
        this.calls = ctx.getBean(CallManager.class);
        this.aiSettings = ctx.getBean(AISettingsManager.class);
        this.configs = ctx.getBean(ConfigManager.class);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println();
        System.out.println(String.format(Lang.CONSOLE_BANNER, Main.VERSION));
        printHelp();

        while (true) {
            System.out.print("agent> ");
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String arg = parts.length > 1 ? parts[1] : "";

            if (cmd.equals("help")) {
                printHelp();
            } else if (cmd.equals("status")) {
                showStatus();
            } else if (cmd.equals("businesses")) {
                listBusinesses();
            } else if (cmd.equals("add-business")) {
                addBusiness(arg);
            } else if (cmd.equals("use")) {
                useBusiness(arg);
            } else if (cmd.equals("start-call")) {
                startCall();
            } else if (cmd.equals("end-call")) {
                endCall();
            } else if (cmd.equals("ai")) {
                showAiSettings();
            } else if (cmd.equals("config")) {
                showConfig();
            } else if (cmd.equals("refresh")) {
                refresh();
            } else if (cmd.equals("exit") || cmd.equals("quit")) {
                shutdown();
                return;
            } else {
                System.out.println(String.format(Lang.UNKNOWN_COMMAND, cmd));
            }
        }
    }

    private void printHelp() {
        System.out.println(Lang.COMMANDS_HEADER);
        System.out.println(Lang.HELP_HELP);
        System.out.println(Lang.HELP_STATUS);
        System.out.println(Lang.HELP_BUSINESSES);
        System.out.println(Lang.HELP_ADD_BUSINESS);
        System.out.println(Lang.HELP_USE_BUSINESS);
        System.out.println(Lang.HELP_START_CALL);
        System.out.println(Lang.HELP_END_CALL);
        System.out.println(Lang.HELP_AI);
        System.out.println(Lang.HELP_CONFIG);
        System.out.println(Lang.HELP_REFRESH);
        System.out.println(Lang.HELP_EXIT);
    }

    private void showStatus() {
        Business active = businesses.getActiveBusiness();
        System.out.println(String.format(Lang.ACTIVE_BUSINESS_LINE,
                active == null ? "(none)" : active.getBusinessName()));
        if (!calls.isCallActive()) {
            System.out.println(Lang.NO_ACTIVE_CALL);
            return;
        }
        Call c = calls.getActiveCall();
        System.out.println("Active call: " + c.getCallId());
        System.out.println("  transcript = " + (c.getTranscript() == null ? "" : c.getTranscript()));
    }

    private void listBusinesses() {
        if (businesses.listBusinesses().isEmpty()) {
            System.out.println(Lang.NO_BUSINESSES_REGISTERED);
            return;
        }
        Business active = businesses.getActiveBusiness();
        for (Business b : businesses.listBusinesses()) {
            String marker = (active != null && active.getBusinessName().equalsIgnoreCase(b.getBusinessName())) ? "* " : "  ";
            System.out.println(marker + b.getBusinessName() + "  (" + b.getBusinessId() + ")");
        }
    }

    private void addBusiness(String arg) {
        if (arg.isEmpty()) {
            System.out.println(Lang.USAGE_ADD_BUSINESS);
            return;
        }
        BusinessDetails details = new BusinessDetails();
        details.setBusinessName(arg);
        Business b = businesses.addBusiness(arg, details);
        System.out.println(String.format(Lang.BUSINESS_REGISTERED, b.getBusinessName()));
    }

    private void useBusiness(String arg) {
        if (arg.isEmpty()) {
            System.out.println(Lang.USAGE_USE_BUSINESS);
            return;
        }
        if (!businesses.setActiveBusiness(arg)) {
            System.out.println(String.format(Lang.BUSINESS_NOT_FOUND, arg));
            return;
        }
        // Load per-business data lazily so 'ai' / 'status' see the fresh state.
        aiSettings.reload();
        calls.reload();
        System.out.println(String.format(Lang.BUSINESS_ACTIVATED, arg));
    }

    private void startCall() {
        if (businesses.getActiveBusiness() == null) {
            System.out.println(Lang.NO_ACTIVE_BUSINESS);
            return;
        }
        Call c = calls.startCall(null, "unknown", "inbound");
        if (c != null) {
            System.out.println(String.format(Lang.CALL_ACTIVE_HINT, c.getCallId()));
            System.out.println(Lang.CALL_END_HINT);
        }
    }

    private void endCall() {
        Call finished = calls.endCall();
        if (finished != null) {
            System.out.println(String.format(Lang.CALL_ENDED, finished.getCallId()));
            System.out.println(String.format(Lang.FINAL_TRANSCRIPT_LINE,
                    finished.getTranscript() == null ? "" : finished.getTranscript()));
        }
    }

    private void showAiSettings() {
        if (businesses.getActiveBusiness() == null) {
            System.out.println(Lang.NO_ACTIVE_BUSINESS);
            return;
        }
        AISettings s = aiSettings.getActiveSettings();
        System.out.println("model             = " + s.getModel());
        System.out.println("roleInstructions  = " + s.getRoleInstructions());
        System.out.println("replyInstructions = " + s.getReplyInstructions());
        System.out.println("organizationInfo  = " + s.getOrganizationInfo());
    }

    private void showConfig() {
        Config c = configs.getConfig();
        System.out.println("debugLevel            = " + c.getDebugLevel());
        System.out.println("aiModel               = " + c.getAiModel());
        System.out.println("allowAiResponse       = " + c.isAllowAiResponse());
        System.out.println("sttLanguage           = " + c.getSttLanguage());
        System.out.println("pauseThreshold        = " + c.getPauseThreshold());
        System.out.println("phraseThreshold       = " + c.getPhraseThreshold());
        System.out.println("nonSpeakingDuration   = " + c.getNonSpeakingDuration());
        System.out.println("ambientNoiseAdjustment= " + c.getAmbientNoiseAdjustment());
        System.out.println("supportedLanguages    = " + c.getSupportedLanguages().keySet());
    }

    private void refresh() {
        System.out.println(Lang.REFRESH_STARTED);
        configs.reload();
        businesses.reload();
        aiSettings.reload();
        calls.reload();
        System.out.println(Lang.REFRESH_DONE);
    }

    private void shutdown() {
        System.out.println(Lang.SHUTDOWN_NOTICE);
        if (calls.isCallActive()) calls.endCall();
        ctx.close();
        System.exit(0);
    }
}
