package ca.ubc.magic.broker.storage.mysql;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.storage.ClientStoreIF;
import ca.ubc.magic.broker.api.storage.ContentStoreIF;
import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.api.storage.EventStoreIF;
import ca.ubc.magic.broker.api.storage.StateStoreIF;
import ca.ubc.magic.broker.api.storage.SubscriberStoreIF;
import ca.ubc.magic.broker.api.storage.TopicStoreIF;
import ca.ubc.magic.broker.storage.helper.Configuration;
import ca.ubc.magic.broker.storage.helper.ConnectionManager;
import ca.ubc.magic.broker.storage.helper.StatementManager;

/**
 * Manages all the manipulations of the database
 * 
 * @author nima
 *
 */
public class MySQLDBManager implements DBManagerIF {
	
	// the type of DB that this DBManager supports
	public static final String MYSQL_DBTYPE = "mySQL";
	
	// the URL to the database on the DB server
	public static final String MYSQL_URL = 	Configuration.getInstance().getDbURL() + 
											Configuration.getInstance().getDBName();
	
	// the name for the DB received from the DB configuration object
	public static final String MYSQL_DB = Configuration.getInstance().getDBName();
	
	// the path to the sql statement configuration file 
	public static final String MYSQL_SCRIPT_XML = "sql/mysql/mysql.xml";
	
	private static final Logger logger = Logger.getLogger( MySQLDBManager.class );
	
	ConnectionManager connectionManager = null;
	StatementManager stmtManager = null;
	
	private TopicStoreIF 	mysqlTopicStore = null;
	private ClientStoreIF  mysqlClientStore = null;
	private EventStoreIF   mysqlEventStore = null;
	private SubscriberStoreIF mysqlSubscriberStore = null;
	private StateStoreIF mysqlStateStore = null;
	private ContentStoreIF mysqlContentStore = null;
	
	public MySQLDBManager (){
		
		try{
			
			connectionManager = new ConnectionManager(Configuration.getInstance());
			
			stmtManager = new StatementManager(MYSQL_SCRIPT_XML);
			
			new MySQLDBCreator().createDB();
			
			mysqlTopicStore = new MySQLTopicStore(
					connectionManager.getDataSource(), stmtManager);
			
			mysqlClientStore = new MySQLClientStore(
					connectionManager.getDataSource(), stmtManager);
			
			mysqlEventStore = new MySQLEventStore(
					connectionManager.getDataSource(), stmtManager);
			
			mysqlSubscriberStore = new MySQLSubscriberStore(
					connectionManager.getDataSource(), stmtManager);
			
			mysqlStateStore = new MySQLStateStore(
					connectionManager.getDataSource(), stmtManager);
			
			mysqlContentStore = new MySQLContentStore(
					connectionManager.getDataSource(), stmtManager);
			
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * @return the TopicStore for the database
	 */
	public TopicStoreIF getTopicStore() {
		return mysqlTopicStore;
	}

	/**
	 * @return the ClientStore for the database
	 */
	public ClientStoreIF getClientStore() {
		return mysqlClientStore;
	}

	/**
	 * @return the EventStore for the database
	 */
	public EventStoreIF getEventStore() {
		return mysqlEventStore;
	}

	/**
	 * @return the SubscriberStore for the database
	 */
	public SubscriberStoreIF getSubscriberStore() {
		return mysqlSubscriberStore;
	}
	
	/**
	 * 
	 * @return
	 */
	public StateStoreIF getStateStore(){
		return mysqlStateStore;
	}
	
	public ContentStoreIF getContentStore(){
		return mysqlContentStore;
	}
}
