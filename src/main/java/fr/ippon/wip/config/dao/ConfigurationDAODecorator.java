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

package fr.ippon.wip.config.dao;

import java.util.List;

import fr.ippon.wip.config.WIPConfiguration;

/**
 * An abstract class for all the configurationDAO decorators
 * 
 * @author Yohan Legat
 *
 */
public abstract class ConfigurationDAODecorator extends AbstractConfigurationDAO {

	private AbstractConfigurationDAO decoratedDAO;

	public ConfigurationDAODecorator(AbstractConfigurationDAO dao) {
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
