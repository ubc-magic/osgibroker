package ca.ubc.magic.broker.api.cache;

import java.util.List;

import ca.ubc.magic.broker.api.PersistenceIF;

public interface CacheStoreIF extends PersistenceIF {
	
	public static final String CACHE_NAME = "cacheName";

	public void read(List<CacheEntryIF> cacheEntryList) throws Exception;
	
	public void write(List<CacheEntryIF> cacheObjects) throws Exception;
	
	public void clean(List<CacheEntryIF> cacheObjects) throws Exception;
}
