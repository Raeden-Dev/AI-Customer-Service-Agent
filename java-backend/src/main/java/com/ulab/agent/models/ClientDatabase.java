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
