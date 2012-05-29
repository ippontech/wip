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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;

/**
 * This class is used to process the downloading of a file. The file will then
 * be accessed from the ResourceHandler servlet.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class WIPDownloader extends Thread {

    private static final Logger LOG = Logger.getLogger(WIPDownloader.class.getName());

    /**
	 * The id of the downloader, used by the ResourceHandler servlet to find it
	 * in the register
	 */
	private long downloaderId;

	/**
	 * A HttpMethod object, resulting from the execution of the request who lead
	 * to the download launched by this downloader
	 */
	private HttpResponse httpResponse;

	/**
	 * The content of the file downloaded by this downloader, as a String
	 */
	private String response;

    private String fileName;

	/**
	 * This object is a lock to make sure that the ResourceHandler servlet will
	 * wait for a complete download before writing its response
	 */
	private Object lock;

	/**
	 * Instanciate a new WIPDownloader from a HttpMethod object
	 * 
	 * @param response
	 *            The HttpMethod after execution of its request
	 */
	public WIPDownloader(HttpResponse response, String fileName) {
        this.fileName = fileName;
		setDownloadId(System.currentTimeMillis());
		this.httpResponse = response;
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
				setResponse(IOUtils.toString(httpResponse.getEntity().getContent()));
				getFileName();
			} catch (IOException e) {
                LOG.log(Level.FINE, "Error while fetching response", e);
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
		return fileName;
	}

}
