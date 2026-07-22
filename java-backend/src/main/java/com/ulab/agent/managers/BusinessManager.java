package com.ulab.agent.managers;

import com.ulab.agent.Main;
import com.ulab.agent.models.AISettings;
import com.ulab.agent.models.Business;
import com.ulab.agent.models.BusinessDetails;
import com.ulab.agent.models.BusinessIntelligence;
import com.ulab.agent.models.CallHistory;
import com.ulab.agent.models.ClientDatabase;
import com.ulab.agent.utils.FileUtils;
import com.ulab.agent.utils.Lang;
import com.ulab.agent.utils.PathUtils;
import com.ulab.agent.utils.TimeUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class BusinessManager {

    // Keyed by businessName (case-insensitive lookup wrapper below).
    private final Map<String, Business> businesses = new LinkedHashMap<>();
    private Business activeBusiness;

    // Needed so a new business can be pre-filled with the placeholder details
    // the user keeps in config.json.
    private final ConfigManager configManager;

    public BusinessManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @PostConstruct
    public void init() {
        reload();
    }

    public void reload() {
        businesses.clear();
        FileUtils.createDirectory(Main.BUSINESSES_DIRECTORY, false);

        File[] dirs = Main.BUSINESSES_DIRECTORY.toFile().listFiles(File::isDirectory);
        if (dirs != null) {
            for (File dir : dirs) {
                Path businessFile = dir.toPath().resolve("business.json");
                if (!Files.exists(businessFile)) continue;
                Business b = FileUtils.loadJsonFile(dir.getName() + "/business.json",
                        businessFile, Business.class, false);
                if (b != null && b.getBusinessName() != null) {
                    businesses.put(b.getBusinessName().toLowerCase(), b);
                }
            }
        }

        Main.console.info(String.format(Lang.BUSINESSES_LOADED, businesses.size()));

        // If we had an active business, try to re-select the same one after reload.
        if (activeBusiness != null) {
            Business refreshed = getBusiness(activeBusiness.getBusinessName());
            activeBusiness = refreshed;
        }
    }

    public Business getBusiness(String name) {
        if (name == null) return null;
        return businesses.get(name.toLowerCase());
    }

    public List<Business> listBusinesses() {
        return Collections.unmodifiableList(new ArrayList<>(businesses.values()));
    }

    public Business addBusiness(String name) {
        String id = UUID.randomUUID().toString();
        BusinessDetails details = buildPlaceholderDetails(name);
        Business business = new Business(id, name, details, TimeUtils.getTimeNow());
        businesses.put(name.toLowerCase(), business);
        save(business);
        createStarterFiles(name);
        Main.console.info(String.format(Lang.BUSINESS_ADDED, name));
        return business;
    }

    /**
     * Copies the placeholder businessDetails from config.json and puts the real
     * business name on it. We make a fresh copy (via JSON) so every business gets
     * its own object instead of all sharing the one in the config.
     */
    private BusinessDetails buildPlaceholderDetails(String name) {
        BusinessDetails template = configManager.getConfig().getDefaultBusinessDetails();
        if (template == null) template = BusinessDetails.placeholder();
        BusinessDetails copy = Main.GSON.fromJson(Main.GSON.toJson(template), BusinessDetails.class);
        copy.setBusinessName(name);
        return copy;
    }

    /**
     * Writes the starter data files for a brand-new business so they exist right
     * away (instead of only appearing later when something first reads them):
     *   intelligence.json  — knowledge base template
     *   clients.json       — one template client
     *   ai-settings.json   — default AI settings
     *   call-history.json  — empty history
     */
    private void createStarterFiles(String name) {
        FileUtils.createDirectory(PathUtils.businessDir(name), false);

        if (!Files.exists(PathUtils.intelligenceFile(name))) {
            FileUtils.saveJsonFile("intelligence.json", PathUtils.intelligenceFile(name),
                    BusinessIntelligence.template(), false);
        }
        if (!Files.exists(PathUtils.clientsFile(name))) {
            FileUtils.saveJsonFile("clients.json", PathUtils.clientsFile(name),
                    ClientDatabase.template(), false);
        }
        if (!Files.exists(PathUtils.aiSettingsFile(name))) {
            FileUtils.saveJsonFile("ai-settings.json", PathUtils.aiSettingsFile(name),
                    AISettings.defaults(), false);
        }
        if (!Files.exists(PathUtils.callHistoryFile(name))) {
            FileUtils.saveJsonFile("call-history.json", PathUtils.callHistoryFile(name),
                    new CallHistory(), false);
        }
    }

    public boolean removeBusiness(String name) {
        Business removed = businesses.remove(name == null ? null : name.toLowerCase());
        if (removed == null) return false;
        Main.console.info(String.format(Lang.BUSINESS_REMOVED, name));
        return true;
    }

    public void save(Business business) {
        Path dir = PathUtils.businessDir(business.getBusinessName());
        FileUtils.createDirectory(dir, false);
        FileUtils.saveJsonFile("business.json", PathUtils.businessFile(business.getBusinessName()), business, false);
    }

    public Business getActiveBusiness() { return activeBusiness; }

    public boolean setActiveBusiness(String name) {
        Business b = getBusiness(name);
        if (b == null) return false;
        activeBusiness = b;
        Main.console.info(String.format(Lang.BUSINESS_ACTIVATED_LOG, b.getBusinessName()));
        return true;
    }
}
