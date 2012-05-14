package ca.ubc.magic.broker.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.cache.CacheEntryIF;
import ca.ubc.magic.broker.api.cache.CacheIF;
import ca.ubc.magic.broker.api.cache.CacheStoreIF;
import ca.ubc.magic.broker.api.storage.DBManagerIF;

public class BrokerCache implements CacheIF {
	
	private static final Logger logger = Logger.getLogger( BrokerCache.class );
	
	public static final String NAME = "BrokerCache";
	
	private List<CacheEntryIF> cacheObjects = null;
	private CacheStoreIF cacheStoreImpl = null;
	
	public BrokerCache(){
		this(null);
	}
	
	public BrokerCache(DBManagerIF dbManager){
		
		cacheObjects = new ArrayList<CacheEntryIF>();
		cacheStoreImpl = new CacheStoreImpl();
		cacheStoreImpl.getDBManager(dbManager);
	}
	
	public void addCacheEntry(CacheEntryIF cacheEntry){
		cacheObjects.add(cacheEntry);
	}
	
	public CacheEntryIF getCacheEntry(Class<?> cacheEntryClass){
		
		Iterator<CacheEntryIF> cacheItr = cacheObjects.iterator();
		
		while(cacheItr.hasNext()){
			CacheEntryIF cacheObj = (CacheEntryIF) cacheItr.next();
			if (cacheObj.getClass().getName() == cacheEntryClass.getName())
				return cacheObj;
		}
		return null;
	}
	
	public void removeCacheEntry(Class<?> cacheEntryClass){
		
		Iterator<CacheEntryIF> cacheItr = cacheObjects.iterator();
		
		while(cacheItr.hasNext()){
			CacheEntryIF cacheObj = (CacheEntryIF) cacheItr.next();
			if (cacheObj.getClass().getName() == cacheEntryClass.getName())
				cacheObjects.remove(cacheObj);
		}
	}
	
	public CacheStoreIF getCacheStore(){
		return cacheStoreImpl;
	}
	
	public void initCacheFromDataStore() throws Exception {
		
		if (cacheObjects == null || cacheObjects.size() == 0)
			throw new BrokerException("No cacheobject is added to the cache.");
		cacheStoreImpl.read(cacheObjects);
		logger.info("BrokerCache was updated from the DataStore");
	}
	
	public void initCacheWithoutDataStore() {
		
		logger.warn("Renewing the cache without a DataStore!");
		cacheObjects = new ArrayList<CacheEntryIF>();
	}
	
	public void commitCacheToDataStore() {
		
		try{
			cacheStoreImpl.write(cacheObjects);
		}catch (Exception e){
			logger.error("Committing to the database failed. No DBManager found!");
		}
	}
	
	public void cleanAll(){
		try {
			cacheStoreImpl.clean(cacheObjects);
		} catch (Exception e) {
			logger.error("Cache Cleaning failed!");
		}
	}
}
