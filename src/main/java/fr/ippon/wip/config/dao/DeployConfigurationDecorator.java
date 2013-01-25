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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;

import com.google.common.base.Predicate;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.ZipConfiguration;

import static com.google.common.collect.Iterators.filter;
import static com.google.common.collect.Iterators.forEnumeration;

/**
 * A decorator checking if some configuration have been dropped into the deploy
 * directory
 * 
 * @author Yohan Legat
 * 
 */
public class DeployConfigurationDecorator extends ConfigurationDAODecorator {

	// accept all files except the README one.
	private FileFilter fileFilterForDeletation = new FileFilter() {

		public boolean accept(File pathname) {
			String fileName = pathname.getName();
			return !fileName.equals("README");
		}
	};

	// filter all xml entries.
	private Predicate<ZipEntry> xmlPredicate = new Predicate<ZipEntry>() {

		public boolean apply(ZipEntry entry) {
			return entry.getName().endsWith(".xml");
		}
	};

	// filter all zip files
	private FilenameFilter zipFilter = new FilenameFilter() {

		public boolean accept(File dir, String name) {
			return name.endsWith(".zip");
		}
	};

	// directory in which configurations are past for deployment
	private File deployPath;

	private ZipConfiguration zip;

	public DeployConfigurationDecorator(AbstractConfigurationDAO dao) {
		super(dao);

        deployPath = new File(ConfigurationDAOFactory.INSTANCE.getDeployDirectoryLocation());
		zip = new ZipConfiguration();
	}

	/**
	 * Retrieve all the configuration pasted in the deploy directory.
	 * 
	 * @return the configuration to deploy
	 */
	private synchronized void checkDeploy() {
		List<WIPConfiguration> deployedConfigurations = new ArrayList<WIPConfiguration>();
		for (File file : deployPath.listFiles(zipFilter)) {
			try {
				deployedConfigurations = unzip(new ZipFile(file));

			} catch (ZipException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		cleanDeployRepertory();
		for (WIPConfiguration newConfiguration : deployedConfigurations)
			super.create(newConfiguration);
	}

	/**
	 * Delete any files within the deploy directory, except the README file
	 */
	private void cleanDeployRepertory() {
		for (File file : deployPath.listFiles(fileFilterForDeletation))
			file.delete();
	}

	@Override
	public WIPConfiguration create(WIPConfiguration configuration) {
		checkDeploy();
		return super.create(configuration);
	}

	@Override
	public List<String> getConfigurationsNames() {
		checkDeploy();
		return super.getConfigurationsNames();
	}

	@Override
	public WIPConfiguration read(String name) {
		checkDeploy();
		return super.read(name);
	}

	/**
	 * Extract configurations contained in a zipFile.
	 * 
	 * @param zipFile
	 *            the file containing the configurations
	 */
	private List<WIPConfiguration> unzip(final ZipFile zipFile) {
		List<WIPConfiguration> configurations = new ArrayList<WIPConfiguration>();
		Iterator<? extends ZipEntry> xmlEntries = filter(forEnumeration(zipFile.entries()), xmlPredicate);

		while (xmlEntries.hasNext()) {
			try {
				String configName = FilenameUtils.getBaseName(xmlEntries.next().getName());
				WIPConfiguration configuration = zip.unzip(zipFile, configName);

				if (configuration != null)
					configurations.add(configuration);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return configurations;
	}
}
