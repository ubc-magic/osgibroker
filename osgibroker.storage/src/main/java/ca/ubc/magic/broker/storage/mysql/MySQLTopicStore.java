package ca.ubc.magic.broker.storage.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;


import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.storage.TopicStoreIF;
import ca.ubc.magic.broker.storage.helper.StatementManager;

/**
 * 
 * 
 * @author nima
 *
 */
public class MySQLTopicStore implements TopicStoreIF {
	
	private DataSource ds = null;
	private StatementManager stmtManager = null;
	private static final Logger logger = Logger.getLogger( MySQLTopicStore.class );
	
	public MySQLTopicStore (DataSource _ds, StatementManager _stmtManager){
		
		this.ds  = _ds;
		this.stmtManager = _stmtManager;
		
	}
	
	/**
	 * adds a topic to the DB
	 */
	public void addTopic(String topic) throws Exception{
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt;
		stmt = connection.prepareStatement(stmtManager.getTopicStoreStmt(TopicStoreIF.ADD_TOPIC));
		stmt.setString(1, topic);
		
		try{
			stmt.executeUpdate();
		}catch (Exception e){
			logger.debug(e.getMessage());
			logger.debug("Adding topic: " + topic + "failed. Topic might be already available");
		}
		
		stmt.close();
		connection.close();
		logger.debug("Topic [" + topic + "] is added to the DB");
	}

	/**
	 * checks whether a topic exists in a DB
	 */
	public boolean topicExists(String topic) throws Exception {
		
		boolean topicExists = false;
		
		PreparedStatement stmt;
			
		Connection connection = ds.getConnection();
		stmt = connection.prepareStatement(stmtManager.getTopicStoreStmt(TopicStoreIF.EXIST_TOPIC));
		stmt.setString(1, topic);
		ResultSet rs = stmt.executeQuery();
		
		if (rs.next()){
			topicExists = true;
			logger.info("Topic [" + topic + "] is available in the DB");
		}else
			logger.info("Topic [" + topic + "] is not available in the DB");
		
		rs.close();
		stmt.close();
		connection.close();
			
		return topicExists;
	}

	/**
	 * deletes a topic from the DB
	 */
	public void deleteTopic(String topic) throws Exception {
		
		if (!topicExists(topic))
			return;
		
		PreparedStatement stmt;
		Connection connection = ds.getConnection();
		stmt = connection.prepareStatement(stmtManager.getTopicStoreStmt(TopicStoreIF.DELETE_TOPIC));
		stmt.setString(1, topic);
		stmt.executeUpdate();
		
		stmt.close();
		connection.close();
		logger.info("Topic [" + topic + "] is deleted from the DB");
	}
	
	/**
	 * gets the list of all topics following the <i>pattern</i> from the DB 
	 */
	public List<String> getTopicNames(String pattern, int limit) throws Exception {
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt;
		List<String> topicNames = null;
		
		topicNames = new ArrayList<String>();
		stmt = connection.prepareStatement(stmtManager.getTopicStoreStmt(TopicStoreIF.GET_TOPIC_NAMES));
		stmt.setString(1, pattern);
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()){
			topicNames.add(rs.getString("name"));
		}
		
		rs.close();
		stmt.close();
		connection.close();
		
		logger.debug("executing getTopicNames done!");
		return topicNames;
	}
	
	public String getTopicByID (int id) throws Exception {
		
		String topicName = null;
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = 
			connection.prepareStatement(stmtManager.getTopicStoreStmt(TopicStoreIF.GET_TOPIC_BY_ID));
		stmt.setInt(1, id);
		ResultSet rs = stmt.executeQuery();
		
		if (rs.next()){
			topicName = rs.getString("name");
		}
		
		rs.close();
		stmt.close();
		connection.close();
		logger.debug("executing getTopicByID done!");
		return topicName;
	}

	public void addTopics(Set<String> topics) throws Exception {
		
//		Iterator<String> it = topics.iterator();
//		while (it.hasNext()){
//			String topic = (String) it.next();
//			try {
//				addTopic(topic);
//			}catch (Exception e){
//				logger.error("Topic insertion into the DataStore failed for topic: " + topic);
//			}
//		}
		Connection connection = ds.getConnection();
		connection.setAutoCommit(false);
		Iterator<String> it = topics.iterator();
		while (it.hasNext()){
			String topic = (String) it.next();
			PreparedStatement stmt = connection.prepareStatement(
					stmtManager.getTopicStoreStmt(TopicStoreIF.ADD_TOPIC));
			stmt.setString(1, topic);
			try {
				stmt.executeUpdate();
			}catch (Exception e){
				logger.error("Topic insertion into the DataStore failed for topic: " + topic);
			}finally{
				stmt.close();
			}
		}
		logger.debug("topics added to the DB");
		connection.commit();
		connection.setAutoCommit(true);
		connection.close();
	}
	
}
