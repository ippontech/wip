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

package fr.ippon.wip.http;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;

/**
 * This class is used to process the downloading of a file. The file will then
 * be accessed from the ResourceHandler servlet.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class WIPDownloader extends Thread {

	/**
	 * The id of the downloader, used by the ResourceHandler servlet to find it
	 * in the register
	 */
	private long downloaderId;

	/**
	 * A HttpMethod object, resulting from the execution of the request who lead
	 * to the download launched by this downloader
	 */
	private HttpMethod method;

	/**
	 * The content of the file downloaded by this downloader, as a String
	 */
	private String response;

	/**
	 * This object is a lock to make sure that the ResourceHandler servlet will
	 * wait for a complete download before writing its response
	 */
	private Object lock;

	/**
	 * Instanciate a new WIPDownloader from a HttpMethod object
	 * 
	 * @param method
	 *            The HttpMethod after execution of its request
	 */
	public WIPDownloader(HttpMethod method) {
		setDownloadId(System.currentTimeMillis());
		this.method = method;
		setResponse("");
		lock = new Object();
	}

	/**
	 * Main method of the thread, who will start downloading the file This
	 * method is synchronized on the lock object
	 */
	@Override
	public void run() {
		synchronized (lock) {
			try {
				setResponse(method.getResponseBodyAsString());
				getFileName();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set the downloaded file to the given String
	 * 
	 * @param response
	 *            A String to store as the downloaded file
	 */
	public void setResponse(String response) {
		this.response = response;
	}

	/**
	 * Get the downloaded file as a String
	 * 
	 * @return The downloaded file as a String
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * Set the id of the downloader
	 * 
	 * @param downloaderId
	 *            The id
	 */
	public void setDownloadId(long downloaderId) {
		this.downloaderId = downloaderId;
	}

	/**
	 * Get the id of the downloader
	 * 
	 * @return The id
	 */
	public long getDownloaderId() {
		return downloaderId;
	}

	/**
	 * Register this downloader in the WIPDownloaderRegister
	 */
	public void register() {
		WIPDownloaderRegister.getInstance().register(downloaderId, this);
	}

	/**
	 * Unregister this downloader from the WIPDownloaderRegister
	 */
	public void unRegister() {
		WIPDownloaderRegister.getInstance().unRegister(downloaderId);
	}

	/**
	 * Get the lock object, used to wait for the download to be complete before
	 * accessing the file
	 * 
	 * @return The lock
	 */
	public Object getLock() {
		return lock;
	}
	
	/**
	 * Return the name of the file that this downloader will download
	 * @return
	 * 			The name of the file
	 */
	public String getFileName() {
		String result = "";
		try {
			result =  method.getURI().toString();
			int index = result.lastIndexOf('/');
			result = result.substring(index + 1);
			int index2 = result.indexOf('?');
			if (index2 > -1)
				result = result.substring(0, index2);
		} catch (URIException e) {
			e.printStackTrace();
		}
		return result;
	}

}
