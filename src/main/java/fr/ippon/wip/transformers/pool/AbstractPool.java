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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An abstract pool providing concurrency managment, so that it is multithread-safe
 * 
 * @author Yohan Legat
 *
 * @param <T> the type of resources inside the pool
 */
public abstract class AbstractPool<T> implements Pool<T> {

	// the lock used to manage concurrency
	private ReentrantLock lock = new ReentrantLock();

	// the resources pool
	protected Collection<T> pool;

	protected AbstractPool(Collection<T> pool) {
		this.pool = pool;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void leave(T resource) {
		try {
			lock.lock();
			pool.add(resource);

		} finally {
			lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public T pick() {
		try {
			lock.lock();
			if (!pool.isEmpty()) {
				Iterator<T> iterator = pool.iterator();
				T resource = iterator.next();
				iterator.remove();
				return resource;
			}
		} finally {
			lock.unlock();
		}

		try {
			Thread.sleep(15);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return pick();
	}
}
