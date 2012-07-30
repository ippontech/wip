package fr.ippon.wip.config.dao;

import java.io.File;
import java.net.URL;

import javax.portlet.PortletContext;

/**
 * A singleton factory for creating instances of WIP configuration data access
 * object.
 * 
 * @author Yohan Legat
 * 
 */
public enum ConfigurationDAOFactory {

	INSTANCE;

	private ConfigurationDAO xmlDAO;

	private PortletContext context;

	/**
	 * We need a reference to the portlet context for retrieving the path to the
	 * server configuration folder. Any portlet application using this factory
	 * should call this method to make sure it has been set.
	 * 
	 * @param context
	 */
	public synchronized void setPortletContext(PortletContext context) {
		if (this.context == null)
			this.context = context;
	}

	/**
	 * Get the instance of a configuration DAO; this DAO persists the
	 * configurations as xml and xslt files.
	 * 
	 * @return the instance of a configuration DAO
	 */
	public synchronized ConfigurationDAO getXMLDAOInstance() {
		if (xmlDAO == null) {
			String pathConfigFiles = context.getRealPath("../../conf/wip");
			File configFolder = new File(pathConfigFiles);
			if (!configFolder.exists())
				configFolder.mkdir();

			xmlDAO = new DeployConfigurationDecorator(new ConfigurationCacheDecorator(new XMLConfigurationDAO(pathConfigFiles)));

			// we create the default configuration if it does not exist in the
			// server configuration folder
			if (xmlDAO.getDefaultConfiguration() == null)
				createDefaultConfiguration(xmlDAO);
		}

		return xmlDAO;
	}

	private void createDefaultConfiguration(ConfigurationDAO configurationDAO) {
		URL url = getClass().getResource("/default-configuration");
		String defaultConfigurationFolder = url.getFile();
		ConfigurationDAO defaultConfigurationDAO = new XMLConfigurationDAO(defaultConfigurationFolder);
		for (String configurationName : defaultConfigurationDAO.getConfigurationsNames())
			configurationDAO.create(defaultConfigurationDAO.read(configurationName));
	}
}
