package fr.ippon.wip.config;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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

/**
 * The purpose of this class is to extract configurations put in the deploy
 * directory.
 * 
 * @author Yohan Legat
 * 
 */
public class DeployerWatcher {

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

	private ZipConfiguration zip;

	public DeployerWatcher() {
		try {
			URL url = getClass().getResource("/deploy");
			deployPath = new File(url.toURI());
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		zip = new ZipConfiguration();
		
	}

	/**
	 * Retrieve all the configuration pasted in the deploy directory.
	 * 
	 * @return the configuration to deploy
	 */
	public List<WIPConfiguration> checkDeploy() {
		List<WIPConfiguration> deployedConfigurations = new ArrayList<WIPConfiguration>();

		for (File file : deployPath.listFiles()) {
			if (file.getName().endsWith(".zip")) {
				try {
					deployedConfigurations = unzip(new ZipFile(file));

				} catch (ZipException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

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
	 * Extract configurations contained in a zipFile.
	 * 
	 * @param zipFile
	 *            the file containing the configurations
	 */
	private List<WIPConfiguration> unzip(ZipFile zipFile) {
		List<WIPConfiguration> configurations = new ArrayList<WIPConfiguration>();
		List<ZipEntry> entries = new ArrayList<ZipEntry>();
		CollectionUtils.addAll(entries, zipFile.entries());

		@SuppressWarnings("unchecked")
		Collection<ZipEntry> xmlEntries = CollectionUtils.select(entries, xmlPredicate);

		for (ZipEntry xmlEntry : xmlEntries) {
			try {
				String configName = FilenameUtils.getBaseName(xmlEntry.getName());
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
