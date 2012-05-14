package ca.ubc.magic.broker.api;

import java.io.Serializable;


/**
 * The PublisherIF is the interface enabling message delivery to a subscriber
 * under a particular topic
 * 
 *@author nima kaviani
 *
 */

public interface PublisherIF extends PersistenceIF{

	
	public static final String PUBLISHER_NAME = "publisher"; 
	public static final String SEND = "send";
	
	// delivers a serializable message to the set of subscribers under a topic
	void deliver(Serializable message, String topic) throws Exception;
	
}
