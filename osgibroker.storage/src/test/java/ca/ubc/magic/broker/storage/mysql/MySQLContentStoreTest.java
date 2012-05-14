package ca.ubc.magic.broker.storage.mysql;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.cache.CacheElementIF;
import ca.ubc.magic.broker.api.cache.CacheElementIF.Status;
import ca.ubc.magic.broker.api.ds.Attribute;
import ca.ubc.magic.broker.api.ds.ContentObjElement;
import ca.ubc.magic.broker.api.ds.TopicContent;
import ca.ubc.magic.broker.api.storage.ContentStoreIF;
import ca.ubc.magic.broker.api.storage.DBCreatorIF;
import ca.ubc.magic.broker.api.storage.TopicStoreIF;
import ca.ubc.magic.broker.storage.helper.Configuration;
import ca.ubc.magic.broker.storage.helper.ConnectionHandler;
import ca.ubc.magic.broker.storage.helper.ConnectionManager;
import ca.ubc.magic.broker.storage.helper.StatementManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MySQLContentStoreTest extends TestCase {
	
	private TopicStoreIF topicStore = null;
	private ContentStoreIF contentStore = null;
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
		
		contentStore = new MySQLContentStore(
				connectionManager.getDataSource(),
				stmtManager);
		
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		stmtManager = null;
		topicStore = null;
		contentStore = null;
		connectionManager = null;
	}
	
	public MySQLContentStoreTest(String name){
		super(name);
	}
	
	@SuppressWarnings("unchecked")
	public void testContentStore() throws Exception {
		
		String topic = "testTopic";
		topicStore.addTopic(topic);
		
		
		String[] contentNames = {"content1", "content2"};
		TopicContent topicContent = new TopicContent(topic);
		
		List<Attribute> contentAttrs = new ArrayList<Attribute>();
		contentAttrs.add(new Attribute(TopicContent.FILE_NAME, "testFile.pdf"));
		contentAttrs.add(new Attribute(TopicContent.FILE_SIZE, "1233"));
		contentAttrs.add(new Attribute(TopicContent.FILE_CONTENT_TYPE, "text/xml"));
		contentAttrs.add(new Attribute(ContentObjElement.URL, "http://localhost:8800/testFile.pdf"));
		contentAttrs.add(new Attribute(RemoteClientIF.CLIENT_ID, "testClient1"));
		contentAttrs.add(new Attribute(SubscriberIF.TOPIC, topic));

		CacheElementIF content1 = new ContentObjElement();
		content1.setList(contentAttrs);
		content1.setCacheElemStatus(Status.added);
		
		topicContent.setElement(contentNames[0], content1);
		
		List<Attribute> contentAttrs2 = new ArrayList<Attribute>();
		contentAttrs2.add(new Attribute(TopicContent.FILE_NAME, "testFile.doc"));
		contentAttrs2.add(new Attribute(TopicContent.FILE_SIZE, "4555"));
		contentAttrs2.add(new Attribute(TopicContent.FILE_CONTENT_TYPE, "application/xml"));
		contentAttrs2.add(new Attribute(ContentObjElement.URL, "http://localhost:8800/testFile.doc"));
		contentAttrs2.add(new Attribute(RemoteClientIF.CLIENT_ID, "testClient2"));
		contentAttrs2.add(new Attribute(SubscriberIF.TOPIC, topic));
		
		CacheElementIF content2 = new ContentObjElement();
		content2.setList(contentAttrs2);
		content2.setCacheElemStatus(Status.added);
		
		topicContent.setElement(contentNames[1], content2);
		
		contentStore.setTopicContent(topic, topicContent);
		
		TopicContent retrievedTopicContent = (TopicContent) contentStore.getTopicContent(topic);
//		Assert.assertEquals(topicContent, retrievedTopicContent);
		
		String[] retrievedContentNames = contentStore.getTopicContentNames(topic);
//		Assert.assertEquals(contentNames[1], retrievedContentNames[0]);
//		Assert.assertEquals(contentNames[0], retrievedContentNames[1]);
		
		content2.setCacheElemStatus(Status.deleted);
		contentStore.setTopicContent(topic, topicContent);
		
		contentStore.deleteTopicAllContent(topic);
		
		topicStore.deleteTopic(topic);
	}
	
	public static Test suite(){
		
		TestSuite suite = new TestSuite();
		suite.addTest(new MySQLContentStoreTest("testContentStore"));
		return suite;
	}

}
