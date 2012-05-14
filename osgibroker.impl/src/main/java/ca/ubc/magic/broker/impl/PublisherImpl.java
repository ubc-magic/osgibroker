package ca.ubc.magic.broker.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.RemotePublisherIF;
import ca.ubc.magic.broker.api.RemoteSubscriberIF;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.storage.DBManagerIF;

/**
 * The class PublisherImpl provides the implementation for a publisher by
 * implementing PublisherIF and RemotePublisherIF and by providing methods
 * to add or remove subscribers to a topic and to deliver messages to the 
 * subscribers for a topic
 * 
 * @author nima kaviani
 *
 */

public class PublisherImpl implements PublisherIF, RemotePublisherIF {
	
	//The name for the local publisher service
	public static final String NAME = "LocalPublisher";
	
	// The singleton publisher variable that is used for message exchange
	private static PublisherImpl publisher = null;
	
	private DBManagerIF dbManager = null;
	
	// A dictionary of subscribers and their corresponding topics
	private HashMap subscribers = null;
	
	// The thread pool is used to respond to the delivery of the messages to the
	// waiting clients concurrently while not blocking any of the clients to respond
	// to the others. This will speed up the execution in responding to the clients
	// while disallowing clients to block requests to other clients due to a halt
	// in their codes.
	private     ExecutorService pool = Executors.newCachedThreadPool();
	
	private static final Logger logger = Logger.getLogger(PublisherImpl.class);
	
	public PublisherImpl(){
		
		subscribers = new HashMap();
	}
	
	// The singleton method returning the singleton publisher variable
//	public static synchronized PublisherImpl getInstance(){
//		if (publisher == null)
//			publisher = new PublisherImpl();
//		return publisher;
//	}
	
	/**
	 * The deliver function is an implementation of the PublisherIF interface
	 * enabling subscribers to a topic to receive a message
	 * 
	 * @param message 	the messsage to be delivered to subscribers
	 * @param topic		the topic to its subscribers the message will be delivered
	 * @throws Exception exception is thrown if there is a problem with delivering the message to the subscriber
	 */
	@SuppressWarnings("unchecked")
	public void deliver(Serializable message, String topic) throws Exception {
		
		ArrayList<String> topicSubscribers = (ArrayList<String>) subscribers.get(topic);
		
		if (topicSubscribers == null){
			logger.debug("No subscriber found for topic " + topic);
		} else{
		
			Iterator subscribersIterator = topicSubscribers.iterator(); 
			while (subscribersIterator.hasNext()){
				RemoteSubscriberIF subscriber = (RemoteSubscriberIF) subscribersIterator.next();
				pool.execute(new WorkerThread(subscriber, message, topic));
			}
		}
		
		this.store (topic, message);
	}
	
	/**
	 * Registers a subscriber with a topic to the publisher
	 * 
	 * @param subscriber	the subscriber to be added to the dictionary of a publisher
	 * @param topic			the topic under which the subscriber is subscribed
	 */
	public void addSubscriber(RemoteSubscriberIF subscriber, String topic) {
		
		ArrayList topicSubscribers = (ArrayList) subscribers.get(topic);
		if (topicSubscribers == null){
			topicSubscribers = new ArrayList();			
		}
		
		if (topicSubscribers.contains(subscriber))
			return;
		
		topicSubscribers.add(subscriber);
		subscribers.put(topic, topicSubscribers);
		
	}

	/**
	 * Removes a subscriber from the list of listeners to a topic
	 * 
	 * @param subscriber	the subscriber to be removed from the dictionary of publisher
	 * @param topic			the topic from which the subscriber is removed
	 */
	public void removeSubscriber(RemoteSubscriberIF subscriber, String topic) {
		ArrayList topicSubscribers = (ArrayList)subscribers.get(topic); 
		if ( topicSubscribers != null&& topicSubscribers.contains(subscriber) ){
			topicSubscribers.remove(subscriber);
			subscribers.put(topic, topicSubscribers);
		}
	}

	public void getDBManager(DBManagerIF _dbManager) {
		
		this.dbManager = _dbManager;
	}

	public void ungetDBManager(DBManagerIF _dbManager) {
		if (this.dbManager == _dbManager)
			this.dbManager = null;
	}
	
	private void store(String topic, Serializable message) throws Exception{
		ArrayList topicSubscribers = (ArrayList) subscribers.get(topic);
		
		if (topicSubscribers == null)
			dbManager.getTopicStore().addTopic(topic);
		
		dbManager.getEventStore().addEvent(topic, (Event) message);
	}
}
