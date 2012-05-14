package ca.ubc.magic.broker.api;

import java.io.Serializable;
import java.util.Set;

/**
 * The RemoteClinetIF interface represents the interface for every client that connects to the Broker.
 *
 *
 * @author nima
 *
 */
public interface RemoteClientIF {

	// Every client connecting to the broker is required to have a clientID
	public static final String CLIENT_ID = "clientID";

	// The receiverID is used to define whether a client is going to be the only receiver of a message
	public static final String RECEIVER_ID = "receiverID";

	// excludeID is used to exclude a client from the set of receipients of a message
	public static final String EXCLUDE_ID = "excludeID";

	// The URL is used for event redirecting. If <it>url</it> is used in combination with a clientID and
	// a topic, events sent to that topic will be forwarded to the specified URL
	public static final String  URL_SUBSCRIBER = "url";

	// A client may have various types depending on its method of subscription. It can be a TCP client, UDP client
	// a ServletClient, BluetoothClient, etc.
	public static final String CLIENT_TYPE = "clientType";

	public static final String ACTION = "action";

	// subscribe and unsubscribe are used as keywords to get the client to subscribe to or unsubscribe from the system
	public static final String SUBSCRIBE = "subscribe";
	public static final String UNSUBSCRIBE = "unsubscribe";

	// The indicator during client susbscription on whether or not the client is expired after certain period of time
	public static final String EXPIRES = "expires";
	public static final String EXPIRE_TIME_MILLIS = "expiresInMillis";
	public static final String REGISTRATION_TIME_MILLIS = "registrationTimeMillis";

	
	/**
	 * offers a communication specific deliver function to send the message to the clinet
	 *
	 * @param	message	the serializable message to be delivered to the client
	 */
	public void deliver(Serializable message);

	/**
	 * gets the property for a connecting clinet
	 *
	 * @param	key	The key for the property of a client
	 */
	public Object getProperty(String key);

	/**
	 * gets the set of all properties associated with a client
	 *
	 * @return	the set of all property names for a client
	 */
	public Set<String> getPropertyNames();

	/**
	 * puts a key/value pare as a pair of properties for the client
	 *
	 * @param	key		The key for a property related to a client
	 * @param	value	The value for a key related to a client
	 */
	public void putProperty(String key, Object value);

	/**
	 * @return	time in milliseconds indicating when the client is registered
	 */
	public long getRegistrationTimeMillis();

	/**
	 * @return 	time in milliseconds indicating how long after registration the client will expire
	 */
	public long getExpirationTimeMillis();

	/**
	 * @return	true of the client is already expired, false otherwise
	 */
	public boolean isExpired();

	/**
	 * resets the registration time for the connected client but keeps the expiration time
	 * as it was defined earlier by the system.
	 */
	public void renewSubscription();

	/**
	 * resets the registration time for the connected client and sets the expiration time
	 * to the new value sent to the server.
	 *
	 * @param _expiresIn	the time for the expiration of the registered client
	 */
	public void renewSubscription(long _expiresIn);
}
