package ca.ubc.magic.broker.api;

import java.io.Serializable;

/**
 * The subscriber interface that delivers a message under a particular topic
 * to the set of listeners registered with it
 * 
 * @author nima
 *
 */

public interface RemoteSubscriberIF {

	/**
	 * delivers the serializable message to all clients registered under a topic
	 * 
	 * @param message	The messge to be delivered
	 * @param topic		The topic to its clients the message gets delivered
	 * @throws Exception	The Exception thrown if the client is not found
	 */
	void deliver(Serializable message, String topic) throws Exception;	
}
