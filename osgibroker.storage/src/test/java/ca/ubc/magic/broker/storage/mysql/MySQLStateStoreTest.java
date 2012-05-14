package ca.ubc.magic.broker.storage.mysql;

import java.util.Map;
import java.util.TreeMap;

import ca.ubc.magic.broker.api.ds.Attribute;
import ca.ubc.magic.broker.api.ds.CacheObject;
import ca.ubc.magic.broker.api.ds.ElementMap;
import ca.ubc.magic.broker.api.ds.Attribute.Type;
import ca.ubc.magic.broker.api.storage.DBCreatorIF;
import ca.ubc.magic.broker.api.storage.StateStoreIF;
import ca.ubc.magic.broker.api.storage.TopicStoreIF;
import ca.ubc.magic.broker.storage.helper.Configuration;
import ca.ubc.magic.broker.storage.helper.ConnectionHandler;
import ca.ubc.magic.broker.storage.helper.ConnectionManager;
import ca.ubc.magic.broker.storage.helper.StatementManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MySQLStateStoreTest extends TestCase {
	
	private StateStoreIF stateStore = null;
	private TopicStoreIF topicStore = null;
	private DBCreatorIF  dbCreator  = null;
	private StatementManager stmtManager = null;
	private ConnectionManager connectionManager = null;
	
	public static final String MYSQL_SCRIPT_XML = "sql/mysql/mysql.xml";
	
	protected void setUp() throws Exception {
		super.setUp();
		
		connectionManager = new ConnectionManager(Configuration.getInstance());
		dbCreator = new MySQLDBCreator();
		dbCreator.createDB();
		
		stmtManager = new StatementManager(MYSQL_SCRIPT_XML);
		
		topicStore = new MySQLTopicStore(
				connectionManager.getDataSource(),
				stmtManager);
		
		stateStore = new MySQLStateStore(
				connectionManager.getDataSource(),
				stmtManager);
		
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		stmtManager = null;
		topicStore = null;
		connectionManager = null;
	}
	
	public MySQLStateStoreTest (String name){
		super (name);
	}
	
	public void testAddState() throws Exception {
		
		String topic = "testTopic";
		
		ElementMap attrMap = new ElementMap(topic);
		attrMap.setUntypedAttribute("a1", "v1");
		attrMap.setUntypedAttribute("a2", "v3");
		attrMap.setUntypedAttribute("a3", "v4");
		
		topicStore.addTopic(topic);
		stateStore.setTopicState(topic, attrMap);
		
		ElementMap a1State = (ElementMap) stateStore.getTopicAttribute(topic, "a1");
		Assert.assertEquals(a1State.getUntypedValue("a1"), attrMap.getUntypedValue("a1"));
		stateStore.deleteTopicAllStates(topic);
	}
	
	public static Test suite(){
		
		TestSuite suite = new TestSuite();
		suite.addTest(new MySQLStateStoreTest("testAddState"));
		return suite;
	}
}
