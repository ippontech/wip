package fr.ippon.wip.config.dao;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

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

    private final Logger LOG = Logger.getLogger(ConfigurationDAOFactory.class.getName());

	private AbstractConfigurationDAO xmlDAO;
	private PortletContext context;
    private String confDirectoryLocation;
    private String deployDirectoryLocation;

    public void setConfDirectoryLocation(String confDirectoryLocation) {
        this.confDirectoryLocation = confDirectoryLocation;
    }

    public String getDeployDirectoryLocation() {
        return deployDirectoryLocation;
    }

    public void setDeployDirectoryLocation(String deployDirectoryLocation) {
        this.deployDirectoryLocation = deployDirectoryLocation;
    }

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
	public synchronized AbstractConfigurationDAO getXMLDAOInstance() {
		if (xmlDAO == null) {
            if (confDirectoryLocation == null) {
                throw new IllegalStateException("confDirectoryLocation has not been initialized");
            }

            LOG.info("pathConfigFiles is "+ confDirectoryLocation+" on server "+context.getServerInfo());

            File configFolder = new File(confDirectoryLocation);
            if (!configFolder.exists()) {
                configFolder.mkdirs();
            }

            File deployFolder = new File(deployDirectoryLocation);
            if (!deployFolder.exists()) {
                deployFolder.mkdirs();
            }

            xmlDAO = new DeployConfigurationDecorator(new ConfigurationCacheDecorator(new XMLConfigurationDAO(confDirectoryLocation)));

            // we create the default configuration if it does not exist in the
            // server configuration folder
            if (xmlDAO.getDefaultConfiguration() == null)
                createDefaultConfiguration(xmlDAO);
        }
		return xmlDAO;
	}

	private void createDefaultConfiguration(AbstractConfigurationDAO configurationDAO) {
		URL url = getClass().getResource("/default-configuration");
		String defaultConfigurationFolder = url.getFile();
		AbstractConfigurationDAO defaultConfigurationDAO = new XMLConfigurationDAO(defaultConfigurationFolder);
		for (String configurationName : defaultConfigurationDAO.getConfigurationsNames())
			configurationDAO.create(defaultConfigurationDAO.read(configurationName));
	}
}
