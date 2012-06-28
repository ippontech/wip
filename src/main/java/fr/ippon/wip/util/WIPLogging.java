package fr.ippon.wip.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public enum WIPLogging {

	INSTANCE;

	private FileHandler accessFileHandler;

	private FileHandler transformFileHandler;

	private int acc;

	private WIPLogging() {
		try {
			acc = 0;

			accessFileHandler = new FileHandler("%h/wip/access.log", true);
			accessFileHandler.setLevel(Level.INFO);
			accessFileHandler.setFormatter(new SimpleFormatter());
			Logger.getLogger("fr.ippon.wip.http.hc").addHandler(accessFileHandler);

			rotateTransformHandler();

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void close() {
		accessFileHandler.close();
		transformFileHandler.close();
	}

	public void rotateTransformHandler() {
		try {
			if(transformFileHandler != null)
				transformFileHandler.close();
			
			transformFileHandler = new FileHandler("%h/wip/transform_" + acc + ".log", true);
			transformFileHandler.setLevel(Level.ALL);
			transformFileHandler.setFormatter(new SimpleFormatter());
			Logger.getLogger("fr.ippon.wip.http.hc.HttpClientExecutor").addHandler(transformFileHandler);
			Logger.getLogger("fr.ippon.wip.http.hc.HttpClientExecutor").setLevel(Level.ALL);
			Logger.getLogger("fr.ippon.wip.transformers.AbstractTransformer").addHandler(transformFileHandler);
			Logger.getLogger("fr.ippon.wip.transformers.AbstractTransformer").setLevel(Level.ALL);
			
			acc++;

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
