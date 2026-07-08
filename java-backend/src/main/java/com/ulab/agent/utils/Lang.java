package com.ulab.agent.utils;

// User-facing strings. Anything longer than a couple of words lives here so it's
// easy to translate or tweak later. Format placeholders use %s / %d — use with String.format.
public class Lang {

    // -------- File & directory ops --------
    public static final String DIR_CREATE_SUCCESS = "Directory created successfully.";
    public static final String DIR_CREATE_FAIL = "Failed to create directory.";
    public static final String FILE_READ_SUCCESS = "File read successfully.";
    public static final String FILE_READ_FAIL = "Failed to read file.";
    public static final String FILE_WRITE_SUCCESS = "File written successfully.";
    public static final String FILE_WRITE_FAIL = "Failed to write to file.";
    public static final String FILE_NOT_FOUND = "File not found.";
    public static final String FILE_LOADED = "Loaded file %s.";

    // -------- Console banner + help --------
    public static final String CONSOLE_BANNER = "=== AI Customer Service Agent v%s ===";
    public static final String COMMANDS_HEADER = "Commands:";
    public static final String HELP_HELP           = "  help                     Show this help";
    public static final String HELP_STATUS         = "  status                   Show active call status";
    public static final String HELP_BUSINESSES     = "  businesses               List registered businesses";
    public static final String HELP_ADD_BUSINESS   = "  add-business <name>      Register a new business";
    public static final String HELP_USE_BUSINESS   = "  use <name>               Set the active business";
    public static final String HELP_START_CALL     = "  start-call               Start a call for the active business";
    public static final String HELP_END_CALL       = "  end-call                 End the active call";
    public static final String HELP_AI             = "  ai                       Show AI settings of the active business";
    public static final String HELP_CONFIG         = "  config                   Show global config";
    public static final String HELP_REFRESH        = "  refresh                  Reload config and data from disk";
    public static final String HELP_EXIT           = "  exit                     Shut down the application";

    // -------- Console command feedback --------
    public static final String NO_ACTIVE_CALL = "No active call.";
    public static final String NO_ACTIVE_BUSINESS = "No business is active. Use 'use <name>' first.";
    public static final String NO_BUSINESSES_REGISTERED = "No businesses registered.";
    public static final String ACTIVE_BUSINESS_LINE = "Active business: %s";
    public static final String USAGE_ADD_BUSINESS = "Usage: add-business <name>";
    public static final String USAGE_USE_BUSINESS = "Usage: use <business name>";
    public static final String BUSINESS_REGISTERED = "Registered business: %s";
    public static final String BUSINESS_ACTIVATED = "Active business set to: %s";
    public static final String BUSINESS_NOT_FOUND = "Business not found: %s";
    public static final String CALL_ACTIVE_HINT = "Call %s active. Speak into your mic.";
    public static final String CALL_END_HINT = "Type 'end-call' when finished.";
    public static final String CALL_ENDED = "Call %s ended.";
    public static final String FINAL_TRANSCRIPT_LINE = "Final transcript: %s";
    public static final String UNKNOWN_COMMAND = "Unknown command: %s (type 'help')";
    public static final String SHUTDOWN_NOTICE = "Shutting down...";
    public static final String REFRESH_STARTED = "Refreshing config and data from disk...";
    public static final String REFRESH_DONE = "Refresh complete.";

    // -------- Manager logs --------
    public static final String BUSINESSES_LOADED = "[BusinessManager] Loaded %d business(es).";
    public static final String BUSINESS_ADDED = "[BusinessManager] Added business %s.";
    public static final String BUSINESS_REMOVED = "[BusinessManager] Removed business %s.";
    public static final String BUSINESS_ACTIVATED_LOG = "[BusinessManager] Active business = %s.";
    public static final String CALL_HISTORY_LOADED = "[CallManager] Loaded %d historical call(s).";
    public static final String CALL_STARTED = "[CallManager] Started call %s.";
    public static final String CALL_ALREADY_ACTIVE = "[CallManager] A call is already active (%s). End it first.";
    public static final String CALL_ENDED_LOG = "[CallManager] Ended call %s.";
    public static final String CALL_NO_ACTIVE = "[CallManager] No active call to end.";
    public static final String CALL_SHUTDOWN_ACTIVE = "[CallManager] Shutting down — ending active call.";
    public static final String TRANSCRIPT_DROPPED = "[CallManager] Dropping transcript — no active call.";
    public static final String TRANSCRIPT_FINAL = "[Transcript / FINAL] %s";
    public static final String STT_STARTED = "[CallManager] STT subprocess started (pid=%d).";
    public static final String STT_STOPPED = "[CallManager] STT subprocess stopped.";
    public static final String STT_EXITED = "[CallManager] STT subprocess exited with code %d.";
    public static final String STT_LAUNCH_FAILED = "[CallManager] Failed to start STT subprocess: %s";
    public static final String STT_DIR_MISSING = "[CallManager] Could not locate python-scripts directory. STT will not start.";
    public static final String TRANSCRIPT_WRITTEN = "[CallManager] Wrote transcript %s.";
    public static final String TRANSCRIPT_WRITE_FAIL = "[CallManager] Failed to write transcript: %s";
    public static final String AI_SETTINGS_READY = "[AISettingsManager] Ready. Model = %s.";
    public static final String CONFIG_LOADED = "[ConfigManager] Loaded config (debugLevel=%d).";
}
