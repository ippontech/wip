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

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a singleton used to register WIPDownloaders in a map, to make
 * them accessible from the ResourceHandler servlet
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class WIPDownloaderRegister {

	/**
	 * The unique instance of the class
	 */
	private static WIPDownloaderRegister instance = null;

	/**
	 * The map in which WIPDownloaders are stored
	 */
	private Map<Long, WIPDownloader> downloaderMap;

	/**
	 * The private constructor
	 */
	private WIPDownloaderRegister() {
		downloaderMap = new HashMap<Long, WIPDownloader>();
	}

	/**
	 * The method used to get the unique instance of the class
	 * 
	 * @return The unique instance of the class
	 */
	public static synchronized WIPDownloaderRegister getInstance() {
		if (instance == null)
			instance = new WIPDownloaderRegister();
		return instance;
	}

	/**
	 * Register a new WIPDownloader, i.e. add it to the map
	 * 
	 * @param id
	 *            The id of the WIPDownloader
	 * @param downloader
	 *            The WIPDownloader
	 */
	public void register(Long id, WIPDownloader downloader) {
		downloaderMap.put(id, downloader);
	}

	/**
	 * Unregister a WIPDownloader, i.e. remove it from the map
	 * 
	 * @param id
	 *            The id of the WIPDownloader
	 */
	public void unRegister(Long id) {
		downloaderMap.remove(id);
	}

	/**
	 * Get the WIPDownloader corresponding to the given id
	 * @param id The id of the WIPDownloader
	 * @return The WIPDownloader linked to the given id
	 */
	public WIPDownloader getDownloader(Long id) {
		return downloaderMap.get(id);
	}

}
