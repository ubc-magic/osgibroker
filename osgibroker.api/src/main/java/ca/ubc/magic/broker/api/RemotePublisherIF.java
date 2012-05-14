package ca.ubc.magic.broker.api;

/**
 * The RemotePublisherIF enables subscription with a remote publisher
 * 
 * @author nima kaviani
 *
 */
public interface RemotePublisherIF {	
	
	/**
	 * The method addSubscriber adds a subscriber under a topic such that the
	 * subscriber will receive all the messages sent to this topic
	 * 
	 * @param subscriber is the reference to the subscriber
	 * @param topic is the topic underwich the subscriber is subscribed
	 */
	void addSubscriber(RemoteSubscriberIF subscriber, String topic);		
	
	/**
	 * The method reomveSubscriber removes a subscriber from a topic so that
	 * the subscriber will no longer receive the messages sent to this topic
	 * 
	 * @param subscriber is the subscriber to be removed
	 * @param topic is the topic from which the subscriber will be removed
	 */
	void removeSubscriber(RemoteSubscriberIF subscriber, String topic);

}
