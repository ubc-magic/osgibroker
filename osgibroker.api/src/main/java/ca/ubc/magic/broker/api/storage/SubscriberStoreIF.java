package ca.ubc.magic.broker.api.storage;

import java.sql.Connection;

public interface SubscriberStoreIF {
	
	public static final String ADD_SUBSCRIBER = "add-subscriber";
	public static final String GET_SUBSCRIBERS = "get-subscribers";
	public static final String DELETE_SUBSCRIBER = "delete-subscriber";
	public static final String EXIST_SUBSCRIBER = "exist-subscriber";

	/**
	 * Add a subscriber to the store
	 * @param sub
	 */
	public void addSubscriber(String subscriberId) throws Exception;
	
	/**
	 * Remove a subscriber from the store
	 * 
	 * @param subscriberId
	 */
	public void deleteSubscriber(String subscriberId) throws Exception;
	
	/**
	 * checks whether a subscription exists or not
	 * 
	 * @param subscriberId
	 * @return
	 * @throws Exception
	 */
	public boolean existsSubscriber(String subscriberId) throws Exception;
}
