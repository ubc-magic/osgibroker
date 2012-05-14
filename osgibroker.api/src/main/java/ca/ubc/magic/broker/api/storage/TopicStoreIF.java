package ca.ubc.magic.broker.api.storage;

/*
 * OSGiBroker Project
 * Copyright (c) UBC Media and Graphics Interdisciplinary Centre (MAGIC) 2009
 * http://www.magic.ubc.ca/
 * 
 */

import java.sql.Connection;
import java.util.List;
import java.util.Set;

/**
 * Interface to channel storage
 * 
 * @author mike
 *
 */
public interface TopicStoreIF {
	
	public static final String ADD_TOPIC = "add-topic";
	public static final String EXIST_TOPIC = "exist-topic";
	public static final String DELETE_TOPIC = "delete-topic";
	public static final String GET_TOPIC_NAMES = "get-topic-names";
	public static final String GET_TOPIC_BY_ID = "get-topic-by-id";

	/**
	 * add a topic to the store
	 * 
	 * @param topic	the name of the topic
	 */
	public void addTopic(String topic) throws Exception;
	
	/**
	 * add a set of topics to the store
	 * @param topics
	 * @throws Exception
	 */
	public void addTopics(Set<String> topics) throws Exception;
	
	/**
	 * Delete topic from the store
	 * 
	 * @param topic
	 */
	public void deleteTopic(String topic) throws Exception;
	
	/**
	 * Return true if topic exists in the store
	 * @param topic name of topic
	 * @return true if topic exists
	 */
	public boolean topicExists(String topic) throws Exception;
	
	/**
	 * Get the topics that match the specified name pattern
	 * e.g. * for all, test.* for test.a.b.c, etc.
	 * @param pattern
	 */
	public List<String> getTopicNames(String pattern, int limit) throws Exception;
	
	/**
	 * 
	 * @param id	the ID for the topic
	 * @return		the name for the topic
	 * @throws Exception
	 */
	public String getTopicByID(int id) throws Exception;
}
