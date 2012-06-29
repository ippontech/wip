package fr.ippon.wip.config.dao;

import java.util.ArrayList;
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
public class ConfigurationCacheDAO extends ConfigurationDAODecorator {

	// a map that contains the loaded configurations associated with their
	// names.
	private final Map<String, WIPConfiguration> cache;

	// a list that contains the name of the configurations.
	private final List<String> configurationNames;

	public ConfigurationCacheDAO(ConfigurationDAO decoratedDAO) {
		super(decoratedDAO);
		configurationNames = super.getConfigurationsNames();
		cache = new HashMap<String, WIPConfiguration>();
	}

	@Override
	public WIPConfiguration create(WIPConfiguration configuration) {
		configuration = super.create(configuration);
		if (configuration == null)
			return null;

		String name = configuration.getName();
		configurationNames.add(name);
		cache.put(name, (WIPConfiguration) configuration.clone());

		return configuration;
	}

	@Override
	public boolean delete(String name) {
		if (!super.delete(name))
			return false;

		cache.put(name, null);
		configurationNames.remove(name);
		return true;
	}

	@Override
	public boolean exists(String name) {
		return configurationNames.contains(name);
	}

	@Override
	public List<String> getConfigurationsNames() {
		return new ArrayList<String>(configurationNames);
	}

	@Override
	public WIPConfiguration read(String name) {
		WIPConfiguration configuration = cache.get(name);
		if (configuration != null)
			return (WIPConfiguration) configuration.clone();

		configuration = super.read(name);
		if (configuration != null)
			cache.put(name, (WIPConfiguration) configuration.clone());

		return configuration;
	}

	@Override
	public WIPConfiguration update(WIPConfiguration configuration) {
		configuration = super.update(configuration);
		if (configuration != null)
			cache.put(configuration.getName(), (WIPConfiguration) configuration.clone());

		return configuration;
	}
}
