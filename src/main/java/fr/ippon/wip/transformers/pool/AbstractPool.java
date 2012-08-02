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
