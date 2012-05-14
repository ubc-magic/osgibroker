package ca.ubc.magic.broker.api.cache;

import java.util.List;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.ds.Attribute;

/**
 * The CacheElementIF class represents a single element in the Broker Cache.
 * 
 * @author nima
 *
 * @param <T> is the type of CacheElement as the CacheElement can have various types
 */
public interface CacheElementIF<T> {
	
	/**
	 * The Status enum type represents the Status of a cache element.
	 * <ul>
	 * <li/> intact: the content of the cache element is not modified
	 * <li/> added: the content of the cache element is just added to the cache
	 * <li/> updated: the content of the cache element is updated/changed
	 * <li/> deleted: the content of the cache element is deleted 
	 * </ul>
	 * @author nima
	 *
	 */
	public enum Status {
		intact, added, updated, deleted
	}
	
	/**
	 * @return	The status for the cache element
	 */
	public Status getCacheElemStatus();
	
	/**
	 * sets the status of the cache element
	 * 
	 * @param status	Specifies the status of the cache element
	 */
	public void setCacheElemStatus(Status status);
	
	/**
	 * adds the object to the list for the caceh element
	 * 
	 * @param obj	The object to be added to the list
	 * @return		The boolean value for the object to be added to the list
	 */
	public boolean add(T obj);
	
	public int size();
	
	public T get(int index);
	
	public T[] toArray(T[] obj);
	
	public List<T> getList();
	
	public void setList(List<T> list);
	
	public Attribute get(String attributeID) throws BrokerException;
}
