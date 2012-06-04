package fr.ippon.wip.state;

import fr.ippon.wip.http.Response;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 01/06/12
 * Time: 10:21
 * To change this template use File | Settings | File Templates.
 */
public class ResponseStore extends LinkedHashMap<UUID,Response> {
    private static final Logger LOG = Logger.getLogger(ResponseStore.class.getName());

    private static final ResponseStore instance = new ResponseStore ();

    // TODO: get max entries from portlet.xml
    // TODO: manage max entries per host
    // TODO: manage expiration dates
    private static final int MAX_ENTRIES = 100;

    private ResponseStore () {
        super ();
    }

    public static ResponseStore getInstance () {
        return instance;
    }

    public UUID store (Response response) {
        UUID id = UUID.nameUUIDFromBytes(response.getUri().getBytes());
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
            LOG.warning("Response evicted from store, URI was: " + entry.getValue().getUri());
            return true;
        }
        return false;
    }
}
