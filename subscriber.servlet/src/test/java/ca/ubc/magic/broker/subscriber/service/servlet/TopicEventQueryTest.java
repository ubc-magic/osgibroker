package ca.ubc.magic.broker.subscriber.service.servlet;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.httpclient.HttpStatus;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.cache.BrokerCache;
import ca.ubc.magic.broker.impl.PublisherImpl;
import ca.ubc.magic.broker.impl.SubscriberImpl;
import ca.ubc.magic.broker.storage.mysql.MySQLDBManager;
import ca.ubc.magic.broker.subscriber.service.servlet.helper.model.TopicEvent;


public class TopicEventQueryTest extends TestCase {
	
	private static ServletTester tester;
    private static String baseUrl;
    
    private DBManagerIF   dbManager;
    private PublisherImpl publisher;
    
    public static final long SLEEP_TIME = 10;
    public static final int  EVENTS_NUM_BENCHMARK1 = 10;
    public static final int  EVENTS_NUM_BENCHMARK2 = 25;
    public static final int  MAX_EVENTS_PUBLISHED  = 50;
    
    long currentTime;
	long startTime = currentTime = System.currentTimeMillis();
	long clientTimeBench1  = startTime;
	long clientTimeBench2  = startTime;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		dbManager  = new MySQLDBManager();
		
		publisher = new PublisherImpl();
		publisher.getDBManager(dbManager);
		
		
		
		ContextProvider.getInstance().init(
				publisher, 
				publisher, 
				dbManager, 
				new SubscriberImpl(publisher, dbManager){public void registerClients(String subscriberName) throws Exception {}}, 
				new BrokerCache());
		
		tester = new ServletTester();
        tester.setContextPath("/");
        tester.addServlet(TopicServlet.class, "/topic/*");
        tester.addServlet(EventsServlet.class, "/event/*");
        tester.addServlet(SubscribeServlet.class, "/subscribe/*");
        tester.addServlet(UnsubscribeServlet.class, "/unsubscribe/*");
        
        baseUrl = tester.createSocketConnector(true);
        tester.start();
        
        HttpTestHelper.subscribe(tester, baseUrl, "topic=testQueryTopic1&clientID=testQueryClient");
        publishEvents();
	}
	
	protected void tearDown() throws Exception {
		
		HttpTestHelper.unsubscribe(tester, baseUrl, "topic=testQueryTopic1&clientID=testQueryClient");
    	dbManager.getEventStore().deleteEvents("testQueryTopic1");
		
		super.tearDown();
		
		tester.stop();
		publisher.ungetDBManager(dbManager);
		
		publisher = null;
		dbManager = null;
	}
	
	public TopicEventQueryTest(String name){
		super(name);
	}
	
	public void noQueryStartBeforeE() throws Exception {
		
		HttpTester req = new HttpTester();
    	HttpTester res = new HttpTester();
    	
    	req.setMethod("GET");
    	req.setHeader("HOST", "tester");
    	req.setURI(baseUrl+"/topic/events?topic=testQueryTopic1&clientID=test2&beforeE="+EVENTS_NUM_BENCHMARK2);
    	
    	res.parse(tester.getResponses(req.generate()));
    	TopicEvent[] events  = HttpTestHelper.parseEvent(res.getContent());
    	Assert.assertEquals(events.length, EVENTS_NUM_BENCHMARK2);
	}
	
	public void noQueryStartAfterE() throws Exception {
		
		HttpTester req = new HttpTester();
    	HttpTester res = new HttpTester();
    	
    	req.setMethod("GET");
    	req.setHeader("HOST", "tester");
    	req.setURI(baseUrl+"/topic/events?topic=testQueryTopic1&clientID=test2&afterE="+EVENTS_NUM_BENCHMARK1);
    	
    	res.parse(tester.getResponses(req.generate()));
    	TopicEvent[] events = HttpTestHelper.parseEvent(res.getContent());
    	Assert.assertEquals(events.length, 0);
	}
	
	public void queryStartQueryEnd() throws Exception{
		
		HttpTester req = new HttpTester();
    	HttpTester res = new HttpTester();
    	
    	req.setMethod("GET");
    	req.setHeader("HOST", "tester");
    	req.setURI(baseUrl+"/topic/events?topic=testQueryTopic1&clientID=test2&start=1274069384272&end="+System.currentTimeMillis());
    	res.parse(tester.getResponses(req.generate()));
    	TopicEvent[] events = HttpTestHelper.parseEvent(res.getContent());
    	
    	Assert.assertEquals(events.length, MAX_EVENTS_PUBLISHED);
    	
	}
	
	public void clientPushEventQueryClient() throws Exception {
		
    	HttpTester res = new HttpTester();
    	
    	res = HttpTestHelper.queryEvent(tester, baseUrl, "topic=testQueryTopic1&clientID=testQueryClient&start=" + 
    			startTime + "&afterE="+EVENTS_NUM_BENCHMARK1);
    	TopicEvent[] topicEvent = HttpTestHelper.parseEvent(res.getContent());

    	Assert.assertEquals(topicEvent.length, EVENTS_NUM_BENCHMARK1); 
   	
    	Assert.assertEquals(Long.parseLong(topicEvent[0].getAttribute("timestamp")), startTime);
    	Assert.assertEquals(Long.parseLong(topicEvent[9].getAttribute("timestamp")), clientTimeBench1);
    	
	}
	
	private void publishEvents() throws Exception {
		
    	for (int i = 0; i < MAX_EVENTS_PUBLISHED; i++){
    		HttpTestHelper.postEvent(tester, baseUrl, "topic=testQueryTopic1&clientID=testQueryClient&name=nima"+i+"&family=kaviani"+i+"&timestamp="+currentTime);
    		Thread.sleep(TopicEventQueryTest.SLEEP_TIME);
    		
    		if (i == EVENTS_NUM_BENCHMARK1 - 1)
    			clientTimeBench1 = currentTime;
    		if (i == EVENTS_NUM_BENCHMARK2 - 1)
    			clientTimeBench2 = currentTime;
    		currentTime = System.currentTimeMillis();
    	}
	}
	
	public static Test suite(){
		TestSuite suite = new TestSuite();
		suite.addTest(new TopicEventQueryTest("noQueryStartBeforeE"));
		suite.addTest(new TopicEventQueryTest("noQueryStartAfterE"));
		suite.addTest(new TopicEventQueryTest("clientPushEventQueryClient"));
		return suite;
	}

}
