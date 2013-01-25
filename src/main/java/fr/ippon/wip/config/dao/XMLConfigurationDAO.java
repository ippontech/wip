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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.thoughtworks.xstream.XStream;

import fr.ippon.wip.config.WIPConfiguration;

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
public class XMLConfigurationDAO extends AbstractConfigurationDAO {

	private static final Logger LOG = Logger.getLogger(XMLConfigurationDAO.class.getName());

	// type of file: general configuration, clipping or transformation
	public static final int FILE_NAME_CONFIG = 0;

	public static final int FILE_NAME_CLIPPING = 1;

	public static final int FILE_NAME_TRANSFORM = 2;

	public static String getConfigurationName(String name, int fileType) {
		switch (fileType) {
		case FILE_NAME_CONFIG:
			return name + ".xml";
		case FILE_NAME_CLIPPING:
			return name + "_clipping.xslt";
		case FILE_NAME_TRANSFORM:
			return name + "_transform.xslt";
		}

		return null;
	}

	// the resource used to marshall and unmarshal configuration as xml
	private XStream xstream;

	/**
	 * Configuration files location
	 */
	private String pathConfigFiles;

	/**
	 * The private constructor that initializes the singleton.
	 * 
	 * @throws URISyntaxException
	 */
	public XMLConfigurationDAO(String pathConfigFiles) {
		this.pathConfigFiles = pathConfigFiles;

		// initialization and configuration of xstream
		xstream = new XStream();
		xstream.alias("configuration", WIPConfiguration.class);
		xstream.omitField(WIPConfiguration.class, "id");
		xstream.omitField(WIPConfiguration.class, "xsltClipping");
		xstream.omitField(WIPConfiguration.class, "xsltTransform");
		xstream.omitField(WIPConfiguration.class, "javascriptResourcesMap");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized WIPConfiguration create(WIPConfiguration configuration) {
		// correct the configuration name if it is already used
		String name = configuration.getName();
		if (exists(name)) {
			name = correctConfigurationName(name, 2);
			configuration.setName(name);
		}

		updateTimestamp(configuration);
		write(configuration);
		return configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized boolean delete(String name) {
		// can't delete the default configuration
		if (DEFAULT_CONFIG_NAME.equals(name))
			return false;

		// can't remove a non-existing configuration
		if (!exists(name))
			return false;

		getConfigurationFile(name, FILE_NAME_CONFIG).delete();
		getConfigurationFile(name, FILE_NAME_CLIPPING).delete();
		getConfigurationFile(name, FILE_NAME_TRANSFORM).delete();
		return true;
	}

	/**
	 * Return the file associated to the given configuration name.
	 * 
	 * @param name
	 *            the name of the configuration
	 * @param fileType
	 *            config (0), clipping (1) or transform (2)
	 * @return the file associated to the configuration
	 */
	public File getConfigurationFile(String name, int fileType) {
		return new File(FilenameUtils.concat(pathConfigFiles, getConfigurationName(name, fileType)));
	}

	@Override
	public List<String> getConfigurationsNames() {
		List<String> configurationNames = new ArrayList<String>();
		for (String filename : new File(pathConfigFiles).list())
			if (filename.endsWith(".xml"))
				configurationNames.add(FilenameUtils.getBaseName(filename));

		Collections.sort(configurationNames);
		return configurationNames;
	}

	public String getPathConfigFiles() {
		return pathConfigFiles;
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
		if (!exists(name))
			return null;

		File configurationFile = getConfigurationFile(name, FILE_NAME_CONFIG);
		File clippingFile = getConfigurationFile(name, FILE_NAME_CLIPPING);
		File transformFile = getConfigurationFile(name, FILE_NAME_TRANSFORM);

		try {
			WIPConfiguration configuration = (WIPConfiguration) xstream.fromXML(configurationFile);
			configuration.setXsltClipping(FileUtils.readFileToString(clippingFile));
			configuration.setXsltTransform(FileUtils.readFileToString(transformFile));
			return (WIPConfiguration) configuration.clone();

		} catch (FileNotFoundException e) {
			LOG.log(Level.WARNING, "An error occured during the access of the configuration named " + name, e);
		} catch (IOException e) {
			LOG.log(Level.WARNING, "An error occured during the access of the configuration named " + name, e);
		}

		return null;
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

		if (!exists(name))
			return null;
		
		updateTimestamp(configuration);
		write(configuration);
		return configuration;
	}

	/**
	 * Write the configuration in files.
	 * 
	 * @param configuration
	 *            the configuration to save
	 */
	private void write(WIPConfiguration configuration) {
		String name = configuration.getName();
		File configurationFile = getConfigurationFile(name, FILE_NAME_CONFIG);
		File clippingFile = getConfigurationFile(name, FILE_NAME_CLIPPING);
		File transformFile = getConfigurationFile(name, FILE_NAME_TRANSFORM);

		try {
			xstream.toXML(configuration, new FileWriter(configurationFile));
			FileUtils.writeStringToFile(clippingFile, configuration.getXsltClipping());
			FileUtils.writeStringToFile(transformFile, configuration.getXsltTransform());

		} catch (IOException e) {
			LOG.log(Level.WARNING, "An error occured during the writing of the configuration named " + name, e);
		}
	}
}
