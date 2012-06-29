package fr.ippon.wip.config.dao;

import java.util.List;

import fr.ippon.wip.config.WIPConfiguration;

/**
 * An abstract class for all the configurationDAO decorators
 * 
 * @author Yohan Legat
 *
 */
public abstract class ConfigurationDAODecorator extends ConfigurationDAO {

	private ConfigurationDAO decoratedDAO;

	public ConfigurationDAODecorator(ConfigurationDAO dao) {
		this.decoratedDAO = dao;
	}

	@Override
	public WIPConfiguration create(WIPConfiguration configuration) {
		return decoratedDAO.create(configuration);
	}

	@Override
	public boolean delete(String name) {
		return decoratedDAO.delete(name);
	}

	@Override
	public boolean exists(String name) {
		return decoratedDAO.exists(name);
	}

	@Override
	public List<String> getConfigurationsNames() {
		return decoratedDAO.getConfigurationsNames();
	}

	@Override
	public WIPConfiguration read(String name) {
		return decoratedDAO.read(name);
	}

	@Override
	public WIPConfiguration update(WIPConfiguration configuration) {
		return decoratedDAO.update(configuration);
	}
}
