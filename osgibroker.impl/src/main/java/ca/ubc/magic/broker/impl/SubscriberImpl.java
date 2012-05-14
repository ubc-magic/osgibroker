package ca.ubc.magic.broker.impl;

import java.io.Serializable;
//import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.RemotePublisherIF;
import ca.ubc.magic.broker.api.RemoteSubscriberIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.notification.NotificationHelper;
import ca.ubc.magic.broker.api.storage.DBManagerIF;


/**
 * The SubscriberImpl provides an implementation of the SubscriberIF and the
 * RemoteSubscriberIF that enables adding and removing listeners and delivering
 * messages to the group of listeners
 *
 * @author nima
 *
 */

public abstract class SubscriberImpl extends Thread implements SubscriberIF, RemoteSubscriberIF {

	private static String subscriberName = "BasicSubscriber";

	// the set of clients registeres under a subscriber
	protected HashMap<String, CopyOnWriteArrayList<RemoteClientIF>> clients;

	// the publisher object that takes care of controlling publications by
	// remote clients
	protected   List<RemotePublisherIF> publishers;
	protected   DBManagerIF dbManager;
	
	// The thread pool is used to respond to the delivery of the messages to the
	// waiting clients concurrently while not blocking any of the clients to respond
	// to the others. This will speed up the execution in responding to the clients
	// while disallowing clients to block requests to other clients due to a halt
	// in their codes.
	private     ExecutorService pool = Executors.newCachedThreadPool();

	private static final Logger logger = Logger.getLogger( SubscriberImpl.class );

	public SubscriberImpl(RemotePublisherIF _publisher, DBManagerIF _dbManager){

		this(subscriberName, _publisher, _dbManager);
	}

	public SubscriberImpl(String _subscriberName, RemotePublisherIF _publisher, DBManagerIF _dbManager){

		subscriberName = _subscriberName;

		publishers = new CopyOnWriteArrayList<RemotePublisherIF>();

		if (_publisher != null)
			publishers.add(_publisher);

		this.clients = new HashMap<String, CopyOnWriteArrayList<RemoteClientIF>>();

		this.dbManager = _dbManager;

		try {
			if (!dbManager.getSubscriberStore().existsSubscriber(_subscriberName))
				dbManager.getSubscriberStore().addSubscriber(_subscriberName);
			else {
				registerClients(_subscriberName);
			}
		} catch (Exception e) {
			logger.error("subscription inclusion into the database failed miserably");
			logger.error(e.getMessage());
		}

	}

	/**
	 * a function to be overwritten by every implementation of the subscriber to decide on how
	 * previously stored clients in the database can restart their communication with the servlet
	 * again.
	 */
	public abstract void registerClients(String _subscriberName) throws Exception;


	/**
	 * Adds a listener to the subscriber
	 *
	 * @param client	the remote client willing to receive a message under a topic
	 * @param topic		the topic under which the client gets registered
	 * @throws Exception
	 */

	public void addListener(RemoteClientIF client, String topic) throws Exception {
		this.addListener(client, topic, false);
		if (!NotificationHelper.isNotificationTopic(topic))
			this.addListener(client, NotificationHelper.getNotificationTopic(topic), false);
	}

	/**
	 * Adds a listener to the subscriber
	 *
	 * @param client	the remote client willing to receive a message under a topic
	 * @param topic		the topic under which the client gets registered
	 * @param isUpdateFromDB	indicates whether the listeners are being updated from the DB or
	 * 							whehter they are newly registerd listeners
	 * @throws Exception
	 */
	public void addListener(RemoteClientIF client, String topic, boolean isUpdateFromDB) throws Exception {

		logger.debug("From DB [?" + Boolean.toString(isUpdateFromDB) + "]" +
				"adding client [" + client.getProperty(RemoteClientIF.CLIENT_ID) + "] to topic [" + topic + "]");
		
		if (existsClient(client, topic)){
			logger.debug("error: client [" + client.getProperty(RemoteClientIF.CLIENT_ID) + 
					"] is already available topic [" + topic + "]");
			throw new BrokerException(BrokerException.CONFILCT_CLIENT);
		}

		CopyOnWriteArrayList<RemoteClientIF> clientListeners = (CopyOnWriteArrayList<RemoteClientIF>) clients.get(topic);
		if (clientListeners == null)
			clientListeners = new CopyOnWriteArrayList<RemoteClientIF>();

		clients.put(topic, clientListeners);

		// if there is no client under a specific topic, the subscriber registers
		// itself with the subscriber first and then adds the client to its list of
		// listeners
		if (clientListeners.size() == 0)
			subscribe(topic, isUpdateFromDB);

		clientListeners.add(client);

		try{
			if (!isUpdateFromDB)
				dbManager.getClientStore().addClient(topic, subscriberName, client);
		}catch(Exception br){
			logger.debug("Inclusion of the client to the datbase has thrown an error!!");
			logger.error(br.getMessage());
		}
	}


