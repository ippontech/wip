/*
 *	Copyright 2010,2011 Ippon Technologies 
 *  
 *	This file is part of Wip Portlet.
 *	Wip Portlet is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Wip Portlet is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Wip Portlet.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ippon.wip.http;

import java.io.IOException;


/**
 * The HttpManager interface defines useful method for http management.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public interface HttpManager {
	
	/**
	 * Do the request only if the response corresponding to the given id has not 
	 * been cached and that response is fresh. A WIPResponse object is returned.
	 * @param id the string session id corresponding to the user id
	 * @param wipRequest the WIPRequest containing the targeted URL
	 * @return the WIPResponse object resulting from the request
	 * @throws IOException
	 */
	public WIPResponse doRequest(String id, WIPRequest request, String instance) throws IOException;

	/**
	 * Propagate the add of cache entry.
	 * @param id the possibly given id (in the case of a private cache)
	 * @param request the key that will be used to retrieve the response
	 * @param response the response to cache
	 */
	public void setCacheEntry(String id, WIPRequest request, WIPResponse response, String instance);
	
	/**
	 * Remove all entries in public and private caches.
	 */
	public void cleanCache();
	
	/**
	 * Remove all entries in the private cache corresponding to the given id.
	 * @param id the user id corresponding to the cache to remove
	 */
	public void cleanPrivateCache(String id);
	
	/**
	 * Destroy the HttpManager.
	 */
	public void destroy();
	
}
