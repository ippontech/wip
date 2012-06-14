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

package fr.ippon.wip.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;

/**
 * This Data Access Object allow CRUD operation on WIP configurations. A
 * configuration named "conf" is saved in three different file:
 * conf_clipping.xslt: contains the xslt code for clipping; conf_transform.xslt:
 * contains the xslt transformation code; conf.xml: contains the rest of the
 * configuration.
 * 
 * This class is thread-safe.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 * @author Yohan Legat
 */
public class XMLWIPConfigurationDAO extends WIPConfigurationDAO {

	private static final Logger LOG = Logger.getLogger(XMLWIPConfigurationDAO.class.getName());

	// type of file: general configuration, clipping or transformation
	private static final int FILE_NAME_CONFIG = 0;

	private static final int FILE_NAME_CLIPPING = 1;

	private static final int FILE_NAME_TRANSFORM = 2;

	// the resource used to marshall and unmarshal configuration as xml
	private XStream xstream;

	/**
	 * The WIPConfigurationManager singleton.
	 */
	private static WIPConfigurationDAO instance = null;

	/**
	 * A map that contains the configurations associated to their names.
	 */
	private final Map<String, WIPConfiguration> configurationsCache;

	/**
	 * Configuration files location
	 */
	private String pathConfigFiles;

	/**
	 * Get the WIPConfigurationManager singleton.
	 * 
	 * @return the class singleton
	 */
	public static synchronized WIPConfigurationDAO getInstance() {
		try {
			if (instance == null)
				instance = new XMLWIPConfigurationDAO();

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return instance;
	}

	/**
	 * The private constructor that initializes the singleton.
	 * 
	 * @throws URISyntaxException
	 */
	private XMLWIPConfigurationDAO() throws URISyntaxException {
		// initialization and configuration of xstream
		xstream = new XStream();
		xstream.alias("configuration", WIPConfiguration.class);
		xstream.omitField(WIPConfiguration.class, "id");
		xstream.omitField(WIPConfiguration.class, "xsltClipping");
		xstream.omitField(WIPConfiguration.class, "xsltTransform");
		xstream.omitField(WIPConfiguration.class, "javascriptResourcesMap");

		configurationsCache = new HashMap<String, WIPConfiguration>();

		// set the configuration files location
		URL url = getClass().getResource("/configurations");
		pathConfigFiles = new File(url.toURI()).toString();

		// save the names of all the configurations
		for (String filename : new File(pathConfigFiles).list())
			if (filename.endsWith(".xml"))
				configurationNames.add(filename.substring(0, filename.length() - 4));

		Collections.sort(configurationNames);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized WIPConfiguration create(WIPConfiguration configuration) {
		// correct the configuration name if it is already used
		String name = configuration.getName();
		if (configurationNames.contains(name)) {
			name = correctConfigurationName(name, 2);
			configuration.setName(name);
		}

		configurationNames.add(name);
		Collections.sort(configurationNames);

		update(configuration);
		return read(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void delete(WIPConfiguration configuration) {
		String name = configuration.getName();

		if (DEFAULT_CONFIG_NAME.equals(name))
			return;

		if (configurationNames.remove(name)) {
			configurationsCache.put(name, null);
			getConfigurationFile(name, FILE_NAME_CONFIG).delete();
			getConfigurationFile(name, FILE_NAME_CLIPPING).delete();
			getConfigurationFile(name, FILE_NAME_TRANSFORM).delete();
		}
	}

	/**
	 * Return the file associated to the given configuration name.
	 * 
	 * @param name
	 *            the name of the configuration
	 * @return the file associated to the configuration
	 */
	private File getConfigurationFile(String name, int fileType) {
		switch (fileType) {
		case FILE_NAME_CONFIG:
			return new File(pathConfigFiles + "/" + name + ".xml");
		case FILE_NAME_CLIPPING:
			return new File(pathConfigFiles + "/" + name + "_clipping.xslt");
		case FILE_NAME_TRANSFORM:
			return new File(pathConfigFiles + "/" + name + "_transform.xslt");
		}

		return null;
	}

	/**
	 * Retrieve the configuration of the given name. The configuration is a
	 * clone such as several portlets won't share the same reference on a
	 * configuration. This way wan can prevent data race.
	 * 
	 * @param name
	 *            the name of the configuration to retrieve
	 * @return the configuration associated to the given name
	 */
	@Override
	public synchronized WIPConfiguration read(String name) {
		if (!configurationNames.contains(name))
			return null;

		WIPConfiguration configuration = configurationsCache.get(name);
		if (configuration != null)
			return (WIPConfiguration) configuration.clone();

		File configurationFile = getConfigurationFile(name, FILE_NAME_CONFIG);
		File clippingFile = getConfigurationFile(name, FILE_NAME_CLIPPING);
		File transformFile = getConfigurationFile(name, FILE_NAME_TRANSFORM);

		try {
			configuration = (WIPConfiguration) xstream.fromXML(configurationFile);
			configuration.setXsltClipping(FileUtils.readFileToString(clippingFile));
			configuration.setXsltTransform(FileUtils.readFileToString(transformFile));
			configurationsCache.put(name, configuration);

		} catch (FileNotFoundException e) {
			LOG.log(Level.WARNING, "An error occured during the access of the configuration named " + name, e);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "An error occured during the access of the configuration named " + name, e);
		}

		return (WIPConfiguration) configuration.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized WIPConfiguration update(WIPConfiguration configuration) {
		String name = configuration.getName();
		// the default configuration can't be changed
		if (DEFAULT_CONFIG_NAME.equals(name))
			return configuration;

		// the configuration must have been created first before it can be
		// updated
		if (!configurationNames.contains(name))
			return configuration;

		File configurationFile = getConfigurationFile(name, FILE_NAME_CONFIG);
		File clippingFile = getConfigurationFile(name, FILE_NAME_CLIPPING);
		File transformFile = getConfigurationFile(name, FILE_NAME_TRANSFORM);

		try {
			xstream.toXML(configuration, new FileWriter(configurationFile));

			FileUtils.writeStringToFile(clippingFile, configuration.getXsltClipping());
			FileUtils.writeStringToFile(transformFile, configuration.getXsltTransform());
			configurationsCache.put(name, configuration);

		} catch (IOException e) {
			LOG.log(Level.WARNING, "An error occured during the saving of the configuration named " + name, e);
		}

		return configuration;
	}
}
