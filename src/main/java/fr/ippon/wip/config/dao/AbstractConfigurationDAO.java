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

package fr.ippon.wip.config.dao;

import java.util.List;

import fr.ippon.wip.config.WIPConfiguration;

/**
 * This abstract Data Access Object defines the signatures of the CRUD method
 * for configurations.
 * 
 * @author Yohan Legat
 * 
 */
public abstract class AbstractConfigurationDAO {

	/**
	 * The default configuration name.
	 */
	public static final String DEFAULT_CONFIG_NAME = "default-config";

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

		boolean alreadyExists = exists(incrementName);
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
	 * Delete the configuration related to the given name.
	 * 
	 * @param name
	 *            the name of the configuration to delete
	 */
	public abstract boolean delete(String name);

	/**
	 * Delete the given configuration.
	 * 
	 * @param configuration
	 *            the configuration to delete
	 */
	public boolean delete(WIPConfiguration configuration) {
		return delete(configuration.getName());
	}

	public boolean exists(String name) {
		return getConfigurationsNames().contains(name);
	}

	/**
	 * Get the list of names of the saved configurations.
	 * 
	 * @return the list of names
	 */
	public abstract List<String> getConfigurationsNames();

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
	
	protected void updateTimestamp(WIPConfiguration configuration) {
		configuration.setTimestamp(System.currentTimeMillis());
	}
}
