package com.ulab.agent.managers;

import com.ulab.agent.Main;
import com.ulab.agent.models.AISettings;
import com.ulab.agent.models.Business;
import com.ulab.agent.utils.FileUtils;
import com.ulab.agent.utils.Lang;
import com.ulab.agent.utils.PathUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class AISettingsManager {

    private final BusinessManager businessManager;
    // Cache: businessName -> settings, so re-reads are cheap and we can hot-reload.
    private final Map<String, AISettings> cache = new HashMap<>();

    public AISettingsManager(BusinessManager businessManager) {
        this.businessManager = businessManager;
    }

    public void reload() {
        cache.clear();
        Business active = businessManager.getActiveBusiness();
        if (active != null) {
            AISettings s = loadOrCreate(active.getBusinessName());
            Main.console.info(String.format(Lang.AI_SETTINGS_READY, s.getModel()));
        }
    }

    public AISettings getActiveSettings() {
        Business active = businessManager.getActiveBusiness();
        if (active == null) return AISettings.defaults();
        return getSettings(active.getBusinessName());
    }

    public AISettings getSettings(String businessName) {
        AISettings cached = cache.get(businessName.toLowerCase());
        if (cached != null) return cached;
        return loadOrCreate(businessName);
    }

    public void updateActiveSettings(AISettings settings) {
        Business active = businessManager.getActiveBusiness();
        if (active == null) return;
        cache.put(active.getBusinessName().toLowerCase(), settings);
        FileUtils.saveJsonFile("ai-settings.json",
                PathUtils.aiSettingsFile(active.getBusinessName()), settings, false);
    }

    private AISettings loadOrCreate(String businessName) {
        Path file = PathUtils.aiSettingsFile(businessName);
        FileUtils.createDirectory(PathUtils.businessDir(businessName), false);
        AISettings loaded = FileUtils.loadJsonFile("ai-settings.json", file, AISettings.class, false);
        if (loaded == null) {
            loaded = AISettings.defaults();
            FileUtils.saveJsonFile("ai-settings.json", file, loaded, false);
        }
        cache.put(businessName.toLowerCase(), loaded);
        return loaded;
    }
}
