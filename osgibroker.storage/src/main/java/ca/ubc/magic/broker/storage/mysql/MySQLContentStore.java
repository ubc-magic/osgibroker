package ca.ubc.magic.broker.storage.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.cache.CacheElementIF;
import ca.ubc.magic.broker.api.cache.CacheElementIF.Status;
import ca.ubc.magic.broker.api.ds.Attribute;
import ca.ubc.magic.broker.api.ds.CacheObject;
import ca.ubc.magic.broker.api.ds.ContentObjElement;
import ca.ubc.magic.broker.api.ds.TopicContent;
import ca.ubc.magic.broker.api.storage.ContentStoreIF;
//import ca.ubc.magic.broker.ds.ContentObjElement;
import ca.ubc.magic.broker.storage.helper.StatementManager;

public class MySQLContentStore implements ContentStoreIF {
	
	private StatementManager stmtManager= null;
	private DataSource ds = null;
	private static final Logger logger = Logger.getLogger( MySQLContentStore.class );
	
	public MySQLContentStore (DataSource _ds, StatementManager _stmtManager){
		this.ds = _ds;
		this.stmtManager = _stmtManager;
	}

	public void deleteTopicAllContent(String topic) throws Exception {
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getContentStoreStmt(ContentStoreIF.DELETE_TOPIC_ALL_CONTENT));
		stmt.setString(1, topic);
		stmt.execute();
		stmt.close();
		connection.close();
		
