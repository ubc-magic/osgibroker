package ca.ubc.magic.broker.api.storage;

import java.sql.Connection;
import java.util.List;

import ca.ubc.magic.broker.api.ds.CacheObject;


public interface StateStoreIF {
	
	public static final String GET_TOPIC = "get-topic";
	public static final String GET_TOPIC_STATE = "get-topic-state";
	public static final String GET_TOPIC_STATE_NAMES = "get-topic-state-names";
	public static final String GET_TOPIC_ATTR = "get-topic-attr";
	public static final String SET_TOPIC_STATE = "set-topic-state";
	public static final String UPDATE_TOPIC_STATE = "update-topic-state";
	public static final String DELETE_TOPIC_ALL_STATES = "delete-topic-all-states";
	public static final String DELETE_TOPIC_STATE = "delete-topic-state";
	
	
	public List<String>  getTopics() throws Exception;
	
	public CacheObject   getTopicState(String topic) throws Exception;
	
	public String[]      getTopicStateNames(String topic) throws Exception;
	
	public CacheObject   getTopicAttribute(String topic, String stateID) throws Exception;
			
	public void setTopicState(String topic, CacheObject topicState) throws Exception;
	
	public void deleteTopicAllStates(String topic) throws Exception;
		
	public void deleteTopicState(String topic, String stateID) throws Exception;
}
