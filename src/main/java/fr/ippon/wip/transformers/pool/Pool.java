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

package fr.ippon.wip.transformers.pool;

/**
 * An basic interface for resources pools.
 * 
 * @author Yohan Legat
 *
 * @param <T> the type of resources inside the pool
 */
public interface Pool<T> {

	/**
	 * Return a resource from the pool
	 * @return a resource from the pool
	 */
	public T acquire();
	
	/**
	 * Put a resource inside the pool
	 * @param o the resource to put in the pool
	 */
	public void release(T resource);
	
}
