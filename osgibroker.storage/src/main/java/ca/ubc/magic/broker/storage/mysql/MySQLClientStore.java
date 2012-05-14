package ca.ubc.magic.broker.storage.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.ds.Client;
import ca.ubc.magic.broker.api.notification.NotificationHelper;
import ca.ubc.magic.broker.api.storage.ClientStoreIF;
import ca.ubc.magic.broker.api.storage.TopicStoreIF;
import ca.ubc.magic.broker.storage.helper.StatementManager;

/**
 * 
 * @author nima
 *
 */
public class MySQLClientStore implements ClientStoreIF {
	
	private StatementManager stmtManager= null;
	private DataSource ds = null;
	private static final Logger logger = Logger.getLogger( MySQLClientStore.class );
	
	public MySQLClientStore(DataSource _ds, StatementManager _stmtManager){
		
		this.ds  = _ds;
		this.stmtManager = _stmtManager;
	}
	
	/**
	 * adds a client to the DataBase
	 */
	public synchronized int addClient(String topic, String subscriberName, String clientType, String clientID) throws Exception {
		
		PreparedStatement stmt;
		Connection connection = ds.getConnection();
		stmt = connection.prepareStatement(stmtManager.getClientStoreStmt(ClientStoreIF.ADD_CLIENT), 
				Statement.RETURN_GENERATED_KEYS);
		
		stmt.setString(1, topic);
		stmt.setString(2, subscriberName);
		stmt.setString(3, clientType);
		stmt.setString(4, clientID);
		stmt.setString(5, null);
		stmt.setString(6, null);
		
		try{
			stmt.execute();
		}catch (Exception e){
			throw new BrokerException(BrokerException.CONFLICT_CLIENT_DB + ": " + clientID);
		}
		
		int clientDbID = 0;
		ResultSet rs = stmt.getGeneratedKeys();
		if (rs.next()) 
			clientDbID = rs.getInt(1);
		else
			throw new BrokerException(BrokerException.CONFLICT_CLIENT_DB + ": " + clientID);
		
		stmt.close();
		connection.close();
		logger.debug("Client [" + clientID + "] type [" + clientType + "] is added to topic [" + topic + "]");
		
		return clientDbID;
	}
	
	/**
	 * 
	 */
	public  void addClient(String topic, String subscriberName, RemoteClientIF client) throws Exception {
		int clientDbID = this.addClient(topic, subscriberName, (String) client.getProperty(RemoteClientIF.CLIENT_TYPE), 
										(String) client.getProperty(RemoteClientIF.CLIENT_ID));
		
		Set<String> propKeys = client.getPropertyNames();
		
		Iterator<String> it = propKeys.iterator();
		while (it.hasNext()){
			String propKey = (String) it.next();
			this.addClientProps(String.valueOf(clientDbID), propKey, (String) client.getProperty(propKey));
		}
		
	}

	/**
	 * returns the list of all the clients registered under a topic. Returns the IDs for the clients
	 */
	public  List<Client> getClientsByTopic(String topic) throws Exception {
		logger.debug("getClientsByTopic");
		return getClients(topic, ClientStoreIF.GET_CLIENTS_BY_TOPIC);
	}
	
	public  List<Client> getClientsBySubscription(String subscription) throws Exception {
		logger.debug("getClientsBySubscription");
		return getClients(subscription, ClientStoreIF.GET_CLIENTS_BY_SUBSCRIPTION);
	}
	
