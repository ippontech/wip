package fr.ippon.wip.config.dao;

import java.io.File;
import java.io.FileFilter;
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
import com.google.common.collect.Iterators;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.ZipConfiguration;

/**
 * A decorator checking if some configuration have been dropped into the deploy directory
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

	// directory in which configurations are past for deployment
	private File deployPath;

	private ZipConfiguration zip;

	public DeployConfigurationDecorator(ConfigurationDAO dao) {
		super(dao);

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
	private void checkDeploy() {
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
	private List<WIPConfiguration> unzip(ZipFile zipFile) {
		List<WIPConfiguration> configurations = new ArrayList<WIPConfiguration>();
		Iterator<? extends ZipEntry> xmlEntries = Iterators.filter(Iterators.forEnumeration(zipFile.entries()), xmlPredicate);

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