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

package fr.ippon.wip.cache;

import fr.ippon.wip.http.WIPRequest;
import fr.ippon.wip.http.WIPResponse;

/**
 * Interface to manage WIP caching mechanisms. 
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public interface CacheManager {
	
	/**
	 * Get a cache entry according to the possibly given user id and WIPRequest key.
	 * @param id the user id if the cache used is private
	 * @param wipRequest the key to get the response in cache
	 * @return the WIPResponse corresponding to the given id and key
	 */
	public WIPResponse getCacheEntry(String id, WIPRequest wipRequest, String instance);
	
	/**
	 * Add a cache entry according to the possibly given user id and WIPRequest key.
	 * @param id the user id if the response has to be cached privately
	 * @param wipRequest the key that will be used to retrieve the response
	 * @param wipResponse the response to be cached
	 */
	public void setCacheEntry(String id, WIPRequest wipRequest, WIPResponse wipResponse, String instance);

	/**
	 * Remove the cache entry corresponding to the possibly given user id and key.
	 * @param id the user id it the response to remove is private
	 * @param wipRequest the key associated to the response to remove.
	 */
	public void removeCacheEntry(String id, WIPRequest wipRequest, String instance);
	
	/**
	 * Remove all entries in the public cache.
	 */
	public void cleanPublicCache();
	
	/**
	 * Remove all entry in the private cache associated to the given user id.
	 * @param id the user id corresponding to the private cache to remove.
	 */
	public void cleanPrivateCache(String id);
	
	/**
	 * Remove all entries in the private cache.
	 */
	public void cleanEntirePrivateCache();
	
}
