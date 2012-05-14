package ca.ubc.magic.broker.api.storage;

import java.sql.Connection;

import ca.ubc.magic.broker.api.cache.CacheElementIF;
import ca.ubc.magic.broker.api.ds.CacheObject;

public interface ContentStoreIF {
	
	public static final String GET_TOPIC = "get-topic";
	public static final String GET_TOPIC_CONTENT = "get-topic-content";
	public static final String GET_TOPIC_CONTENT_BY_ID = "get-topic-content-by-id";
	public static final String SET_TOPIC_CONTENT = "set-topic-content";
	public static final String DELETE_TOPIC_ALL_CONTENT = "delete-topic-all-content";
	public static final String DELETE_TOPIC_CONTENT = "delete-topic-content-by-id";
	public static final String GET_TOPIC_CONTENT_NAMES = "get-topic-content-ids";
	public static final String UPDATE_CONTENT_BY_ID = "update-content-by-id";
	
	
	public void setTopicContent(String topic, CacheObject topicContent) throws Exception;
	
	public CacheObject getTopicContent(String topic) throws Exception ;
	
	public CacheElementIF getContent(String topics, String contentID) throws Exception;
	
	public boolean existsContent(String topic, String contentID) throws Exception;
	
	public void updateContentByID(String topic, CacheObject topicContent, String contentID) throws Exception;
	
	public String[] getTopicContentNames(String topic) throws Exception;
	
	public void deleteTopicAllContent(String topic) throws Exception;
	
	public void deleteTopicContent(String topic, String contentID) throws Exception;
	
}
