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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

import fr.ippon.wip.config.dao.XMLConfigurationDAO;

/**
 * Allow zip and unzip operations over configurations.
 * 
 * @author Yohan Legat
 * 
 */
public class ZipConfiguration {

	/**
	 * Copy the data from an InputStream toward an OutputStream
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	private void copy(InputStream src, OutputStream dest) throws IOException {
		byte[] buffer = new byte[8192];
		int length;
		while ((length = src.read(buffer)) != -1)
			dest.write(buffer, 0, length);
	}

	/**
	 * Extract the files related to the configuration of the given name.
	 * 
	 * @param zipFile
	 * @param configurationName
	 * @return
	 * @throws IOException
	 */
	private boolean extract(ZipFile zipFile, String configurationName) throws IOException {
		XMLConfigurationDAO xmlConfigurationDAO = new XMLConfigurationDAO(FileUtils.getTempDirectoryPath());
		File file;
		ZipEntry entry;
		int[] types = new int[] { XMLConfigurationDAO.FILE_NAME_CLIPPING, XMLConfigurationDAO.FILE_NAME_TRANSFORM, XMLConfigurationDAO.FILE_NAME_CONFIG };
		for (int type : types) {
			file = xmlConfigurationDAO.getConfigurationFile(configurationName, type);
			entry = zipFile.getEntry(file.getName());
			if (entry == null)
				return false;

			copy(zipFile.getInputStream(entry), FileUtils.openOutputStream(file));
		}

		return true;
	}

	/**
	 * Unzip and return a configuration from a zip archive.
	 * 
	 * @param zipFile
	 *            the archive
	 * @param configurationName
	 *            the name of the configuration to retrieve
	 * @return the configuration to retrieve
	 * @throws IOException
	 */
	public WIPConfiguration unzip(ZipFile zipFile, String configurationName) throws IOException {
		XMLConfigurationDAO xmlConfigurationDAO = new XMLConfigurationDAO(FileUtils.getTempDirectoryPath());
		WIPConfiguration configuration = xmlConfigurationDAO.read(configurationName);
		if (configuration != null)
			xmlConfigurationDAO.delete(configuration);

		if (!extract(zipFile, configurationName))
			return null;

		configuration = xmlConfigurationDAO.read(configurationName);

		return configuration;
	}

	/**
	 * Create a zip archive from a configuration.
	 * 
	 * @param configuration
	 *            the configuration to zip
	 * @param out
	 *            the stream to be used
	 */
	public void zip(WIPConfiguration configuration, ZipOutputStream out) {
		XMLConfigurationDAO xmlConfigurationDAO = new XMLConfigurationDAO(FileUtils.getTempDirectoryPath());

		/*
		 * a configuration with the same name may already has been unzipped in
		 * the temp directory, so we try to delete it for avoiding name
		 * modification (see ConfigurationDAO.correctConfigurationName).
		 */
		xmlConfigurationDAO.delete(configuration);
		xmlConfigurationDAO.create(configuration);

		String configName = configuration.getName();

		try {
			int[] types = new int[] { XMLConfigurationDAO.FILE_NAME_CLIPPING, XMLConfigurationDAO.FILE_NAME_TRANSFORM, XMLConfigurationDAO.FILE_NAME_CONFIG };
			for (int type : types) {
				File file = xmlConfigurationDAO.getConfigurationFile(configName, type);
				ZipEntry entry = new ZipEntry(file.getName());
				out.putNextEntry(entry);
				copy(FileUtils.openInputStream(file), out);
				out.closeEntry();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
