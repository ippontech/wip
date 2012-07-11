package fr.ippon.wip.config.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ippon.wip.config.WIPConfiguration;

/**
 * A decorator used as cache for configurations.
 * 
 * @author Yohan Legat
 * 
 */
public class ConfigurationCacheDecorator extends ConfigurationDAODecorator {

	// a map that contains the loaded configurations associated with their
	// names.
	private final Map<String, WIPConfiguration> cache;

	// a list that contains the name of the configurations.
	private final List<String> configurationNames;
	
	private static final Comparator<String> lowerCaseComparator = new Comparator<String>() {
		public int compare(String s1, String s2) {
			return s1.toLowerCase().compareTo(s2.toLowerCase());
		}
	};

	public ConfigurationCacheDecorator(ConfigurationDAO decoratedDAO) {
		super(decoratedDAO);
		configurationNames = super.getConfigurationsNames();
		Collections.sort(configurationNames, lowerCaseComparator);
		cache = new HashMap<String, WIPConfiguration>();
	}

	@Override
	public synchronized WIPConfiguration create(WIPConfiguration configuration) {
		configuration = super.create(configuration);
		if (configuration == null)
			return null;

		String name = configuration.getName();
		configurationNames.add(name);
		cache.put(name, (WIPConfiguration) configuration.clone());
		Collections.sort(configurationNames, lowerCaseComparator);

		return configuration;
	}

	@Override
	public synchronized boolean delete(String name) {
		if (!super.delete(name))
			return false;

		cache.put(name, null);
		configurationNames.remove(name);
		return true;
	}

	@Override
	public synchronized boolean exists(String name) {
		return configurationNames.contains(name);
	}

	@Override
	public synchronized List<String> getConfigurationsNames() {
		return new ArrayList<String>(configurationNames);
	}

	@Override
	public synchronized WIPConfiguration read(String name) {
		WIPConfiguration configuration = cache.get(name);
		if (configuration != null)
			return (WIPConfiguration) configuration.clone();

		configuration = super.read(name);
		if (configuration != null)
			cache.put(name, (WIPConfiguration) configuration.clone());

		return configuration;
	}

	@Override
	public synchronized WIPConfiguration update(WIPConfiguration configuration) {
		configuration = super.update(configuration);
		if (configuration != null)
			cache.put(configuration.getName(), (WIPConfiguration) configuration.clone());

		return configuration;
	}
}
