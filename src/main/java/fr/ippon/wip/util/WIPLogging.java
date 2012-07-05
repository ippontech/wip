package fr.ippon.wip.util;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.io.FileUtils;

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
	private final AtomicInteger acc = new AtomicInteger(1);

	private final NumberFormat format = new DecimalFormat("000");

	private final String HOME = System.getProperty("user.home");

	/*
	 * Several threads can be started when serving dependances of a resource, so
	 * transform log file handlers have to be linked to those threads.
	 */
	private ThreadLocal<File> localLogFile = new ThreadLocal<File>();

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
			Logger.getLogger("fr.ippon.wip.http.hc.HttpClientExecutor").addHandler(accessFileHandler);
			Logger.getLogger("fr.ippon.wip").addHandler(new ConsoleHandler());

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
	 * Create a new transform log file handler associated to the resource and to
	 * the thread calling this method
	 */
	private File createTransformLogFile() {
		String number = format.format(acc.getAndIncrement());
		File logFile = new File(HOME + "/wip/" + resource + "_" + number + ".log");
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
	 * Change the state of this instance so that next transform log file
	 * created will be named after the given name
	 * 
	 * @param url
	 */
	public void resetForResource(String url) {
		url = (url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
		resource = url.substring(url.lastIndexOf("/") + 1);
		acc.set(1);
		
		for(File file : new File(HOME + "/wip").listFiles())
			if(file.getName().startsWith(resource))
				FileUtils.deleteQuietly(file);
	}
}