package ca.ubc.magic.broker.api.cache;

import java.util.Date;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.ds.CacheObject;
import ca.ubc.magic.broker.api.notification.NotificationListenerIF;

/**
 * The CacheEntryIF is the entry point for objects to be cached. It implements the typical CRUD behavior
 * for both the cache entry objects and their internal elements.
 * 
 * @author nima
 *
 */

public interface CacheEntryIF extends NotificationListenerIF {
	
	/**
	 * @return		The array of all topic names for every single cache object registered with the cache entry
	 */
	public String[] getCacheObjectTopics();
	
	/**
	 * Creates a cache entry object in the cache entry by using the topic and the specified cacheObj
	 * 
	 * @param topic		The topic name to register the cache object under
	 * @param cacheObj	The cache object to be registered in the cache entry
	 */
	public CacheObject createCacheObject(String topic);
	
	/**
	 * Retrieves the cache object associated with the spcified topic
	 * 
	 * @param topic		The name of the topic to retrieve its cache object
	 * @return			The retrieved cache object
	 */
	public CacheObject    getCacheObject(String topic);
	
	/**
	 * Updates the cache object associated with a topic
	 * 
	 * @param topic			The name of the topic whose cache object needs to be updated
	 * @param cacheObj		The cache object to be updated
	 * @return				The old cache object
	 */
	public CacheObject updateCacheObject(String topic, CacheObject cacheObj);
	
	/**
	 * Removes the cache object of a specified topic
	 * 
	 * @param topic				the name of the topic whose cache object needs to be removed
	 * @return					the remove cache object or null if the cache object does not exist
	 */
	public CacheObject  removeCacheObject(String topic) throws BrokerException;
	
	/**
	 * Checks whether or not there is a cache object under a topic name
	 * 
	 * @param topic		The name of the topic to check for existence of its cache object
	 * @return			<i>true</i> if the cache object exists, <i>false</i> otherwise
	 */
	public boolean		  existsCacheObject(String topic);
	
	public Date getLastAccessed();
	
	public int getExpiresAfter();
	
	public boolean isExpired();
	
	public void setLastAccessed(Date dateTime );
	
	public void setExpiresAfter(int milliseconds);
	
	public void clean();
}
