package fr.ippon.wip.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class ZipConfiguration {

	private XMLWIPConfigurationDAO xmlDAO;

	public ZipConfiguration(String path) {
		xmlDAO = new XMLWIPConfigurationDAO(path, false);
	}

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

	public WIPConfiguration unzip(ZipFile zipFile, String configurationName) {
		WIPConfiguration configuration = xmlDAO.read(configurationName);
		if (configuration != null)
			xmlDAO.delete(configuration);

		configuration = null;
		int[] types = new int[] { XMLWIPConfigurationDAO.FILE_NAME_CLIPPING, XMLWIPConfigurationDAO.FILE_NAME_TRANSFORM, XMLWIPConfigurationDAO.FILE_NAME_CONFIG };
		try {
			for (int type : types) {
				File file = xmlDAO.getConfigurationFile(configurationName, type);
				file.createNewFile();
				ZipEntry entry = zipFile.getEntry(FilenameUtils.getBaseName(file.getName()));
				if(entry == null)
					return null;
				
				copy(zipFile.getInputStream(entry), FileUtils.openOutputStream(file));
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return configuration;
	}

	/**
	 * Copy the data from an InputStream toward an OutputStream
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	private void copy(InputStream src, OutputStream dest) throws IOException {
		byte[] buffer = new byte[1024];
		int length;
		while ((length = src.read(buffer)) != -1)
			dest.write(buffer, 0, length);
	}
}
