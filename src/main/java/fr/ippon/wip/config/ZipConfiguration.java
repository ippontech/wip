package fr.ippon.wip.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;

/**
 * Allow zip and unzip operations over configurations.
 * 
 * @author Yohan Legat
 * 
 */
public class ZipConfiguration {

	private XMLWIPConfigurationDAO xmlDAO;

	public ZipConfiguration() {
		xmlDAO = new XMLWIPConfigurationDAO(FileUtils.getTempDirectoryPath(), false);
	}

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
		xmlDAO.resetConfigurationsNames();
		WIPConfiguration configuration = xmlDAO.read(configurationName);
		if (configuration != null)
			xmlDAO.delete(configuration);

		if (!extract(zipFile, configurationName))
			return null;

		xmlDAO.resetConfigurationsNames();
		configuration = xmlDAO.read(configurationName);

		return configuration;
	}

	/**
	 * Extract the files related to the configuration of the given name.
	 * @param zipFile
	 * @param configurationName
	 * @return
	 * @throws IOException
	 */
	private boolean extract(ZipFile zipFile, String configurationName) throws IOException {
		File file;
		ZipEntry entry;
		int[] types = new int[] { XMLWIPConfigurationDAO.FILE_NAME_CLIPPING, XMLWIPConfigurationDAO.FILE_NAME_TRANSFORM, XMLWIPConfigurationDAO.FILE_NAME_CONFIG };
		for (int type : types) {
			file = xmlDAO.getConfigurationFile(configurationName, type);
			entry = zipFile.getEntry(file.getName());
			if (entry == null)
				return false;

			copy(zipFile.getInputStream(entry), FileUtils.openOutputStream(file));
		}

		return true;
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
		String configName = configuration.getName();
		xmlDAO.delete(configuration);
		xmlDAO.create(configuration);

		try {
			int[] types = new int[] { XMLWIPConfigurationDAO.FILE_NAME_CLIPPING, XMLWIPConfigurationDAO.FILE_NAME_TRANSFORM, XMLWIPConfigurationDAO.FILE_NAME_CONFIG };
			for (int type : types) {
				File file = xmlDAO.getConfigurationFile(configName, type);
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