	/**
	 * takes care of subscribing a subscriber with a publisher for a particular topic
	 *
	 * @param topic	the topic under which the subscriber gets subscribed
	 * @throws Exception if there is a problem with the database, an exception is thrown
	 */

	private void subscribe(String topic) throws Exception{
		subscribe(topic, false);
		if (!NotificationHelper.isNotificationTopic(topic))
			subscribe(NotificationHelper.getNotificationTopic(topic), false);
	}

	/**
	 * takes care of subscribing a subscriber with a publisher for a particular topic
	 *
	 * @param topic	the topic under which the subscriber gets subscribed
	 * @param isUpdateFromDB	indicates whether the updates are coming from the DB
	 * @throws Exception if there is a problem with the database, an exception is thrown
	 */
	private void subscribe(String topic, boolean isUpdateFromDB) throws Exception{

		Iterator it = publishers.iterator();

		while (it.hasNext()){

			RemotePublisherIF publisher = (RemotePublisherIF) it.next();
//			logger.debug("class for publisher: " + publisher.getClass().getName());
			publisher.addSubscriber(this, topic);

		}
		if (!isUpdateFromDB)
			dbManager.getTopicStore().addTopic(topic);
	}

	/**
	 * if the subscriber is not null when the publisher goes down, the subscriber resubscribes itself
	 * with the publisher as soon as it detects the publisher back into operation. This is a quite dangerous
	 * function as it resets the publisher. Care should be given whenever a reSubscription is happening due
	 * to chances for the publisher to be overwritten unwantedly
	 */
	public void reSubscribe(RemotePublisherIF publisher) throws Exception {

		//resets the publisher.
		if (!publishers.contains(publisher))
			publishers.add(publisher);

		if (clients != null && !clients.isEmpty()){
			logger.debug(clients.toString());
			if (clients.keySet() == null)
				logger.error("clients are not available");
			Iterator it = clients.keySet().iterator();
			while (it.hasNext()){
				String nextElem = (String) it.next();
				logger.debug(nextElem);
				this.subscribe(nextElem);
			}
		}
	}

	/**
	 * removes a listener from the list of listeners to a topic
	 *
	 * @param client	the client that is being removed from the list of subscribers
	 * @param topic		the topic from which the client is removed
	 * @throws Exception if there is a probelm with removing the client from the database, an exception is thrown
	 */
	@SuppressWarnings("unchecked")
	public void removeListener(RemoteClientIF client, String topic) throws Exception {
		CopyOnWriteArrayList<RemoteClientIF> clientListeners = (CopyOnWriteArrayList<RemoteClientIF>)clients.get(topic);
		CopyOnWriteArrayList<RemoteClientIF> clientEventListeners = (CopyOnWriteArrayList<RemoteClientIF>)clients.get(NotificationHelper.getNotificationTopic(topic));
		if ( clientListeners != null && clientListeners.contains(client) ){
			clientListeners.remove(client);
			clientEventListeners.remove(client);
			// Sets the Topic for the clients
			if (clientListeners.size() == 0)
				unsubscribe(topic);
			else
				clients.put(topic, clientListeners);

			// Sets the NotificationListener Topic for the clients
			if (clientEventListeners.size() == 0)
				unsubscribe(NotificationHelper.getNotificationTopic(topic));
			else
				clients.put(NotificationHelper.getNotificationTopic(topic), clientEventListeners);
		}
		dbManager.getClientStore().deleteClient(topic, (String) client.getProperty(RemoteClientIF.CLIENT_ID));
		dbManager.getClientStore().deleteClient(NotificationHelper.getNotificationTopic(topic), (String) client.getProperty(RemoteClientIF.CLIENT_ID));
		
		logger.debug(" client [" + client.getProperty(RemoteClientIF.CLIENT_ID) + "] removed from topic [" + topic + "]");
	}

