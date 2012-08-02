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
	public T pick();
	
	/**
	 * Put a resource inside the pool
	 * @param o the resource to put in the pool
	 */
	public void leave(T resource);
	
}
