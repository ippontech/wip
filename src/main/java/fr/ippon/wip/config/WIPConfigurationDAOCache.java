package fr.ippon.wip.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class 
 * @author ylegat
 *
 */
public class WIPConfigurationDAOCache extends WIPConfigurationDAO {
	
	// a map that contains the loaded configurations associated with their names.
	private final Map<String, WIPConfiguration> cache;
	
	// a reference to the used DAO
	private final WIPConfigurationDAO dao;
	
	public WIPConfigurationDAOCache(WIPConfigurationDAO dao) {
		this.dao = dao;
		cache = new HashMap<String, WIPConfiguration>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public WIPConfiguration create(WIPConfiguration configuration) {
		configuration = dao.create(configuration);
		if(configuration != null)
			cache.put(configuration.getName(), (WIPConfiguration) configuration.clone());
		
		return configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean delete(WIPConfiguration configuration) {
		if(!dao.delete(configuration))
			return false;
		
		cache.put(configuration.getName(), null);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WIPConfiguration read(String name) {
		WIPConfiguration configuration = cache.get(name);
		if (configuration != null) 
			return (WIPConfiguration) configuration.clone();
		
		configuration = dao.read(name);
		cache.put(name, (WIPConfiguration) configuration.clone());
		return configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WIPConfiguration update(WIPConfiguration configuration) {
		configuration = dao.update(configuration);
		if(configuration != null)
			cache.put(configuration.getName(), (WIPConfiguration) configuration.clone());
		
		return configuration;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized List<String> getConfigurationsNames() {
		return dao.getConfigurationsNames();
	}
}