	private  List<Client> getClients (String paramName, String queryName) throws Exception {
		
		logger.debug("getClient paramName[" + paramName +"] query [" + queryName + "]");
		
		PreparedStatement stmt  = null;
		PreparedStatement stmt2 = null;
		List<Client> clients = new ArrayList<Client>();
		Connection connection = ds.getConnection();
		stmt = connection.prepareStatement(stmtManager.getClientStoreStmt(queryName));
		
		
		stmt.setString(1, paramName);
		
		ResultSet rs  = stmt.executeQuery();
		ResultSet rs2 = null;
		
		while (rs.next()){
			
			Client client = new Client();
			
			int  clientDbID = rs.getInt("id");
			int  topicID    = rs.getInt("topic_id");
			String topic = null;
			
			stmt2 = connection.prepareStatement(stmtManager.getTopicStoreStmt(TopicStoreIF.GET_TOPIC_BY_ID));
			stmt2.setInt(1, topicID);
			
			rs2 = stmt2.executeQuery();
			if (rs2.next())
				topic = rs2.getString("name");
			
			HashMap<String, String> clientProps = getClientProps(clientDbID);
			
			if (clientProps.keySet() == null){
				logger.error("No properties found for client with the DB ID: " + clientDbID);
				continue;
			}
			
			for (String key : clientProps.keySet())
				client.putProperty(key, clientProps.get(key));
			
			if (client.isExpired() && topic != null){
				this.deleteClient(topic, (String) client.getProperty(RemoteClientIF.CLIENT_ID));
				this.deleteClient(NotificationHelper.getNotificationTopic(topic), (String) client.getProperty(RemoteClientIF.CLIENT_ID));
			}else
				clients.add(client);
		}
		if (stmt2 != null){
			stmt2.close();
			rs2.close();
		}
		
		rs.close();
		stmt.close();
		connection.close();
		
		logger.debug("# of returned clients: " + clients.size());
		return clients;
		
	}

	/**
	 * deletes a client with the clientID registered under topic, from the database
	 */
	public  void deleteClient(String topic, String clientID) throws Exception {
		
		PreparedStatement stmt;
		
		this.deleteClientProps(clientID, topic);
		
		Connection connection = ds.getConnection();
		stmt = connection.prepareStatement(stmtManager.getClientStoreStmt(ClientStoreIF.DELETE_CLIENT));
		stmt.setString(1, topic);
		stmt.setString(2, clientID);
		stmt.execute();
		
		stmt.close();
		connection.close();
		logger.debug("client [" + clientID +"] is deleted from topic [" + topic+ "]");
	}
	
	/**
	 * checks whether there is a client with clientID registered under topic
	 */
	public  void deleteClient(String topic, RemoteClientIF client) throws Exception {
		this.deleteClient(topic, (String) client.getProperty(RemoteClientIF.CLIENT_ID));
	}
	
	/**
	 * The method updates the properties for a given client under the specified topic.
	 * 
	 * @param topic			The topic for which the client properties need to be updated
	 * @param client		The reference to the client whose value needs to be updated
	 * @throws Exception	The exception thrown in case of any failure
	 */
	public void updateClient(String topic, RemoteClientIF client) throws Exception {
		
		PreparedStatement stmt = null;
		Set<String> clientPropNames = client.getPropertyNames();
		
		Connection connection = ds.getConnection();
		connection.setAutoCommit(false);
		
		for (String clientPropName : clientPropNames){
			
			stmt = connection.prepareStatement(stmtManager.getClientStoreStmt(ClientStoreIF.UPDATE_CLIENT_PROPS));
			stmt.setString(1, (String) client.getProperty(clientPropName));
			stmt.setString(2, (String) client.getProperty(RemoteClientIF.CLIENT_ID));
			stmt.setString(3, (String) client.getProperty(RemoteClientIF.CLIENT_TYPE));
			stmt.setString(4, (String) clientPropName);
			stmt.setString(5, topic);
			stmt.execute();
		}
		connection.commit();
		connection.setAutoCommit(false);
		
		if (stmt != null)
			stmt.close();
		connection.close();
		logger.debug("client [" + client.getProperty(RemoteClientIF.CLIENT_ID) +"] is updated for topic [" + topic+ "]");
	}
	

