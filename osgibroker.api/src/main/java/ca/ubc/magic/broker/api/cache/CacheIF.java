package ca.ubc.magic.broker.api.cache;

public interface CacheIF {
	
	public void addCacheEntry(CacheEntryIF cacheEntry);
	
	public CacheEntryIF getCacheEntry(Class<?> cacheEntryClass);
	
	public void removeCacheEntry(Class<?> cacheEntryClass);
	
	public CacheStoreIF getCacheStore();
	
	public void initCacheWithoutDataStore();
	
	public void initCacheFromDataStore() throws Exception;
	
	public void commitCacheToDataStore() throws Exception;

	public void cleanAll();
}
