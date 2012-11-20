/*
 *	Copyright 2010,2011 Ippon Technologies 
 *  
 *	This file is part of Web Integration Portlet (WIP).
 *	Web Integration Portlet (WIP) is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Web Integration Portlet (WIP) is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Web Integration Portlet (WIP).  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ippon.wip.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import fr.ippon.wip.http.reponse.Response;

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

    public static ResponseStore getInstance() {
        return instance;
    }

    // TODO: manage max entries per host
    // TODO: manage expiration dates
    private int maxEntries = 100;

	private ResponseStore() {
        super();
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<UUID, Response> entry) {
        if (size() > maxEntries) {
            LOG.warning("Response evicted from store, URI was: " + entry.getValue().getUrl());
            return true;
        }
        return false;
    }

    public void setMaxEntries(int maxEntries) {
		this.maxEntries = maxEntries;
	}

    /**
     * Store a Response object and return a corresponding UUID to retrieve it later
     *
     * @param response
     * @return
     */
    public UUID store(Response response) {
        UUID id = UUID.nameUUIDFromBytes(response.getUrl().getBytes());
        //if (containsKey(id)) {
        //    throw new IllegalStateException("An object already exists for this UUID");
        //} else {
            put(id, response);
        //}
        return id;
    }
}