	public void removeAllListeners(RemoteClientIF client) throws Exception {

		logger.debug(" removing client [" + client.getProperty(RemoteClientIF.CLIENT_ID) + "] from all topics");
		synchronized(this){
			for (String topic : clients.keySet())
				try{
					RemoteClientIF _client;
					if ((_client = getClient((String) client.getProperty(RemoteClientIF.CLIENT_ID), topic)) != null )
						removeListener(_client, topic);
				}catch(Exception e){
					logger.debug("removeAll failed for topic [" + topic + "] for client: " +
							client.getProperty(RemoteClientIF.CLIENT_ID));
				}
		}
	}

	/**
	 * if there is no more client under a topic for a subscriber, the subscriber
	 * gets removed from the list of subscribers to a topic for a publisher
	 */
	private void unsubscribe(String topic){

		Iterator it = publishers.iterator();
		while (it.hasNext()){

			RemotePublisherIF publisher = (RemotePublisherIF) it.next();
			publisher.removeSubscriber(this, topic);
			publisher.removeSubscriber(this, NotificationHelper.getNotificationTopic(topic));
		}
	}

	/**
	 * delivers a message to the clients registered with a topic
	 *
	 * @param message	the message to the clients registered with a topic
	 * @param topic		the topic to its listeners the message is delivered
	 * @throws Exception an exception is thrown if there is a problem with storing the event in the database
	 */
	@SuppressWarnings("unchecked")
	public void deliver(Serializable message, String topic) throws Exception{
		
		logger.debug("\n subscriber delivering message to topic: [" + topic + "]");

		String receiverID = ((Event) message).getAttribute(RemoteClientIF.RECEIVER_ID);
		String excludeID  = ((Event) message).getAttribute(RemoteClientIF.EXCLUDE_ID);

		if (receiverID != null && excludeID != null && receiverID.equals(excludeID))
			throw new BrokerException(BrokerException.CONFLICT_RECIVER_ID_EXCLUDE_ID);

		// if there is a receiverID indicated with the set of received events
		// the message gets only delivered to a receiverID rather than the whole
		// group of subscribers
		if (receiverID != null){
			logger.debug("message is only target for clientID [" + receiverID + "]");
			deliverTo(message, topic, receiverID);
			return;
		}

		// if there is a excludeID indicated with the set of received events
		// the message gets delivered to all the subscribed clients except for
		// the one whose ID is defined to be excluded by the publisher
		if (excludeID != null){
			logger.debug("message delivery exluded for clientID [" + excludeID + "]");
			deliverExclude(message, topic, excludeID);
			return;
		}

		// otherwise the message gets transferred to all the subscribed clients
		// in the list
		CopyOnWriteArrayList<RemoteClientIF> clientListeners = (CopyOnWriteArrayList<RemoteClientIF>)clients.get(topic);

		for (RemoteClientIF client : clientListeners){

			synchronized (this){
				if (client.isExpired()){
					try{
						this.removeListener(client, topic);
					}catch(Exception e){
						logger.error("Client " + client.getProperty(RemoteClientIF.CLIENT_ID) + "is expired but its removal failed!");
					}
					logger.info("Client " + client.getProperty(RemoteClientIF.CLIENT_ID) + "was expired and removed.");
				}
				else
					pool.execute(new WorkerThread(client, message));
			}
		}
	}

