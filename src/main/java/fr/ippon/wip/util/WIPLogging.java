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

package fr.ippon.wip.util;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.io.FileUtils;

/**
 * Logging managment singleton for access log and transformation log.
 * Transformation log are only taking care of in debug mode. This mode should
 * not be activated in production: the naming log file process is not well
 * suited when several users access to the portlet.
 * 
 * @author Yohan Legat
 * 
 */
public enum WIPLogging {

	// the singleton instance
	INSTANCE;

	// the access log file handler
	private FileHandler accessFileHandler;

	// the url retrieved
	private String url;

	// accumulator used for creating unique file handlers.
	private final AtomicInteger acc = new AtomicInteger(1);

	private final NumberFormat format = new DecimalFormat("000");

	private final String HOME = System.getProperty("user.home");

	/*
	 * Several threads can be started when serving dependances of a resource, so
	 * transform log file handlers have to be linked to those threads.
	 */
	private static ThreadLocal<File> localLogFile = new ThreadLocal<File>();

	private WIPLogging() {
		try {
			// FileHandler launch an exception if parent path doesn't exist, so
			// we make sure it exists
			File logDirectory = new File(HOME + "/wip");
			if (!logDirectory.exists() || !logDirectory.isDirectory())
				logDirectory.mkdirs();

			accessFileHandler = new FileHandler("%h/wip/access.log", true);
			accessFileHandler.setLevel(Level.INFO);
			accessFileHandler.setFormatter(new SimpleFormatter());
			Logger.getLogger("fr.ippon.wip.http.hc.HttpClientExecutor.AccessLog").addHandler(accessFileHandler);

			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setFilter(new Filter() {

				public boolean isLoggable(LogRecord record) {
					return !record.getLoggerName().equals("fr.ippon.wip.http.hc.HttpClientExecutor.AccessLog");
				}
			});

			Logger.getLogger("fr.ippon.wip").addHandler(consoleHandler);

			// For HttpClient debugging
			// FileHandler fileHandler = new
			// FileHandler("%h/wip/httpclient.log", true);
			// fileHandler.setLevel(Level.ALL);
			// Logger.getLogger("org.apache.http.headers").addHandler(fileHandler);
			// Logger.getLogger("org.apache.http.headers").setLevel(Level.ALL);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close the log access file handler
	 */
	public void closeAccessLogFile() {
		accessFileHandler.close();
	}

	/**
	 * Close the transform file handler, if any, associated to the thread
	 * calling this method
	 */
	public void closeTransformLogFile() {
		localLogFile.set(null);
	}

	/**
	 * Create a new transform log file handler associated to the current url and to
	 * the thread calling this method
	 */
	private File createTransformLogFile() {
		String number = format.format(acc.getAndIncrement());
		File logFile = new File(HOME + "/wip/" + url + "_" + number + ".log");
		localLogFile.set(logFile);
		return logFile;
	}

	/**
	 * Log information into the transform log file handler linked to the thread
	 * calling this method.
	 * 
	 * @param className
	 * @param log
	 */
	public void logTransform(String log) {
		File logFile = localLogFile.get();
		if (logFile == null)
			logFile = createTransformLogFile();

		// retrieve the origin of the log call
		StackTraceElement broadcaster = Thread.currentThread().getStackTrace()[2];

		try {
			FileUtils.write(logFile, broadcaster + "\n" + log + "\n\n", true);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Change the state of this instance so that next transform log file created
	 * will be named after the given name
	 * 
	 * @param url
	 */
	public void resetForUrl(String url) {
		url = (url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
		this.url = url.substring(url.lastIndexOf("/") + 1);
		acc.set(1);

		for (File file : new File(HOME + "/wip").listFiles())
			if (file.getName().startsWith(this.url))
				FileUtils.deleteQuietly(file);
	}
}