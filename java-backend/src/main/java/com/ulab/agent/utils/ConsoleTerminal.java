package com.ulab.agent.utils;

import com.ulab.agent.Main;
import com.ulab.agent.ai.CallMode;
import com.ulab.agent.managers.AISettingsManager;
import com.ulab.agent.managers.BusinessManager;
import com.ulab.agent.managers.CallManager;
import com.ulab.agent.managers.ConfigManager;
import com.ulab.agent.managers.IntelligenceManager;
import com.ulab.agent.models.AISettings;
import com.ulab.agent.models.Business;
import com.ulab.agent.models.BusinessDetails;
import com.ulab.agent.models.Call;
import com.ulab.agent.models.Client;
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
    private final IntelligenceManager intelligence;

    public ConsoleTerminal(ConfigurableApplicationContext ctx) {
        this.ctx = ctx;
        this.businesses = ctx.getBean(BusinessManager.class);
        this.calls = ctx.getBean(CallManager.class);
        this.aiSettings = ctx.getBean(AISettingsManager.class);
        this.configs = ctx.getBean(ConfigManager.class);
        this.intelligence = ctx.getBean(IntelligenceManager.class);
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
            } else if (cmd.equals("status") || cmd.equals("sts")) {
                showStatus();
            } else if (cmd.equals("businesses") || cmd.equals("biz")) {
                listBusinesses();
            } else if (cmd.equals("add-business") || cmd.equals("+biz")) {
                addBusiness(arg);
            } else if (cmd.equals("use")) {
                useBusiness(arg);
            } else if (cmd.equals("start-call") || cmd.equals("+call")) {
                startCall(arg);
            } else if (cmd.equals("end-call") || cmd.equals("-call")) {
                endCall();
            } else if (cmd.equals("clients")) {
                listClients();
            } else if (cmd.equals("intel")) {
                showIntelligence();
            } else if (cmd.equals("set-mode")) {
                setMode(arg);
            } else if (cmd.equals("ai")) {
                showAiSettings();
            } else if (cmd.equals("config") || cmd.equals("cfg")) {
                showConfig();
            } else if (cmd.equals("refresh") || cmd.equals("ref")) {
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
        System.out.println(Lang.HELP_CLIENTS);
        System.out.println(Lang.HELP_INTEL);
        System.out.println(Lang.HELP_SET_MODE);
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
        System.out.println("  mode       = " + (calls.getActiveMode() == null ? "?" : calls.getActiveMode().getDisplayName()));
        System.out.println("  caller     = " + (calls.getActiveClient() == null ? "unknown (new customer)"
                : calls.getActiveClient().getName() + " (" + calls.getActiveClient().getClientId() + ")"));
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
        intelligence.reload();
        System.out.println(String.format(Lang.BUSINESS_ACTIVATED, arg));
    }

    /**
     * start-call            -> caller is treated as a NEW customer
     * start-call <clientId> -> caller is that client (EXISTING customer mode)
     */
    private void startCall(String clientId) {
        if (businesses.getActiveBusiness() == null) {
            System.out.println(Lang.NO_ACTIVE_BUSINESS);
            return;
        }

        CallMode mode = CallMode.NEW_CUSTOMER;
        Client client = null;
        if (!clientId.isEmpty()) {
            client = intelligence.findClientOfActiveBusiness(clientId);
            if (client == null) {
                System.out.println(String.format(Lang.CLIENT_NOT_FOUND, clientId));
                return;
            }
            mode = CallMode.EXISTING_CUSTOMER;
            System.out.println(String.format(Lang.CALLING_AS_CLIENT, client.getName(), client.getClientId()));
        }

        Call c = calls.startCall(null, "inbound", mode, client);
        if (c != null) {
            System.out.println(String.format(Lang.CALL_ACTIVE_HINT, c.getCallId()));
            System.out.println(Lang.CALL_END_HINT);
        }
    }

    /** Lists the clients of the active business (id, name, account type). */
    private void listClients() {
        Business active = businesses.getActiveBusiness();
        if (active == null) {
            System.out.println(Lang.NO_ACTIVE_BUSINESS);
            return;
        }
        var clientList = intelligence.getClients(active.getBusinessName()).getClients();
        if (clientList.isEmpty()) {
            System.out.println(String.format(Lang.NO_CLIENTS,
                    PathUtils.clientsFile(active.getBusinessName())));
            return;
        }
        System.out.println(String.format(Lang.CLIENTS_HEADER, active.getBusinessName()));
        for (Client c : clientList) {
            System.out.println("  " + c.getClientId() + "  " + c.getName()
                    + (c.getAccountType() == null ? "" : "  (" + c.getAccountType() + ")"));
        }
    }

    /** Prints the exact business-info text the AI receives, for easy checking. */
    private void showIntelligence() {
        Business active = businesses.getActiveBusiness();
        if (active == null) {
            System.out.println(Lang.NO_ACTIVE_BUSINESS);
            return;
        }
        System.out.println(intelligence.buildBusinessInfoText(active.getBusinessName()));
    }

    /** Forces the call mode by hand (mostly useful for testing the scenarios). */
    private void setMode(String arg) {
        CallMode mode = CallMode.fromString(arg);
        if (arg.isEmpty() || mode == null) {
            System.out.println(Lang.USAGE_SET_MODE);
            return;
        }
        if (calls.changeMode(mode, "operator command")) {
            System.out.println(String.format(Lang.MODE_SET, mode.getDisplayName()));
        } else {
            System.out.println(Lang.MODE_SET_FAILED);
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
        intelligence.reload();
        System.out.println(Lang.REFRESH_DONE);
    }

    private void shutdown() {
        System.out.println(Lang.SHUTDOWN_NOTICE);
        if (calls.isCallActive()) calls.endCall();
        ctx.close();
        System.exit(0);
    }
}