	/**
	 * checks whether there is a client with clientID registered under topic
	 */
	public  boolean existsClient(String topic, String clientID) throws Exception{
		
		boolean exists = false;
		PreparedStatement stmt;
		Connection connection = ds.getConnection();
		stmt = connection.prepareStatement(stmtManager.getClientStoreStmt(ClientStoreIF.EXIST_CLIENT));
		
		stmt.setString(1, topic);
		stmt.setString(2, clientID);
		
		ResultSet rs = stmt.executeQuery();
		
		if (rs.next())
			exists = true;
			
		rs.close();
		stmt.close();
		connection.close();
		logger.debug("client [" + clientID + "] does not exist in the topic [" + topic + "]");
		return exists;
	}
	
	/**
	 * checks whether there is a client with clientID registered under topic
	 */
	public  boolean existsClient(String topic, RemoteClientIF client) throws Exception {
		
		return this.existsClient(topic, (String) client.getProperty(RemoteClientIF.CLIENT_ID)); 
	}
	
	public  Set<String> getClientTopics(String clientID) throws Exception {
		
		Set<String> topics = new HashSet<String>();
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getClientStoreStmt(ClientStoreIF.GET_CLIENT_TOPIC_ID_BY_NAME));
		
		stmt.setString(1, clientID);
		
		ResultSet rs = stmt.executeQuery();
		
		while (rs.next()){
			
			int topicID = rs.getInt("topic_id");
			
			stmt = connection.prepareStatement(stmtManager.getTopicStoreStmt(TopicStoreIF.GET_TOPIC_BY_ID));
			stmt.setInt(1, topicID);
			
			ResultSet rs2 = stmt.executeQuery();
			if (rs2.next())
				topics.add(rs2.getString("name"));
		}
		
		rs.close();
		stmt.close();
		connection.close();
		
		return topics;
	}
	
	/**
	 * 
	 */
	private int getClientTopicID (int clientDbID) throws Exception {
		
		int topicID = -1;
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getClientStoreStmt(ClientStoreIF.GET_CLIENT_TOPIC_ID_BY_ID));
			
		stmt.setInt(1, clientDbID);
		
		ResultSet rs = stmt.executeQuery();
		
		if (rs.next())
			topicID = rs.getInt("topic_id");
		
		stmt.close();
		connection.close();
		return topicID;
	}
	
	
	private void addClientProps(String clientID, String propName, String propValue) throws Exception {
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getClientStoreStmt(ClientStoreIF.ADD_CLIENT_PROPS));
		stmt.setString(1, clientID);
		stmt.setString(2, propName);
		stmt.setString(3, propValue);
		
		stmt.execute();
		
		stmt.close();
		connection.close();
	}
	
	private  void deleteClientProps(String clientID, String topic) throws Exception {
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getClientStoreStmt(ClientStoreIF.EXIST_CLIENT));
		
		stmt.setString(1, topic);
		stmt.setString(2, clientID);
		
		ResultSet rs = stmt.executeQuery();
		
		if(rs.next()){
			
			int clientDbID = rs.getInt("id");
			
			stmt = connection.prepareStatement(stmtManager.getClientStoreStmt(ClientStoreIF.DELETE_CLIENT_PROPS));
			stmt.setInt(1, clientDbID);
			stmt.execute();
			
		}
			
		rs.close();
		stmt.close();
		connection.close();
	}
	
	private  HashMap<String, String> getClientProps(int clientDbID) throws Exception {
		
		HashMap<String, String> clientProps = new HashMap<String, String>();
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getClientStoreStmt(ClientStoreIF.GET_CLIENT_PROPS));
		
		stmt.setInt(1, clientDbID);
		
		ResultSet rs = stmt.executeQuery();
		
		if (rs == null)
			throw new SQLException("No properties were returned for the client with the DataBase ID: " + clientDbID);
		
		while(rs.next())
			clientProps.put(rs.getString("prop_name"), rs.getString("prop_value"));
		
		rs.close();
		stmt.close();
		connection.close();
		
		return clientProps;
	}

}
