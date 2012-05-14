package ca.ubc.magic.broker.subscriber.service.servlet;


import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import ca.ubc.magic.broker.api.cache.CacheElementIF;
import ca.ubc.magic.broker.api.cache.CacheIF;
import ca.ubc.magic.broker.api.ds.CacheObject;
import ca.ubc.magic.broker.api.ds.Client;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.storage.ClientStoreIF;
import ca.ubc.magic.broker.api.storage.ContentStoreIF;
import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.api.storage.EventStoreIF;
import ca.ubc.magic.broker.api.storage.StateStoreIF;
import ca.ubc.magic.broker.api.storage.SubscriberStoreIF;
import ca.ubc.magic.broker.api.storage.TopicStoreIF;
import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.RemotePublisherIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.cache.BrokerCache;
import ca.ubc.magic.broker.impl.PublisherImpl;

/**
 * The class ContextProvider is a singleton class to provide required context objects for servlets. This
 * is a work around to solve the problem of passing objects to the servlets that we have for OSGiBroker.
 * The current implementation is week and only provides a minimum basic ContextProvider with the 
 * RemotePublisherIF, SubscriberIF, and DBManagerIF to be passed to the servlets. If the class doesn't invoke
 * the <i>init</i> method then the default values are used for testing. However, in the actual execution, the
 * proper values from the broker (discovered from the OSGiBroker registry) should be passed to this to be used
 * by the servlets.
 * 
 * @author nima
 *
 */
public class ContextProvider {
	
	private static ContextProvider contextProvider = null;
	
	private RemotePublisherIF  rpublisher  = null;
	private PublisherIF        lpublisher = null;
	private SubscriberIF subscriber = null;
	private DBManagerIF  dbManager  = null;
	private CacheIF  brokerCache = null;
	
	/**
	 * initializing the ContextProvider to default PublisherImpl, a NULL DBManagerIF, and a default SubsciberIF.
	 * The init method should always be called to change the default values for the context provider.
	 */
	private ContextProvider(){
		
		this.rpublisher = new PublisherImpl();
		this.lpublisher = (PublisherIF) this.rpublisher;
		this.brokerCache = new BrokerCache(dbManager);
		
//		this.dbManager = new MySQLDBManager();
		this.dbManager = new DBManagerIF(){

			public ClientStoreIF getClientStore() {
				return new ClientStoreIF() {

					public int addClient(String topic, String subscriberName,
							String clientType, String clientID)
							throws Exception {
								return 0;
					}

					public void addClient(String topic, String subscriberName,
							RemoteClientIF client) throws Exception {
					}

					public void deleteClient(String topic, String clientID)
							throws Exception {
					}

					public void deleteClient(String topic, RemoteClientIF client)
							throws Exception {
					}

					public boolean existsClient(String topic, String clientID)
							throws Exception {
						return true;
					}

					public boolean existsClient(String topic,
							RemoteClientIF client) throws Exception {
						return true;
					}

					public Set<String> getClientTopics(String clientID)
							throws Exception {
						return null;
					}

					public List<Client> getClientsBySubscription(String subscription)
							throws Exception {
						return null;
					}

					public List<Client> getClientsByTopic(String topic)
							throws Exception {
						return null;
					}

					public void updateClient(String topic, RemoteClientIF client) throws Exception {}
				};
			}

			public EventStoreIF getEventStore() {
				return new EventStoreIF(){

					public void addEvent(String topic, Event event)
							throws Exception {
					}

					public void deleteEvents(String topic) throws Exception {
					}

					public List<Event> findEvents(String queryString)
							throws Exception {
						return null;
					}

					public Event getEvent(String id) throws Exception {
						return null;
					}

					public List<Event> getLastEvents(String topic, int numEvents)
							throws Exception {
						return null;
					}

					public List<Event> getLastEvents(String topic)
							throws Exception {
						return null;
					}




					public List<Event> getAfterEClient(String topic,
							long queryStart, int queryAfterE)
							throws SQLException, XPathExpressionException {
						// TODO Auto-generated method stub
						return null;
					}

					public List<Event> getAfterEServer(String topic,
							long queryStart, int queryAfterE)
							throws SQLException, XPathExpressionException {
						// TODO Auto-generated method stub
						return null;
					}

					public List<Event> getAfterTClient(String topic,
							long queryStart, long queryAfterT)
							throws SQLException, XPathExpressionException {
						// TODO Auto-generated method stub
						return null;
					}

					public List<Event> getAfterTServer(String topic,
							long queryStart, long queryAfterT)
							throws SQLException, XPathExpressionException {
						// TODO Auto-generated method stub
						return null;
					}

					public List<Event> getBeforeEClient(String topic,
							long queryStart, int queryBeforeE)
							throws SQLException, XPathExpressionException {
						// TODO Auto-generated method stub
						return null;
					}

					public List<Event> getBeforeEServer(String topic,
							long queryStart, int queryBeforeE)
							throws SQLException, XPathExpressionException {
						// TODO Auto-generated method stub
						return null;
					}

					public List<Event> getBeforeTClient(String topic,
							long queryStart, long queryBeforeT)
							throws SQLException, XPathExpressionException {
						// TODO Auto-generated method stub
						return null;
					}

					public List<Event> getBeforeTServer(String topic,
							long queryStart, long queryBeforeT)
							throws SQLException, XPathExpressionException {
						// TODO Auto-generated method stub
						return null;
					}

					public List<Event> getFrameClientTime(String topic,
							long queryStart, long queryEnd)
							throws XPathExpressionException, SQLException {
						// TODO Auto-generated method stub
						return null;
					}

					public List<Event> getFrameServerTime(String topic,
							long queryStartT, long queryEnd)
							throws XPathExpressionException, SQLException {
						// TODO Auto-generated method stub
						return null;
					}
				};
			}

			public StateStoreIF getStateStore() {
				return new StateStoreIF(){

					public void deleteTopicAllStates(String topic)
							throws Exception {
					}

					public void deleteTopicState(String topic, String stateID)
							throws Exception {
					}

					public CacheObject getTopicAttribute(String topic,
							String stateID) throws Exception {
						return null;
					}

					public CacheObject getTopicState(String topic)
							throws Exception {
						return null;
					}

					public String[] getTopicStateNames(String topic)
							throws Exception {
						return null;
					}

					public List<String> getTopics() throws Exception {
						return null;
					}

					public void setTopicState(String topic,
							CacheObject topicState) throws Exception {
					}
				};
			}

			public SubscriberStoreIF getSubscriberStore() {
				return new SubscriberStoreIF(){

					public void addSubscriber(String subscriberId)
							throws Exception {
					}

					public void deleteSubscriber(String subscriberId)
							throws Exception {
					}

					public boolean existsSubscriber(String subscriberId)
							throws Exception {
						return true;
					}
				};
			}

			public TopicStoreIF getTopicStore() {
				return new TopicStoreIF(){

					public void addTopic(String topic) throws Exception {
					}

					public void addTopics(Set<String> topics) throws Exception {
					}

					public void deleteTopic(String topic) throws Exception {
					}

					public String getTopicByID(int id) throws Exception {
						return null;
					}

					public List<String> getTopicNames(String pattern, int limit)
							throws Exception {
						return null;
					}

					public boolean topicExists(String topic) throws Exception {
						return true;
					}
				};
			}

			public ContentStoreIF getContentStore() {
				return new ContentStoreIF() {

					public void deleteTopicAllContent(String topic)
							throws Exception {}

					public void deleteTopicContent(String topic,
							String contentID) throws Exception {
					}

					public boolean existsContent(String topic, String contentID)
							throws Exception {return false;	}

					public CacheElementIF getContent(String topics,
							String contentID) throws Exception {
						return null;}

					public CacheObject getTopicContent(String topic)
							throws Exception {return null;}

					public String[] getTopicContentNames(String topic)
							throws Exception {return null;}

					public void setTopicContent(String topic,
							CacheObject topicContent) throws Exception {}

					public void updateContentByID(String topic,
							CacheObject topicContent, String contentID)
							throws Exception {}
				};
			}
		};
		
		this.subscriber = new ServletSubscriberImpl(this.rpublisher, this.dbManager);
	}
	
