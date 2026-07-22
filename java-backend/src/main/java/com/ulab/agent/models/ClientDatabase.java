package com.ulab.agent.models;

import java.util.ArrayList;
import java.util.List;

/**
 * The list of clients of one business.
 * Stored as data/businesses/<name>/clients.json.
 *
 * Kept as a wrapper class (instead of a bare list) so the JSON file reads as
 * {"clients": [...]} and we can add more fields later without breaking files.
 */
public class ClientDatabase {

    private List<Client> clients = new ArrayList<>();

    /**
     * A starter database with one example client, so a new business gets a
     * clients.json it can just open and edit instead of a blank file.
     */
    public static ClientDatabase template() {
        ClientDatabase db = new ClientDatabase();
        Client c = new Client();
        c.setClientId("C001");
        c.setName("Example Client");
        c.setPhoneNumber("+8800000000000");
        c.setEmail("client@example.com");
        c.setAccountType("regular");
        c.setJoinDate("01/01/2026");
        c.setNotes("This is a template client. Edit clients.json to add real clients.");
        c.getPastIssues().add("Example past issue the AI should know about.");
        db.getClients().add(c);
        return db;
    }

    public List<Client> getClients() {
        if (clients == null) clients = new ArrayList<>();
        return clients;
    }

    public void setClients(List<Client> clients) { this.clients = clients; }

    /** Finds a client by id, ignoring upper/lower case. Returns null if not found. */
    public Client findClient(String clientId) {
        if (clientId == null) return null;
        for (Client c : getClients()) {
            if (clientId.equalsIgnoreCase(c.getClientId())) return c;
        }
        return null;
    }
}
