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
import ca.ubc.magic.broker.api.ds.ElementMap;
import ca.ubc.magic.broker.api.storage.StateStoreIF;
import ca.ubc.magic.broker.storage.helper.StatementManager;

public class MySQLStateStore implements StateStoreIF{
	
	private StatementManager stmtManager= null;
	private DataSource ds = null;
	private static final Logger logger = Logger.getLogger( MySQLStateStore.class );
	
	public MySQLStateStore(DataSource _ds, StatementManager _stmtManager){
		
		this.ds  = _ds;
		this.stmtManager = _stmtManager;
	}
	
	public CacheObject getTopicAttribute(String topic, String stateID) throws Exception{
		
		CacheObject elementMap = null;
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getStateStoreStmt(StateStoreIF.GET_TOPIC_ATTR));
		stmt.setString(1, stateID);
		stmt.setString(2, topic);
		
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()){
			if (elementMap == null)
				elementMap = new ElementMap(topic);
			
			//TODO nimak -	The type for the attribute should be added  when creating a typed attribute
			Attribute attr = new Attribute(rs.getString("name"), rs.getString("value"));
			((ElementMap)elementMap).setAttribute(attr.getName(), attr);
		}
		
		rs.close();
		stmt.close();
		connection.close();
		return elementMap;
	}

	public CacheObject getTopicState(String topic) throws Exception {
		
		CacheObject topicState = null;
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getStateStoreStmt(StateStoreIF.GET_TOPIC_STATE));
		stmt.setString(1, topic);
		
		ResultSet rs = stmt.executeQuery();
		
		while(rs.next()){
			if (topicState == null)
				topicState = new ElementMap(topic);
			((ElementMap) topicState).setUntypedAttribute(rs.getString("name"), rs.getString("value"), false);
		}
		
		rs.close();
		stmt.close();
		connection.close();
		return topicState;
	}

	public String[] getTopicStateNames(String topic) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getTopics() throws Exception{
		
		List<String> topicsList = null;
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getStateStoreStmt(StateStoreIF.GET_TOPIC));
		
		ResultSet rs = stmt.executeQuery();
		
		while(rs.next()){
			if (topicsList == null)
				topicsList = new ArrayList<String>();
			topicsList.add(rs.getString("name"));
		}
		
		rs.close();
		stmt.close();
		connection.close();
		return topicsList;
	}

	@SuppressWarnings("unchecked")
	public void setTopicState(String topic, CacheObject attrMap) throws Exception{
		
		String[] attrNames = attrMap.getElementAttributeNames(false);
		for (String attrName : attrNames){
			CacheElementIF<Attribute> attrStateList = attrMap.getElement(attrName, false);
			
			Connection connection = ds.getConnection();
			PreparedStatement stmt = null;
			
			if (attrStateList.getCacheElemStatus() == Status.added){
				stmt = connection.prepareStatement(stmtManager.getStateStoreStmt(StateStoreIF.SET_TOPIC_STATE));
				storeStateList(topic, attrStateList, connection, stmt, Status.added);
			}
			else if (attrStateList.getCacheElemStatus() == Status.deleted){
				stmt = connection.prepareStatement(stmtManager.getStateStoreStmt(StateStoreIF.DELETE_TOPIC_STATE));
				storeStateList(topic, attrStateList, connection, stmt, Status.deleted);
			}
			else if (attrStateList.getCacheElemStatus() == Status.updated){
				
				stmt = connection.prepareStatement(stmtManager.getStateStoreStmt(StateStoreIF.DELETE_TOPIC_STATE));
				storeStateList(topic, attrStateList, connection, stmt, Status.deleted);
				stmt = connection.prepareStatement(stmtManager.getStateStoreStmt(StateStoreIF.SET_TOPIC_STATE));
				storeStateList(topic, attrStateList, connection, stmt, Status.added);
			}
			
			if (attrStateList.getCacheElemStatus() == Status.added || attrStateList.getCacheElemStatus() == Status.updated)
				attrStateList.setCacheElemStatus(Status.intact);
			
			if (stmt != null) stmt.close();
			if (connection != null) connection.close();
		}
	}
		
	private void storeStateList(String topic, CacheElementIF<Attribute> attrStateList,
			Connection connection, PreparedStatement stmt, Status storeStateStatus) throws Exception {
		
		
		connection.setAutoCommit(false);
		
		Iterator<Attribute> itr = attrStateList.getList().iterator();
		int attrIndex = 0;
		while (itr.hasNext()){
			
			Attribute attr = itr.next();
			
			if (storeStateStatus == Status.added){
				logger.debug("Trying to add the existing records in the DataStore");
				addAttr(attrIndex++, attr, topic, stmt);
			}
			else if (storeStateStatus == Status.deleted){
				logger.debug("Trying to delete records from the DataStore");
				deleteAttr(attr, topic, stmt);
			}
			else
				logger.warn("StateStorage failed for state: " + attr.getName() + "in topic: " + topic);
		}
		connection.commit();
		connection.setAutoCommit(true);
	}
	
	private int addAttr(int index, Attribute attr, String topic, PreparedStatement stmt) throws Exception {
		
		stmt.setInt(1, index);
		stmt.setString(2, attr.getName());
		stmt.setString(3, attr.getType().toString());
		stmt.setString(4, attr.getValue());
		stmt.setString(5, topic);
		return stmt.executeUpdate();
	}
	
	private int deleteAttr(Attribute attr, String topic, PreparedStatement stmt) throws Exception {
		stmt.setString(1, attr.getName());
		stmt.setString(2, topic);
		return stmt.executeUpdate();
	}
	
	public void deleteTopicAllStates(String topic) throws Exception {
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getStateStoreStmt(StateStoreIF.DELETE_TOPIC_ALL_STATES));
		stmt.setString(1, topic);
		stmt.execute();
		stmt.close();
		connection.close();
	}
	
	public void deleteTopicState(String topic, String stateID) throws Exception{
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getStateStoreStmt(StateStoreIF.DELETE_TOPIC_STATE));
		stmt.setString(1, topic);
		stmt.setString(2, stateID);
		stmt.execute();
		stmt.close();
		connection.close();
	}
}
