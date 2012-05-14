package ca.ubc.magic.broker.storage.mysql;

import java.util.List;

import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.storage.DBCreatorIF;
import ca.ubc.magic.broker.api.storage.EventStoreIF;
import ca.ubc.magic.broker.api.storage.TopicStoreIF;
import ca.ubc.magic.broker.storage.helper.Configuration;
import ca.ubc.magic.broker.storage.helper.ConnectionHandler;
import ca.ubc.magic.broker.storage.helper.ConnectionManager;
import ca.ubc.magic.broker.storage.helper.StatementManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MySQLTopicStoreTest extends TestCase {

	private EventStoreIF eventStore = null;
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

		eventStore = new MySQLEventStore(
				connectionManager.getDataSource(),
				stmtManager);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		stmtManager = null;
		eventStore  = null;
		topicStore = null;
		connectionManager = null;
	}

	public MySQLTopicStoreTest (String name){
		super (name);
	}

	public void testAddEvent() throws Exception{

		Event testEvt1 = new Event();
		testEvt1.addAttribute("testName1", "testValue1-1");

		Event testEvt2 = new Event();
		testEvt2.addAttribute("testName2", "testValue2-1");
		testEvt2.addAttribute("testName3", "testValue2-2");

		Event testEvt3 = new Event();
		testEvt3.addAttribute("testName1", "testValue1-1");

		topicStore.addTopic("testChannel");

		Assert.assertTrue(topicStore.topicExists("testChannel"));

		eventStore.addEvent("testChannel", testEvt1);
		eventStore.addEvent("testChannel", testEvt2);
		eventStore.addEvent("testChannel", testEvt3);
		Thread.currentThread().sleep(100);

		List<Event> evtList = eventStore.getLastEvents("testChannel", 2);

		Assert.assertEquals(testEvt3.toString(), ((Event)evtList.get(1)).toString());
		Assert.assertEquals(testEvt2.toString(), ((Event)evtList.get(0)).toString());
		Assert.assertNotSame(testEvt1.toString(), ((Event)evtList.get(0)).toString());

		eventStore.deleteEvents("testChannel");
		Assert.assertNull(eventStore.getLastEvents("testChannel"));

		topicStore.deleteTopic("testChannel");
		Assert.assertFalse(topicStore.topicExists("testChannel"));
	}

	public static Test suite(){

		TestSuite suite = new TestSuite();
		suite.addTest(new MySQLTopicStoreTest("testAddEvent"));
		return suite;

	}

}
