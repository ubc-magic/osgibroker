package ca.ubc.magic.broker.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.cache.CacheEntryIF;
import ca.ubc.magic.broker.api.cache.CacheStoreIF;
import ca.ubc.magic.broker.api.ds.CacheObject;
import ca.ubc.magic.broker.api.ds.ElementMap;
import ca.ubc.magic.broker.api.ds.TopicContent;
import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.cache.content.ContentManager;
import ca.ubc.magic.broker.cache.state.StateManager;

public class CacheStoreImpl implements CacheStoreIF {
	
	private static final Logger logger = Logger.getLogger( CacheStoreImpl.class );
	
	private DBManagerIF dbManager;

	public void read(List<CacheEntryIF> cacheEntryList) throws Exception {
		
		if (dbManager == null)
			throw new BrokerException("No DBManager was found to retrieve cache info from");
		
		List<String> topicLists = dbManager.getTopicStore().getTopicNames("t*", 500);

		for (CacheEntryIF cacheEntry : cacheEntryList){

			Iterator<String> it = topicLists.iterator();
			while (it.hasNext()){
				String topic = (String) it.next();
				if (cacheEntry instanceof StateManager)
					cacheEntry.updateCacheObject(topic, (CacheObject) dbManager.getStateStore().getTopicState(topic));
				else if (cacheEntry instanceof ContentManager)
					cacheEntry.updateCacheObject(topic, (CacheObject) dbManager.getContentStore().getTopicContent(topic));
			}
		}
	}

	public void write(List<CacheEntryIF> cacheObjects) throws Exception{
		if (dbManager == null)
			throw new BrokerException("No DBManager was found to retrieve cache info from");
		
		this.storeTopics(cacheObjects);
		
		Iterator<CacheEntryIF> it = cacheObjects.iterator();
		while (it.hasNext()){
			CacheEntryIF cacheObject = (CacheEntryIF) it.next();
			
			this.storeCacheEntry(cacheObject);
		}
	}
	
	private void storeTopics(List<CacheEntryIF> cacheObjects) throws Exception{
		
		Set<String> topics = new HashSet<String>(); 
		
		Iterator<CacheEntryIF> itr = cacheObjects.iterator();
		while (itr.hasNext()){
			CacheEntryIF cacheEntry = itr.next();
			Object[] cacheEntryTopics = cacheEntry.getCacheObjectTopics();
			for (Object topic : cacheEntryTopics){
				topics.add((String)topic);
			}
		}
		dbManager.getTopicStore().addTopics(topics);
	}
	
	private void storeCacheEntry(CacheEntryIF cacheObject){
		
		CacheEntryIF cacheEntryManager = cacheObject;
		String[] topics = cacheObject.getCacheObjectTopics();
		for (String topic : topics){
			CacheObject topicCacheObject = cacheEntryManager.getCacheObject(topic);
			try{
				if (cacheEntryManager instanceof StateManager)
					dbManager.getStateStore().setTopicState(topic, (ElementMap) topicCacheObject);
				if (cacheEntryManager instanceof ContentManager)
					dbManager.getContentStore().setTopicContent(topic, (CacheObject) topicCacheObject);
			}catch(Exception e){
				logger.error("Committing CacheElement failed for topic: " + topic);
			}
		}
	}
	
	public void getDBManager(DBManagerIF _dbManager) {
		
		this.dbManager = _dbManager;
	}

	public void ungetDBManager(DBManagerIF _dbManager) {
		if (this.dbManager == _dbManager)
			this.dbManager = null;
	}

	public void clean(List<CacheEntryIF> cacheObjects) throws Exception {
		
		Iterator<CacheEntryIF> itr = cacheObjects.iterator();
		
		while (itr.hasNext()){
			CacheEntryIF cacheObject = (CacheEntryIF) itr.next();
			cacheObject.clean();
		}		
	}
}
