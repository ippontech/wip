package fr.ippon.wip.util;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import fr.ippon.wip.http.hc.HttpClientExecutor;

public enum WIPLogging {

	INSTANCE;

	private static final Logger LOG = Logger.getLogger(HttpClientExecutor.class.getName());
	
	private FileHandler accessFileHandler;

	private String resource;

	private int acc;
	
	private ThreadLocal<FileHandler> localTransformFileHandler = new ThreadLocal<FileHandler>();

	private WIPLogging() {
		try {
			File logDirectory = new File(System.getProperty("user.home") + "/wip");
			// FileHandler launch an exception if parent path doesn't exist, so we make sure it exists
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

	public void closeAll() {
		accessFileHandler.close();
		closeTransformFileHandler();
	}
	
	public void closeTransformFileHandler() {
		FileHandler transformFileHandler = localTransformFileHandler.get();
		if(transformFileHandler != null) {
			transformFileHandler.close();
			localTransformFileHandler.set(null);
		}
	}
	
	public void newFileHandlerTransformer(String url) {
		url = (url.endsWith("/")) ? url.substring(0, url.length() - 1) : url;
		resource = url.substring(url.lastIndexOf("/") + 1);
		acc = 1;
	}
	
	public void logInTransformFileHandler(Class className, String log) {
		FileHandler transformFileHandler = localTransformFileHandler.get();
		if(transformFileHandler == null)
			nextFileHandlerTransformer();
		
		Logger.getLogger(className.getName() + "." + Thread.currentThread().getId()).finest(log);
	}

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
}