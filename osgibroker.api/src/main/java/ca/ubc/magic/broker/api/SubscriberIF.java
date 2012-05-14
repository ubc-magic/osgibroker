package ca.ubc.magic.broker.api;

/**
 * The interface for adding and removing client listeners to a subscriber
 * and under a particular topic
 *
 * @author nima kaviani
 *
 */

public interface SubscriberIF extends PersistenceIF {

	public static final String TOPIC = "topic";
	public static final String SUBSCRIBER_NAME = "subscriber";

	/**
	 * Adds a client as a listener to a subscriber
	 *
	 * @param client		The client to be added as a listener
	 * @param topic			The topic under which the client gets added
	 * @throws Exception	The exception thrown in case a problem with adding the client as a listener happens
	 */
	public void addListener(RemoteClientIF client, String topic) throws Exception;

	/**
	 * Removes a client from a topic
	 *
	 * @param client		The client to be removed from a topic
	 * @param topic			The topic from which the client is removed
	 * @throws Exception	The Exception thrown if any problem happens with removing the client from a topic
	 */
	public void removeListener(RemoteClientIF client, String topic) throws Exception;

	/**
	 * Removes the client from all topics that it might be subscribed to.
	 * @param client		The client to be removed
	 * @throws Exception
	 */
	public void removeAllListeners(RemoteClientIF client) throws Exception;

	/**
	 * returns the clinet object with an associated clinetID and topic
	 *
	 * @param clientID	The id for the client object to be returned
	 * @param topic		The topic for which the client is retrieved
	 * @return			The RemoteClientIF object for the clinetID
	 */
	public RemoteClientIF getClient (String clientID, String topic);

	/**
	 *
	 * @param _subscriberName
	 * @throws Exception
	 */
	public void registerClients(String _subscriberName) throws Exception;

	/**
	 * For clients whose subscriptions expire after expiresInSec seconds, this function
	 * resets the timer so that those clients can remain subscribed
	 *
	 * @param clientID		The ID for the client whose subscription is renewed
	 * @param topic			A chosen topic for the client under which the client is registered.
	 * 						The client has to prove its registration at least under one topic
	 * 						before being able to renew its subscription
	 * @param expiresInSec	If specified, indiciates the number of seconds that the client would
	 * 						like to stay subscribe after renewing its subscription
	 */
	public void renewSubscription(String clientID, String topic, long expiresInSec);

	public void renewSubscription(String clientID, String topic);
}
