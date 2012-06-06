package fr.ippon.wip.state;

import fr.ippon.wip.http.Response;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Singleton class used to store Response from ACTION to RENDER phase (or servlet)
 *
 * This store could grow infinitely if some responses are never reclaimed (could happens
 * if communication with the browser is interrupted), so a maximum number of entry is forced.
 *
 * To retrieve an entry the Map#remove(UUID uuid) method must be called
 *
 * The oldest entries are evicted first.
 *
 * @author François Prot
 */
public class ResponseStore extends LinkedHashMap<UUID, Response> {
    private static final Logger LOG = Logger.getLogger(ResponseStore.class.getName());

    private static final ResponseStore instance = new ResponseStore();

    // TODO: get max entries from portlet.xml
    // TODO: manage max entries per host
    // TODO: manage expiration dates
    private static final int MAX_ENTRIES = 100;

    private ResponseStore() {
        super();
    }

    public static ResponseStore getInstance() {
        return instance;
    }

    /**
     * Store a Response object and return a corresponding UUID to retrieve it later
     *
     * @param response
     * @return
     */
    public UUID store(Response response) {
        UUID id = UUID.nameUUIDFromBytes(response.getUrl().getBytes());
        if (containsKey(id)) {
            throw new IllegalStateException("An object already exists for this UUID");
        } else {
            put(id, response);
        }
        return id;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<UUID, Response> entry) {
        if (size() > MAX_ENTRIES) {
            entry.getValue().dispose();
            LOG.warning("Response evicted from store, URI was: " + entry.getValue().getUrl());
            return true;
        }
        return false;
    }
}
