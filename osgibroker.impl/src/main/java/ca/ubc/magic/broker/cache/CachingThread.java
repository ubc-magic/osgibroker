package ca.ubc.magic.broker.cache;

import java.util.Dictionary;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.cache.CacheIF;

public class CachingThread extends Thread {
	
	private static final Logger logger = Logger.getLogger(  CachingThread.class );

	private CacheIF cache = null;
	private String savingPeriod = null;
	
	public CachingThread(CacheIF _cache, String _savingPeriod){
		this.cache = _cache;
		this.savingPeriod = _savingPeriod;
	}
	
	public String getSavingPeriod(){
		return savingPeriod;
	}

	public void run() {
		try{
			while(!Thread.currentThread().interrupted())
			{
					cache.commitCacheToDataStore();
					Thread.sleep(Long.parseLong(savingPeriod)*1000);
			}
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
	}
	
	public void destroy() {
		
		cache.cleanAll();
		
	}
}
