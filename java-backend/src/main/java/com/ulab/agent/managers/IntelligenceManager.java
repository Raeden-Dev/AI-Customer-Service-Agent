package com.ulab.agent.managers;

import com.ulab.agent.Main;
import com.ulab.agent.models.Business;
import com.ulab.agent.models.BusinessIntelligence;
import com.ulab.agent.models.Client;
import com.ulab.agent.models.ClientDatabase;
import com.ulab.agent.utils.FileUtils;
import com.ulab.agent.utils.Lang;
import com.ulab.agent.utils.PathUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and serves the "brain files" of each business:
 *
 *   intelligence.json -> what the business does, services, policies, FAQs
 *   clients.json      -> the clients of the business and their history
 *
 * The AI never reads these files directly. Instead this manager turns them
 * into plain-text blocks (buildBusinessInfoText / buildClientInfoText) that
 * get pasted into the AI prompt through GET /api/call-context.
 *
 * If a business has no files yet, starter templates are created so the owner
 * can just open them and fill in the blanks.
 */
@Component
public class IntelligenceManager {

    private final BusinessManager businessManager;

    // Caches so we do not re-read the files on every request. Key = business name (lower case).
    private final Map<String, BusinessIntelligence> intelligenceCache = new HashMap<>();
    private final Map<String, ClientDatabase> clientsCache = new HashMap<>();

    public IntelligenceManager(BusinessManager businessManager) {
        this.businessManager = businessManager;
    }

    /** Clears the caches so the next access re-reads the JSON files from disk. */
    public void reload() {
        intelligenceCache.clear();
        clientsCache.clear();
        Business active = businessManager.getActiveBusiness();
        if (active != null) {
            ClientDatabase db = getClients(active.getBusinessName());
            Main.console.info(String.format(Lang.INTELLIGENCE_LOADED, db.getClients().size()));
        }
    }

    // ---- business intelligence ----

    /** Returns the knowledge base of a business, creating a template file if missing. */
    public BusinessIntelligence getIntelligence(String businessName) {
        BusinessIntelligence cached = intelligenceCache.get(businessName.toLowerCase());
        if (cached != null) return cached;

        FileUtils.createDirectory(PathUtils.businessDir(businessName), false);
        BusinessIntelligence loaded = FileUtils.loadJsonFile("intelligence.json",
                PathUtils.intelligenceFile(businessName), BusinessIntelligence.class, false);
        if (loaded == null) {
            loaded = BusinessIntelligence.template();
            FileUtils.saveJsonFile("intelligence.json", PathUtils.intelligenceFile(businessName), loaded, false);
        }
        intelligenceCache.put(businessName.toLowerCase(), loaded);
        return loaded;
    }

    // ---- clients ----

    /** Returns the client database of a business, creating an empty file if missing. */
    public ClientDatabase getClients(String businessName) {
        ClientDatabase cached = clientsCache.get(businessName.toLowerCase());
        if (cached != null) return cached;

        FileUtils.createDirectory(PathUtils.businessDir(businessName), false);
        ClientDatabase loaded = FileUtils.loadJsonFile("clients.json",
                PathUtils.clientsFile(businessName), ClientDatabase.class, false);
        if (loaded == null) {
            loaded = new ClientDatabase();
            FileUtils.saveJsonFile("clients.json", PathUtils.clientsFile(businessName), loaded, false);
        }
        clientsCache.put(businessName.toLowerCase(), loaded);
        return loaded;
    }

    /** Finds one client of the active business by id. Returns null if there is no match. */
    public Client findClientOfActiveBusiness(String clientId) {
        Business active = businessManager.getActiveBusiness();
        if (active == null) return null;
        return getClients(active.getBusinessName()).findClient(clientId);
    }

    // ---- text blocks for the AI prompt ----

    /**
     * Turns intelligence.json into one readable text block, for example:
     *
     *   Business name: test1
     *   About: A small salon in Dhaka...
     *   Services:
     *   - Haircut - 500 BDT
     *   Policies:
     *   - Refunds within 7 days
     *   Common questions:
     *   Q: Do you take walk-ins?  A: Yes, before 6pm.
     */
    public String buildBusinessInfoText(String businessName) {
        BusinessIntelligence bi = getIntelligence(businessName);
        StringBuilder sb = new StringBuilder();
        sb.append("Business name: ").append(businessName).append('\n');

        if (!bi.getAbout().isBlank()) {
            sb.append("About: ").append(bi.getAbout()).append('\n');
        }
        appendList(sb, "Services:", bi.getServices());
        appendList(sb, "Policies:", bi.getPolicies());

        if (!bi.getFaqs().isEmpty()) {
            sb.append("Common questions:\n");
            for (BusinessIntelligence.FaqEntry faq : bi.getFaqs()) {
                sb.append("Q: ").append(faq.getQuestion())
                  .append("  A: ").append(faq.getAnswer()).append('\n');
            }
        }
        return sb.toString().trim();
    }

    /** Turns one client record into a readable text block for the AI prompt. */
    public String buildClientInfoText(Client client) {
        if (client == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("Client id: ").append(nullSafe(client.getClientId())).append('\n');
        sb.append("Name: ").append(nullSafe(client.getName())).append('\n');
        appendIfPresent(sb, "Phone: ", client.getPhoneNumber());
        appendIfPresent(sb, "Email: ", client.getEmail());
        appendIfPresent(sb, "Account type: ", client.getAccountType());
        appendIfPresent(sb, "Client since: ", client.getJoinDate());
        appendIfPresent(sb, "Notes: ", client.getNotes());
        appendList(sb, "Past issues:", client.getPastIssues());
        return sb.toString().trim();
    }

    // ---- small helpers ----

    private static void appendList(StringBuilder sb, String title, List<String> items) {
        if (items == null || items.isEmpty()) return;
        sb.append(title).append('\n');
        for (String item : items) {
            sb.append("- ").append(item).append('\n');
        }
    }

    private static void appendIfPresent(StringBuilder sb, String label, String value) {
        if (value == null || value.isBlank()) return;
        sb.append(label).append(value).append('\n');
    }

    private static String nullSafe(String s) { return s == null ? "" : s; }
}
