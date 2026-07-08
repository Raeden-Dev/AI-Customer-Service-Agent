package com.ulab.agent.utils;

import com.ulab.agent.Main;

import java.nio.file.Path;

public class PathUtils {
    public static String sanitize(String name) {
        if (name == null || name.isBlank()) return "unnamed";
        return name.trim().replaceAll("[\\\\/:*?\"<>|]+", "_");
    }

    public static Path businessDir(String businessName) {
        return Main.BUSINESSES_DIRECTORY.resolve(sanitize(businessName));
    }

    public static Path businessFile(String businessName) {
        return businessDir(businessName).resolve("business.json");
    }

    public static Path aiSettingsFile(String businessName) {
        return businessDir(businessName).resolve("ai-settings.json");
    }

    public static Path callHistoryFile(String businessName) {
        return businessDir(businessName).resolve("call-history.json");
    }

    public static Path transcriptsDir(String businessName) {
        return businessDir(businessName).resolve("transcripts");
    }
}