		logger.debug("delete all content for topic [" + topic + "]");
		
	}

	public void deleteTopicContent(String topic, String contentID) throws Exception {
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getContentStoreStmt(ContentStoreIF.DELETE_TOPIC_CONTENT));
		stmt.setString(1, contentID);
		stmt.setString(2, topic);
		stmt.execute();
		stmt.close();
		connection.close();
		
		logger.debug("delete content [" + contentID + "] for topic [" + topic + "]");
	}

	public CacheObject getTopicContent(String topic) throws Exception {
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getContentStoreStmt(ContentStoreIF.GET_TOPIC_CONTENT));
		stmt.setString(1, topic);
		
		TopicContent topicContent = new TopicContent(topic);
		
		ResultSet rs = stmt.executeQuery();
		while (rs.next()){
			String contentID = rs.getString("content_id");
			
			CacheElementIF<Attribute> content = topicContent.getContent(contentID); 
			if (content == null){
				content = new ContentObjElement<Attribute>();
				topicContent.setElement(contentID, content);
			}
			
			content.add(new Attribute(rs.getString("name"), rs.getString("value")));
		}
		
		rs.close();
		stmt.close();
		connection.close();
		
		logger.debug("topic content was taken from DB for topic [" + topic + "]");
		
		return topicContent;
	}

	public String[] getTopicContentNames(String topic) throws Exception {
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getContentStoreStmt(ContentStoreIF.GET_TOPIC_CONTENT_NAMES));
		stmt.setString(1, topic);
		
		List<String> contentNames = new ArrayList<String>();
		ResultSet rs = stmt.executeQuery();
		while (rs.next()){
			contentNames.add(rs.getString("content_id"));
		}
		
		rs.close();
		stmt.close();
		connection.close();
		
		logger.debug("topic content names were taken from DB for topic [" + topic + "]");
		
		return contentNames.toArray(new String[0]);
	}

	@SuppressWarnings("unchecked")
	public void setTopicContent(String topic, CacheObject topicContent)	throws Exception {
		
		String[] attrNames = topicContent.getElementAttributeNames(false);
		
		for (String attrName : attrNames){
			
			Connection connection = ds.getConnection();
			PreparedStatement stmt = null;
			CacheElementIF<Attribute> attrContentList = topicContent.getElement(attrName, false);
			
			if (attrContentList.getCacheElemStatus() == Status.added){
				stmt = connection.prepareStatement(stmtManager.getContentStoreStmt(ContentStoreIF.SET_TOPIC_CONTENT));
				storeContentList(topic, attrName, attrContentList, connection, stmt, Status.added);
			}
			else if (attrContentList.getCacheElemStatus() == Status.deleted){
				stmt = connection.prepareStatement(stmtManager.getContentStoreStmt(ContentStoreIF.DELETE_TOPIC_CONTENT));
				storeContentList(topic, attrName, attrContentList, connection, stmt, Status.deleted);
			}
			else if (attrContentList.getCacheElemStatus() == Status.updated){
				
				stmt = connection.prepareStatement(stmtManager.getContentStoreStmt(ContentStoreIF.DELETE_TOPIC_CONTENT));
				storeContentList(topic, attrName, attrContentList, connection, stmt, Status.deleted);
				stmt = connection.prepareStatement(stmtManager.getContentStoreStmt(ContentStoreIF.SET_TOPIC_CONTENT));
				storeContentList(topic, attrName, attrContentList, connection, stmt, Status.added);
			}
			
			if (attrContentList.getCacheElemStatus() == Status.added || attrContentList.getCacheElemStatus() == Status.updated)
				attrContentList.setCacheElemStatus(Status.intact);
			
			if (stmt != null) stmt.close();
			if (connection != null) connection.close();
		}
		
		logger.debug("topic content added to the DB for topic [" + topic + "]");
	}
	
	private void storeContentList(String topic, String contentID, CacheElementIF<Attribute> attrContentList,
			Connection connection, PreparedStatement stmt, Status storeContentStatus) throws Exception {
		
		if (existsContent(topic, contentID) && storeContentStatus == Status.added){
			logger.debug("ContentID (" + contentID + ") is already registered with the topic: " + topic);
			return;
		}
		
		connection.setAutoCommit(false);
		
		Iterator<Attribute> itr = attrContentList.getList().iterator();
		while (itr.hasNext()){
			
			Attribute attr = itr.next();
			
			if (storeContentStatus == Status.added){
				//checks to see whether the content is already in the database;
				logger.debug("Trying to add the existing records in the DataStore");
				addAttr(contentID, attr, topic, stmt);
			}
			else if (storeContentStatus == Status.deleted){
				logger.debug("Trying to delete records from the DataStore");
				deleteAttr(contentID, topic, stmt);
			}
			else
				logger.warn("StateStorage failed for state: " + attr.getName() + "in topic: " + topic);
		}
		connection.commit();
		connection.setAutoCommit(true);
		logger.debug("content [" + contentID + "] stored for topic [" + topic + "]");
	}
	
	private int addAttr(String contentID, Attribute attr, String topic, PreparedStatement stmt) throws Exception {
		
		stmt.setString(1, contentID);
		stmt.setString(2, attr.getName());
		stmt.setString(3, attr.getValue());
		stmt.setString(4, topic);
		return stmt.executeUpdate();
	}
	
	private int deleteAttr(String contentID, String topic, PreparedStatement stmt) throws Exception {
		stmt.setString(1, contentID);
		stmt.setString(2, topic);
		return stmt.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public boolean existsContent(String topic, String contentID) throws Exception {
		CacheElementIF content = getContent(topic, contentID);
		return (content.getList().size() > 0) ? true : false;
	}

	@SuppressWarnings("unchecked")
	public CacheElementIF getContent(String topics, String contentID) throws Exception {
		
		CacheElementIF content = new ContentObjElement();
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getContentStoreStmt(ContentStoreIF.GET_TOPIC_CONTENT_BY_ID));
		stmt.setString(1, contentID);
		stmt.setString(2, topics);
		
		ResultSet rs = stmt.executeQuery();
		
		List<Attribute> contentAttrs = new ArrayList<Attribute>();
		
		while (rs.next())
			contentAttrs.add(new Attribute (rs.getString("name"), rs.getString("value")));
		
		content.setList(contentAttrs);
		
		rs.close();
		stmt.close();
		connection.close();
		
		logger.debug("content [" + contentID + "] taken from DB for topic [" + topics + "]");
		
		return content;
	}

	@SuppressWarnings("unchecked")
	public void updateContentByID(String topic, CacheObject topicContent, String contentID) throws Exception {
		
		Connection connection = ds.getConnection();
		connection.setAutoCommit(false);
		
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getContentStoreStmt(ContentStoreIF.UPDATE_CONTENT_BY_ID));
		CacheElementIF<Attribute> content = (CacheElementIF<Attribute>) topicContent.getElement(contentID);
		
		Attribute[] contentAttrs = content.getList().toArray(new Attribute[0]);
		
		for (Attribute attr : contentAttrs){
			stmt.setString(3, attr.getValue());
			stmt.setString(1, contentID);
			stmt.setString(2, attr.getName());
			stmt.setString(4, topic);
			stmt.executeUpdate();
		}
		
		connection.commit();
		connection.setAutoCommit(true);
		stmt.close();
		connection.close();
		logger.debug("content [" + contentID + "] updated for topic [" + topic + "]");
	}

}
