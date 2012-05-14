package ca.ubc.magic.broker.storage.mysql;

import java.util.List;

import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.storage.DBCreatorIF;
import ca.ubc.magic.broker.api.storage.EventStoreIF;
import ca.ubc.magic.broker.api.storage.TopicStoreIF;
import ca.ubc.magic.broker.storage.helper.Configuration;
import ca.ubc.magic.broker.storage.helper.ConnectionManager;
import ca.ubc.magic.broker.storage.helper.StatementManager;
import junit.framework.Assert;
import junit.framework.TestCase;

public class MySQLEventQueryTest extends TestCase {
	
	private EventStoreIF eventStore = null;
	private TopicStoreIF topicStore = null;
	private DBCreatorIF  dbCreator  = null;
	private StatementManager stmtManager = null;
	private ConnectionManager connectionManager = null;
	
	private Event attributeArray[] = null;
	private long  indexTimeStamp   = -1;
	
	private static final String TEST_QUERY_TOPIC = "testQueryTopic";
	
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
		
		attributeArray = new Event[100];
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		stmtManager = null;
		eventStore  = null;
		topicStore = null;
		connectionManager = null;
	}

	public MySQLEventQueryTest (String name){
		super (name);
	}
	
	private void createTests() throws Exception {
		
		topicStore.addTopic(MySQLEventQueryTest.TEST_QUERY_TOPIC);
		indexTimeStamp = System.currentTimeMillis();
		
		for (int i = 0; i < 50; i++){
			
			int j = i * 2;
			Event evt = new Event();
			evt.addAttribute(Event.CLIENT_EVENT_TIMESTAMP, Long.toString(Long.parseLong("1272485558533") + i));
			evt.addAttribute("name", "nima"+Integer.toString(j));
			evt.addAttribute("family", "k"+Integer.toString(j));
			attributeArray[j]  = evt; 
			
			j++;
			evt = new Event();
			evt.addAttribute(Event.CLIENT_EVENT_TIMESTAMP, Long.toString(Long.parseLong("1272485558533") - i));
			evt.addAttribute("name", "kaviani"+Integer.toString(j));
			evt.addAttribute("family", "k"+Integer.toString(j));
			attributeArray[j]  = evt; 
		}
		
		for (int i = 0; i < 100; i++)
			eventStore.addEvent(MySQLEventQueryTest.TEST_QUERY_TOPIC, attributeArray[i]);
	}
	
	private void deleteTests() throws Exception {
		
		for (int i = 0; i < 100; i++)
			eventStore.deleteEvents(MySQLEventQueryTest.TEST_QUERY_TOPIC);
		
		topicStore.deleteTopic(MySQLEventQueryTest.TEST_QUERY_TOPIC);
	}
	
	
	public void testQueryBeforeE() throws Exception {
		
		deleteTests();
		createTests();
		
		List<Event> eventList = null;
		
		long startindex = 1272485558533L;
		
		eventList = eventStore.getAfterEClient(MySQLEventQueryTest.TEST_QUERY_TOPIC, startindex, 20);
		eventList = eventStore.getBeforeEClient(MySQLEventQueryTest.TEST_QUERY_TOPIC, startindex, 20);
		
		eventList = eventStore.getBeforeEServer(MySQLEventQueryTest.TEST_QUERY_TOPIC, System.currentTimeMillis(), 50);
		Assert.assertEquals(eventList.size(), 50);
		
		List<Event> eventList1 = eventStore.getBeforeTServer(MySQLEventQueryTest.TEST_QUERY_TOPIC, System.currentTimeMillis(), System.currentTimeMillis() - indexTimeStamp);
		List<Event> eventList2 = eventStore.getAfterTServer(MySQLEventQueryTest.TEST_QUERY_TOPIC, indexTimeStamp, System.currentTimeMillis() - indexTimeStamp);
		
		Assert.assertEquals(eventList1.size(), eventList2.size());
		
		eventList = eventStore.getFrameServerTime(TEST_QUERY_TOPIC, indexTimeStamp, System.currentTimeMillis());
		
		deleteTests();
	}

}
