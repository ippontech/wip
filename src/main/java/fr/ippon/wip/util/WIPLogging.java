package fr.ippon.wip.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public enum WIPLogging {

	INSTANCE;

	private FileHandler accessFileHandler;

	private FileHandler transformFileHandler;

	private int acc;

	private Map<String, File> logMap;

	private WIPLogging() {
		try {
			logMap = new HashMap<String, File>();
			File logDirectory = new File(System.getProperty("user.home") + "/wip");
			// FileHandler launch an exception if parent path doesn't exist
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

	public void close() {
		accessFileHandler.close();
		if (transformFileHandler != null)
			transformFileHandler.close();
	}

	public File getLogFileByUrl(String url) {
		return logMap.get(url);
	}

	public List<String> getUrlsLogged() {
		List<String> urls = new ArrayList<String>(logMap.keySet());
		Collections.sort(urls);

		return urls;
	}

	public void newFileHandlerTransformer(String url) {
		try {
			if (transformFileHandler != null)
				transformFileHandler.close();

			transformFileHandler = new FileHandler("%h/wip/transform_" + acc + ".log", true);
			transformFileHandler.setLevel(Level.ALL);
			transformFileHandler.setFormatter(new SimpleFormatter());
			Logger.getLogger("fr.ippon.wip.http.hc.HttpClientExecutor").addHandler(transformFileHandler);
			Logger.getLogger("fr.ippon.wip.http.hc.HttpClientExecutor").setLevel(Level.ALL);
			Logger.getLogger("fr.ippon.wip.transformers.AbstractTransformer").addHandler(transformFileHandler);
			Logger.getLogger("fr.ippon.wip.transformers.AbstractTransformer").setLevel(Level.ALL);

			logMap.put(url, new File(System.getProperty("user.home") + "/wip/transform_" + acc + ".log"));
			acc++;

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
