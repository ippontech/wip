package fr.ippon.wip.config;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class WIPConfigurationDAOBuilder {

	private WIPConfigurationDAO xmlDAO;
	
	private static WIPConfigurationDAOBuilder instance;
	
	private WIPConfigurationDAOBuilder() {
		
	}
	
	public synchronized static WIPConfigurationDAOBuilder getInstance() {
		if(instance == null)
			instance = new WIPConfigurationDAOBuilder();
		
		return instance;
	}
	
	public synchronized WIPConfigurationDAO getXMLInstance() {
		if(xmlDAO == null) {
			try {
				// set the configuration files location
				URL url = getClass().getResource("/configurations");
				String pathConfigFiles = new File(url.toURI()).toString();
				xmlDAO = new WIPConfigurationDAOCache(new XMLWIPConfigurationDAO(pathConfigFiles));
				
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		return xmlDAO;
	}
}
