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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

/**
 * The WIPConfiguration manager manages the store and the
 * retrieve of configurations.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 * @author Legat Yohan
 */
public class WIPConfigurationManager {

	private static final Logger LOG = Logger.getLogger(WIPConfigurationManager.class.getName());

	/**
	 * The WIPConfigurationManager singleton.
	 */
	private static WIPConfigurationManager instance = null;

	/**
	 * Get the WIPConfigurationManager singleton.
	 * 
	 * @return the class singleton
	 */
	public static synchronized WIPConfigurationManager getInstance() {
		try {
			if (instance == null)
				instance = new WIPConfigurationManager();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return instance;
	}

	/**
	 * A map that contains the configurations associated to their names.
	 */
	private final Map<String, WIPConfiguration> configurationsCache;

	/**
	 * A list that contains the name of the configurations. Names are sorted.
	 */
	private final List<String> configurationNames;

	/**
	 * The default configuration name.
	 */
	public static final String DEFAULT_CONFIG_NAME = "default-config";

	/**
	 * Configuration files location
	 */
	private String pathConfigFiles;

	/**
	 * The private constructor that initializes the singleton.
	 * 
	 * @throws URISyntaxException
	 */
	private WIPConfigurationManager() throws URISyntaxException {
		configurationsCache = new HashMap<String, WIPConfiguration>();
		configurationNames = new ArrayList<String>();

		// set the configuration files location
		URL url = getClass().getResource("/content/configurations/");
		pathConfigFiles = new File(url.toURI()).toString();

		// save the names of all the configurations
		for(String filename : new File(pathConfigFiles).list())
			configurationNames.add(filename.substring(0, filename.length() - 4));
		
		// instanciation of the default configuration as a read only configuration: it can't be changed
		File defaultConfigurationFile = getConfigurationFile(DEFAULT_CONFIG_NAME);
		WIPConfiguration defaultConfiguration = new WIPConfigurationImpl(defaultConfigurationFile, true);
		configurationsCache.put(DEFAULT_CONFIG_NAME, defaultConfiguration);
	}
	
	/**
	 * Return "name(x+1)" while "name(x)" already exists as a configuration name.
	 * @param name the name of the configuration
	 * @param increment the increment x
	 * @return a unique configuration name.
	 */
	private String correctConfigurationName(String name, int increment) {
		String incrementName = name + "(" + increment + ")";

		boolean alreadyExists = configurationNames.contains(incrementName);
		return alreadyExists ? correctConfigurationName(name, increment + 1) : incrementName;
	}

	/**
	 * Create a configuration.
	 * @param name the name of the configuration to create
	 * @return the new configuration
	 * @throws IOException
	 */
	public WIPConfiguration createConfiguration(String name) throws IOException {
		// correct the configuration name if it is already used
		if (configurationNames.contains(name))
			name = correctConfigurationName(name, 2);

		configurationNames.add(name);
		Collections.sort(configurationNames);

		File defaultConfigurationFile = getConfigurationFile(DEFAULT_CONFIG_NAME);
		File newConfigurationFile = getConfigurationFile(name);
		FileUtils.copyFile(defaultConfigurationFile, newConfigurationFile);

		return getConfiguration(name);
	}

	/**
	 * Delete the configuration associated to the given name.
	 * 
	 * @param name
	 *            the name of the configuration to delete
	 */
	public void deleteConfiguration(String name) {
		if (DEFAULT_CONFIG_NAME.equals(name))
			return;

		if (configurationNames.remove(name))
			configurationsCache.put(name, null);
	}

	/**
	 * Retrieve the configuration of the given name.
	 * @param name the name of the configuration to retrieve
	 * @return
	 */
	public WIPConfiguration getConfiguration(String name) {
		if (!configurationNames.contains(name))
			return null;

		WIPConfiguration configuration = configurationsCache.get(name);
		if (configuration != null)
			return configuration;

		File configurationFile = getConfigurationFile(name);
		configuration = new WIPConfigurationImpl(configurationFile);
		configurationsCache.put(name, configuration);

		return configuration;
	}

	/**
	 * Return the file associated to the given configuration name.
	 * @param name the name of the configuration
	 * @return the file associated to the configuration
	 */
	private File getConfigurationFile(String name) {
		return new File(pathConfigFiles + "/" + name + ".xml");
	}

	/**
	 * Retrive the default configuration
	 * @return the default configuration
	 */
	public WIPConfiguration getDefaultConfiguration() {
		return getConfiguration(DEFAULT_CONFIG_NAME);
	}

	/**
	 * Get the list of names of the saved configurations.
	 * 
	 * @return the list of names
	 */
	public List<String> getSavedConfigurations() {
		return configurationNames;
	}
}
