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

import java.util.ArrayList;
import java.util.List;

/**
 * This abstract Data Access Object defines the signatures of the CRUD method
 * for configurations.
 * 
 * @author Yohan Legat
 * 
 */
public abstract class WIPConfigurationDAO {

	/**
	 * The default configuration name.
	 */
	public static final String DEFAULT_CONFIG_NAME = "default-config";

	/**
	 * A list that contains the name of the configurations.
	 */
	protected final List<String> configurationNames;

	protected final DeployerWatcher deployerWatcher;

	public WIPConfigurationDAO() {
		this(true);
	}
	
	public WIPConfigurationDAO(boolean withWatcher) {
		configurationNames = new ArrayList<String>();
		deployerWatcher = withWatcher ? new DeployerWatcher() : null;
	}

	/**
	 * Check the deploy directory for seeking new configurations
	 */
	public synchronized void deploy() {
		List<WIPConfiguration> newConfigurations = deployerWatcher.checkDeploy();
		for (WIPConfiguration configuration : newConfigurations)
			create(configuration);
		
		resetConfigurationsNames();
	}

	/**
	 * Return "name(x+1)" while "name(x)" already exists as a configuration
	 * name.
	 * 
	 * @param name
	 *            the name of the configuration
	 * @param increment
	 *            the increment x
	 * @return a unique configuration name.
	 */
	protected String correctConfigurationName(String name, int increment) {
		String incrementName = name + "(" + increment + ")";

		boolean alreadyExists = getConfigurationsNames().contains(incrementName);
		return alreadyExists ? correctConfigurationName(name, increment + 1) : incrementName;
	}

	/**
	 * Save the given configuration.
	 * 
	 * @param configuration
	 *            the configuration to save
	 * @return the saved configuration
	 */
	public abstract WIPConfiguration create(WIPConfiguration configuration);

	/**
	 * Delete the given configuration.
	 * 
	 * @param name
	 *            the configuration to delete
	 */
	public abstract boolean delete(WIPConfiguration configuration);

	/**
	 * Get the list of names of the saved configurations.
	 * 
	 * @return the list of names
	 */
	public List<String> getConfigurationsNames() {
		return configurationNames;
	}

	/**
	 * Retrieve the default configuration
	 * 
	 * @return the default configuration
	 */
	public WIPConfiguration getDefaultConfiguration() {
		return read(DEFAULT_CONFIG_NAME);
	}

	/**
	 * Retrieve the configuration of the given name.
	 * 
	 * @param name
	 *            the name of the configuration to retrieve
	 * @return the configuration associated to the given name
	 */
	public abstract WIPConfiguration read(String name);

	/**
	 * Save the modifications of the given configuration.
	 * 
	 * @param configuration
	 *            the configuration to save
	 * @return the saved configuration
	 */
	public abstract WIPConfiguration update(WIPConfiguration configuration);
	
	/**
	 * Update the names of the configurations currently available.
	 */
	public abstract void resetConfigurationsNames();
}
