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

package fr.ippon.wip.cache;

import java.util.HashMap;
import java.util.Map;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.http.WIPRequest;
import fr.ippon.wip.http.WIPResponse;

/**
 * Implementation of the CacheManager interface. It defines a public cache and a map 
 * of private caches indexed by the coresponding user id. This class is a singleton.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class CacheManagerImpl implements CacheManager {
	
	/**
	 * CacheManager singleton
	 */
	private static CacheManager instance = null;
	
	/**
	 * The public cache.
	 */
	private Map<WIPRequest, WIPResponse> publicCache;
	
	/**
	 * A map indexed by user id and containing the different user private cache.
	 */
	private Map<String, Map<WIPRequest, WIPResponse>> privateCache;
	
	/**
	 * Get the unique instance of the class.
	 * @return the cache manager singleton
	 */
	public static synchronized CacheManager getInstance() {
		if (instance == null)
			instance = new CacheManagerImpl();
		return instance;
	}
	
	/**
	 * The private constructor to instantiate the singleton.
	 */
	private CacheManagerImpl() {
		privateCache = new HashMap<String, Map<WIPRequest, WIPResponse>>();
		publicCache = new HashMap<WIPRequest, WIPResponse>();
	}
	
	public void setCacheEntry(String id, WIPRequest wipRequest, WIPResponse wipResponse, String instance) {
		WIPConfiguration wipConfig = WIPConfigurationManager.getInstance().getConfiguration(instance);
		boolean enable = wipConfig.getEnableCache();
		
		boolean set = false;
		if (enable) {
			// Checking if the cache is forced to be public or private
			if (wipRequest.isResource()) {
				if (wipConfig.getResourceCachePublic()) {
					publicCache.put(wipRequest, wipResponse);
					set = true;
				}
			} else {
				if (wipConfig.getPageCachePrivate()) {
					if (!privateCache.containsKey(id)) 
						privateCache.put(id, new HashMap<WIPRequest, WIPResponse>());
					privateCache.get(id).put(wipRequest, wipResponse);
					set = true;
				}
			}
			// If it is not forced, checking if the response is private 
			if (!set) {
				if (wipResponse.isPrivateResponse()) {
					if (!privateCache.containsKey(id)) 
						privateCache.put(id, new HashMap<WIPRequest, WIPResponse>());
					privateCache.get(id).put(wipRequest, wipResponse);
				} else {
					publicCache.put(wipRequest, wipResponse);
				}
			}
		}
	}
	
	public WIPResponse getCacheEntry(String id, WIPRequest wipRequest, String instance) {
		WIPResponse wipResponse = null;
		WIPConfiguration wipConfig = WIPConfigurationManager.getInstance().getConfiguration(instance);
		boolean enable = wipConfig.getEnableCache();
		if (enable) {
			wipResponse = publicCache.get(wipRequest);
			if (wipResponse == null) {
				if (privateCache.containsKey(id))
					wipResponse = privateCache.get(id).get(wipRequest);
			}
		}
		return wipResponse;
	}
	
	public void removeCacheEntry(String id, WIPRequest wipRequest, String instance) {
		WIPConfiguration wipConfig = WIPConfigurationManager.getInstance().getConfiguration(instance);
		boolean enable = wipConfig.getEnableCache();
		if (enable) {
			WIPResponse wipResponse = publicCache.remove(wipRequest);
			if (wipResponse == null) 
				privateCache.get(id).remove(wipRequest);
		}
	}
	
	public void cleanPrivateCache(String id) {
		if (privateCache.get(id) != null)
			privateCache.get(id).clear();
	}
	
	public void cleanEntirePrivateCache() {
		privateCache.clear();
	}
	
	public void cleanPublicCache() {
		publicCache.clear();
	}

}
