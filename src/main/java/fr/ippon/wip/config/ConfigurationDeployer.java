package fr.ippon.wip.config;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FilenameUtils;

import fr.ippon.wip.config.XMLWIPConfigurationDAO;

/**
 * The purpose of this class is to extract configurations put in the deploy directory.
 *  
 * @author Yohan Legat
 *
 */
public class ConfigurationDeployer {

	// accept all files except the README one.
	private FileFilter fileFilterForDeletation = new FileFilter() {

		public boolean accept(File pathname) {
			String fileName = pathname.getName();
			return !fileName.equals("README");
		}
	};

	// accept al xml files.
	private Predicate xmlPredicate = new Predicate() {

		public boolean evaluate(Object object) {
			return ((ZipEntry) object).getName().endsWith(".xml");
		}
	};

	// directory in which configurations are past for deployment
	private File deployPath;

	// data access object for retrieving configurations in the deploy directory
	private XMLWIPConfigurationDAO deployDAO;

	public ConfigurationDeployer() {
		try {
			URL url = getClass().getResource("/deploy");
			deployPath = new File(url.toURI());
			deployDAO = new XMLWIPConfigurationDAO(deployPath.toString(), false);

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieve all the configuration pasted in the deploy directory.
	 * @return the configuration to deploy
	 */
	public List<WIPConfiguration> checkDeploy() {
		for (File file : deployPath.listFiles()) {
			if (file.getName().endsWith(".zip")) {
				try {
					extractZipFile(new ZipFile(file));

				} catch (ZipException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		List<WIPConfiguration> deployedConfigurations = new ArrayList<WIPConfiguration>();
		deployDAO.resetConfigurationsNames();
		for (String configurationName : deployDAO.getConfigurationsNames())
			deployedConfigurations.add(deployDAO.read(configurationName));

		cleanDeployRepertory();
		return deployedConfigurations;
	}

	/**
	 * Delete any files within the deploy directory, except the README file
	 */
	private void cleanDeployRepertory() {
		for (File file : deployPath.listFiles(fileFilterForDeletation))
			file.delete();
	}

	/**
	 * Copy the data from an InputStream toward an OutputStream
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	private void copy(InputStream src, OutputStream dest) throws IOException {
		byte[] buffer = new byte[1024];
		int readen;
		while ((readen = src.read(buffer)) != -1)
			dest.write(buffer, 0, readen);

		dest.close();
	}

	/**
	 * Extract a configuration from a zip file.
	 * @param zipFile
	 * @param name the name of the configuration
	 * @param configEntry
	 * @param clippingEntry
	 * @param transformEntry
	 */
	private void extractConfiguration(ZipFile zipFile, String name, ZipEntry configEntry, ZipEntry clippingEntry, ZipEntry transformEntry) {
		try {
			File configFile = deployDAO.getConfigurationFile(name, XMLWIPConfigurationDAO.FILE_NAME_CONFIG);
			File clippingFile = deployDAO.getConfigurationFile(name, XMLWIPConfigurationDAO.FILE_NAME_CLIPPING);
			File transformFile = deployDAO.getConfigurationFile(name, XMLWIPConfigurationDAO.FILE_NAME_TRANSFORM);

			copy(zipFile.getInputStream(configEntry), new FileOutputStream(configFile));
			copy(zipFile.getInputStream(clippingEntry), new FileOutputStream(clippingFile));
			copy(zipFile.getInputStream(transformEntry), new FileOutputStream(transformFile));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Extract configurations contained in a zipFile.
	 * @param zipFile the file containing the configurations
	 */
	private void extractZipFile(ZipFile zipFile) {
		List<ZipEntry> entries = new ArrayList<ZipEntry>();
		CollectionUtils.addAll(entries, zipFile.entries());

		@SuppressWarnings("unchecked")
		Collection<ZipEntry> xmlEntries = CollectionUtils.select(entries, xmlPredicate);

		for (ZipEntry xmlEntry : xmlEntries) {
			String name = FilenameUtils.getBaseName(xmlEntry.getName());
			ZipEntry clippingEntry = zipFile.getEntry(name + "_clipping.xslt");
			ZipEntry transformEntry = zipFile.getEntry(name + "_transform.xslt");

			if (clippingEntry != null && transformEntry != null)
				extractConfiguration(zipFile, name, xmlEntry, clippingEntry, transformEntry);
		}
	}
}
