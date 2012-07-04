package fr.ippon.wip.util;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Logging managment singleton for access log and transformation logs.
 * 
 * @author Yohan Legat
 * 
 */
public enum WIPLogging {

	// the singleton instance
	INSTANCE;

	// the access log file handler
	private FileHandler accessFileHandler;

	// the resource retrieved
	private String resource;

	// accumulator used for creating unique file handler
	private int acc;

	/*
	 * Several threads can be started when serving dependances of a resource, so
	 * transform log file handlers have to be linked to those threads.
	 */
	private ThreadLocal<FileHandler> localTransformFileHandler = new ThreadLocal<FileHandler>();

	private WIPLogging() {
		try {
			File logDirectory = new File(System.getProperty("user.home") + "/wip");
			// FileHandler launch an exception if parent path doesn't exist, so
			// we make sure it exists
			if (!logDirectory.exists() || !logDirectory.isDirectory())
				logDirectory.mkdirs();

			accessFileHandler = new FileHandler("%h/wip/access.log", true);
			accessFileHandler.setLevel(Level.INFO);
			accessFileHandler.setFormatter(new SimpleFormatter());
			Logger.getLogger("fr.ippon.wip.http.hc").addHandler(accessFileHandler);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close the log access file handler
	 */
	public void closeAll() {
		accessFileHandler.close();
	}

	/**
	 * Close the transform file handler, if any, associated to the thread
	 * calling this method
	 */
	public void closeTransformFileHandler() {
		FileHandler transformFileHandler = localTransformFileHandler.get();
		if (transformFileHandler != null) {
			transformFileHandler.close();
			localTransformFileHandler.set(null);
		}
	}

	/**
	 * Log information into the transform log file handler linked to the thread
	 * calling this method.
	 * 
	 * @param className
	 * @param log
	 */
	public void logInTransformFileHandler(Class className, String log) {
		FileHandler transformFileHandler = localTransformFileHandler.get();
		if (transformFileHandler == null)
			nextFileHandlerTransformer();

		Logger.getLogger(className.getName() + "." + Thread.currentThread().getId()).finest(log);
	}

	/**
	 * Create a new transform log file handler associated to the resource and to
	 * the thread calling this method
	 */
	private synchronized void nextFileHandlerTransformer() {
		try {
			NumberFormat format = new DecimalFormat("000");
			String number = format.format(acc);
			FileHandler transformFileHandler = new FileHandler("%h/wip/" + resource + "_" + number + ".log", true);
			transformFileHandler.setLevel(Level.ALL);
			transformFileHandler.setFormatter(new SimpleFormatter());
			localTransformFileHandler.set(transformFileHandler);

			Logger.getLogger("fr.ippon.wip.http.hc.HttpClientExecutor." + Thread.currentThread().getId()).addHandler(transformFileHandler);
			Logger.getLogger("fr.ippon.wip.http.hc.HttpClientExecutor." + Thread.currentThread().getId()).setLevel(Level.ALL);
			Logger.getLogger("fr.ippon.wip.transformers.AbstractTransformer." + Thread.currentThread().getId()).addHandler(transformFileHandler);
			Logger.getLogger("fr.ippon.wip.transformers.AbstractTransformer." + Thread.currentThread().getId()).setLevel(Level.ALL);
			acc++;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Change the state of this instance so that next transform log file
	 * handlers created will be named after the name of the resource
	 * 
	 * @param url
	 */
	public void nextResource(String url) {
		url = (url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
		resource = url.substring(url.lastIndexOf("/") + 1);
		acc = 1;
	}
}