	/**
	 * the singleton function to get the instance of the ContextProvider object
	 * 
	 * @return	the reference to the static ContextProvider singleton object
	 */
	public static ContextProvider getInstance (){
		
		if (contextProvider == null)
			synchronized (ContextProvider.class){
				if (contextProvider == null)
					contextProvider = new ContextProvider();
			}
		return contextProvider;
	}
	
	/**
	 * The initialization of the ContextProvider class to the proper publihser, dbManager, and subscriber
	 * 
	 * @param _publisher	The publisher to be used by servlets using the ContextProvider
	 * @param _dbManager	The dbManager to be used by servlets using the ContextProvider
	 * @param _subscriber	The subscriber to be used by servlets using the ContextProvider
	 */
	public void init(PublisherIF _lpublisher, RemotePublisherIF _rpublisher, DBManagerIF _dbManager, 
			SubscriberIF _subscriber, CacheIF _brokerCache){
		this.rpublisher  = _rpublisher;
		this.lpublisher = _lpublisher;
		this.dbManager  = _dbManager;
		this.subscriber = _subscriber;
		this.brokerCache = _brokerCache;
	}
	
	/**
	 * 
	 * @return	The reference to the RemotePublisherIF
	 */
	public RemotePublisherIF getRemotePublisher(){
		return this.rpublisher;
	}
	
	/**
	 * 
	 * @return	The reference to the PublisherIF
	 */
	public PublisherIF getPublisher(){
		return this.lpublisher;
	}
	
	/**
	 * 
	 * @return The reference to the SubscriberIF
	 */
	public SubscriberIF getSubscriber(){
		return this.subscriber;
	}
	
	/**
	 * 
	 * @return The reference to the DBManagerIF
	 */
	public DBManagerIF getDBManager(){
		return this.dbManager;
	}
	
	/**
	 * 
	 * @return
	 */
	public CacheIF getCache(){
		return this.brokerCache;
	}

}
