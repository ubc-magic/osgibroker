package ca.ubc.magic.broker.api.storage;

/**
 * The Manager for the database to receive a reference to a TopicStore, a ClientStore, and EventStore, or a SubscriberStore
 * 
 * @author nima
 *
 */
public interface DBManagerIF {
	
	// static final variables to define the type, the url, and the name for the DB that the current manager is managing.
	// the values are used in particular to define OSGi properties once a dbManager service is registered
	public static final String DB_TYPE = "dbType";
	public static final String DB_URL = "dbURL";
	public static final String DB_NAME = "dbName";
	
	/**
	 * 
	 * @return	the TopicStore refrence for the database
	 */
	public TopicStoreIF getTopicStore();
	
	/**
	 * 
	 * @return	the ClientStore reference for the database
	 */
	public ClientStoreIF getClientStore();
	
	/**
	 * 
	 * @return	the EventStore reference for the database
	 */
	public EventStoreIF  getEventStore();
	
	/**
	 * 
	 * @return	the SubscriberStore reference for the databases
	 */
	public SubscriberStoreIF getSubscriberStore();
	
	/**
	 * 
	 * @return the StateStore reference for the database
	 */
	public StateStoreIF getStateStore();
	
	/**
	 * 
	 * @return the ContentStore reference for the database
	 */
	public ContentStoreIF getContentStore();
}
