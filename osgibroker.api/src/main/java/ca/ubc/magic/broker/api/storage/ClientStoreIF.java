package ca.ubc.magic.broker.api.storage;

import java.util.List;
import java.util.Set;

import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.ds.Client;

public interface ClientStoreIF {
	
	public static final String ADD_CLIENT = "add-client";
	public static final String GET_CLIENTS_BY_TOPIC = "get-clients-by-topic";
	public static final String GET_CLIENTS_BY_SUBSCRIPTION = "get-clients-by-subscription";
	public static final String DELETE_CLIENT = "delete-client";
	public static final String EXIST_CLIENT = "exist-client";
	public static final String GET_CLIENT_TOPIC_ID_BY_ID = "get-client-topicID-by-id";
	public static final String GET_CLIENT_TOPIC_ID_BY_NAME = "get-client-topicID-by-name";
	public static final String ADD_CLIENT_PROPS = "add-client-props";
	public static final String DELETE_CLIENT_PROPS = "delete-client-props";
	public static final String GET_CLIENT_PROPS = "get-client-props";
	public static final String UPDATE_CLIENT_PROPS = "update-client-props";
	
	public static final int NULL_CLIENT_ID = -1;

	/**
	 * Get the subscriptions in a channel
	 * 
	 * @param topic
	 * @return	a list of clientIDs for the registered clients under the topic
	 */
	public List<Client> getClientsByTopic(String topic) throws Exception;
	
	/**
	 * 
	 * @param subscription	the name of the subscription handling the client
	 * @return				a list of ClientIDs for the registered clients under with the defined subscription
	 * @throws Exception
	 */
	public List<Client> getClientsBySubscription(String subscription) throws Exception;
	
	/**
	 * Add subscription (channel to subscriber)
	 * 
	 * @param channel
	 * @param subscriber
	 * @throws Exception 
	 */
	
	public int addClient(String topic, String subscriberName, String clientType, String clientID) throws Exception;
	
	public void addClient(String topic, String subscriberName, RemoteClientIF client) throws Exception;
	
	
	
	/**
	 * Remove subscription
	 * 
	 * @param channel
	 * @param subscriber
	 */
	public void deleteClient(String topic, String clientID) throws Exception;
	
	public void deleteClient(String topic, RemoteClientIF client) throws Exception;
	
	// TODO
	// add subscription with filter, return id so it can be identified and removed
	
	// remove specific subscription by id
	
	/**
	 * checks to see if the client with the specified clientID is already available in the database 
	 * and under the defined channel
	 * 
	 * @param topic		the topic to be searched
	 * @param clientID	the id for the client to look for
	 */
	public boolean existsClient(String topic, String clientID) throws Exception;
	
	/**
	 * 
	 * checks to see if the client with the specified clientID is already available in the database 
	 * and under the defined channel
	 * 
	 * @param topic		the topic to be searched
	 * @param client	the client to look for
	 * @return			true if the client is already registered with the topic and false otherwise
	 * @throws Exception
	 */
	public boolean existsClient(String topic, RemoteClientIF client) throws Exception;
	
	/**
	 * returns the topics under which the client is registered.
	 * 
	 * @param clientID		the ID for the client whose topic is to be discovered
	 * @return				the ID for the topic to be returned
	 * @throws Exception
	 */
//	public int getClientTopicID (int clientDbID) throws Exception;
	
	/**
	 * 
	 * @param clientID
	 * @param topic
	 * @return
	 * @throws Exception
	 */
//	public HashMap<String, String> getClientProps(int clientDbID) throws Exception;
	
	/**
	 * 
	 * @param clientID
	 * @return
	 * @throws Exception
	 */
	public Set<String> getClientTopics(String clientID) throws Exception;
	
	/**
	 * The method updates the properties for a given client under the specified topic.
	 * 
	 * @param topic			The topic for which the client properties need to be updated
	 * @param client		The reference to the client whose value needs to be updated
	 * @throws Exception	The exception thrown in case of any failure
	 */
	public void updateClient(String topic, RemoteClientIF client) throws Exception;
}