	/**
	 * returns the RemoteClient from the list of subscribed clients to a channel
	 *
	 * @param clientID
	 * @param topic
	 * @return
	 */
	public RemoteClientIF getClient(String clientID, String topic){

		CopyOnWriteArrayList<RemoteClientIF> clientListeners = (CopyOnWriteArrayList<RemoteClientIF>)clients.get(topic);

		if (clientListeners == null)
			return null;

		Iterator it = clientListeners.iterator();
		while (it.hasNext()){
			RemoteClientIF client = (RemoteClientIF) it.next();

			if (client.getProperty(RemoteClientIF.CLIENT_ID).equals(clientID)){

				if (client.isExpired()){
					try {
						this.removeListener(client, topic);
						return null;
					} catch (Exception e) {
						logger.error("Error in unsubscribing client with ClientID: " + clientID);
					}
				}else
					return client;
			}
		}
		return null;
	}

	/**
	 * delivers the message only to a client with the defined receiverID which
	 * is registered under the defined topic
	 *
	 * @param message		the message to be delivered
	 * @param topic			the topic under which the client should be listening
	 * @param receiverID	the ID for the client to receive the message
	 * @throws Exception	an exception is thrown if there is a problem with storing the message to a client
	 */
	private void deliverTo(Serializable message, String topic, String receiverID) throws Exception{

		RemoteClientIF client = getClient(receiverID, topic);
		if (!client.isExpired())
			pool.execute(new WorkerThread(client, message));
		else{
			this.removeListener(client, topic);
			logger.info("Client with ID" + receiverID + "is expired. No message delivery possible");
			throw new BrokerException ("Client with ID" + receiverID + "is expired. No message delivery possible");
		}

	}

	/**
	 * delivers the message to all the subscribed clients except for the one
	 * whose ID is defined to be excluded in the received message
	 *
	 * @param message		the message to be delivered
	 * @param topic			the topic under which the client should be listening
	 * @param receiverID	the ID for the client to receive the message
	 */
	@SuppressWarnings("unchecked")
	private void deliverExclude(Serializable message, String topic, String excludeID){
		CopyOnWriteArrayList<RemoteClientIF> clientListeners = (CopyOnWriteArrayList<RemoteClientIF>)clients.get(topic);

		Iterator it = clientListeners.iterator();
		while (it.hasNext()){
			RemoteClientIF client = (RemoteClientIF) it.next();
			if (client.getProperty(RemoteClientIF.CLIENT_ID).equals(excludeID))
				continue;
			else{
				if (client.isExpired()){
					try{
						this.removeListener(client, topic);
					}catch(Exception e){
						logger.error("Client " + client.getProperty(RemoteClientIF.CLIENT_ID) + "is expired but its removal failed!");
					}
					logger.info("Client " + client.getProperty(RemoteClientIF.CLIENT_ID) + "was expired and removed.");
				}
				else
					pool.execute(new WorkerThread(client, message));
			}
		}
	}

	/**
	 * checks whether a client is already registered with the subscriber. if so,
	 * the client won't get added again
	 *
	 * @param client	the client to be added to list
	 * @param topic		the topic the client is willing to be added under
	 * @return			true if the client is already registered with the topic and false if not
	 */
	protected boolean existsClient(RemoteClientIF client, String topic){

		return (getClient((String) client.getProperty(RemoteClientIF.CLIENT_ID), topic) != null) ? true : false;
	}

	public void renewSubscription(String clientID, String topic, long expiresInSec){

		RemoteClientIF client = this.getClient(clientID, topic);
		if (expiresInSec != -1){
			client.renewSubscription(expiresInSec);
		}
		else{
			client.renewSubscription();
		}
		try{
			dbManager.getClientStore().updateClient(topic, client);
			dbManager.getClientStore().updateClient(NotificationHelper.getNotificationTopic(topic), client);
		}catch(Exception e){
			logger.error("updating client properties threw error for the client: " + client.getProperty(RemoteClientIF.CLIENT_ID));
			System.out.println("Property Update Error for Client: " + client.getProperty(RemoteClientIF.CLIENT_ID));
		}
	}

	public void renewSubscription(String clientID, String topic){
		renewSubscription(clientID, topic, -1);
	}

	public void getDBManager(DBManagerIF manager) {
		// TODO Auto-generated method stub
	}

	public void ungetDBManager(DBManagerIF manager) {
		// TODO Auto-generated method stub
	}
}
