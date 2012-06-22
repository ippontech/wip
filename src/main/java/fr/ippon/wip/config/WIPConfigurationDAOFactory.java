package fr.ippon.wip.config;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * A singleton factory for creating instances of WIP configuration data access object.
 * 
 * @author Yohan Legat
 *
 */
public class WIPConfigurationDAOFactory {

	private WIPConfigurationDAO xmlDAO;

	private static WIPConfigurationDAOFactory instance;

	/**
	 * Get the unique instance of this class, creating it if necessary
	 * @return
	 */
	public synchronized static WIPConfigurationDAOFactory getInstance() {
		if (instance == null)
			instance = new WIPConfigurationDAOFactory();

		return instance;
	}

	private WIPConfigurationDAOFactory() {

	}

	/**
	 * Get the instance of a configuration DAO; this DAO persists the configurations as xml and xslt files.
	 * @return the instance of a configuration DAO
	 */
	public synchronized WIPConfigurationDAO getXMLInstance() {
		if (xmlDAO == null) {
			try {
				// set the configuration files location
				URL url = getClass().getResource("/configurations");
				String pathConfigFiles = new File(url.toURI()).toString();
				xmlDAO = new WIPConfigurationDAOCache(new XMLWIPConfigurationDAO(pathConfigFiles, true));

			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

		return xmlDAO;
	}
}
