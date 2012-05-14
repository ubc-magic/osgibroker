package ca.ubc.magic.broker.storage.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;


import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.storage.SubscriberStoreIF;
import ca.ubc.magic.broker.storage.helper.StatementManager;

/**
 * 
 * @author nima
 *
 */
public class MySQLSubscriberStore implements SubscriberStoreIF {
	
	
	private DataSource ds = null;
	private StatementManager stmtManager = null;
	private static final Logger logger = Logger.getLogger( MySQLSubscriberStore.class );
	
	public MySQLSubscriberStore(DataSource _ds, StatementManager _stmtManager) {
		
		this.ds = _ds;
		this.stmtManager = _stmtManager;
	}
	
	/**
	 * adds a subscriber with the subscriberID to the topic
	 */
	public void addSubscriber(String subscriberId) throws Exception{
		
		if (existsSubscriber(subscriberId))
			return;
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getSubscriberStoreStmt(SubscriberStoreIF.ADD_SUBSCRIBER));
		stmt.setString(1, subscriberId);
		stmt.executeUpdate();
		
		stmt.close();
		connection.close();
		logger.debug("Subscriber [" + subscriberId + "] is added to the DB");
	}

	/**
	 * deletes a subscriber with the subscriberId from the topic
	 */
	public void deleteSubscriber(String subscriberId) throws Exception {
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getSubscriberStoreStmt(SubscriberStoreIF.DELETE_SUBSCRIBER));
		
		stmt.setString(1, subscriberId);
		
		stmt.execute();
		stmt.close();
		connection.close();
		
		logger.debug("Subscriber [" + subscriberId + "] is deleted from the DB");
	}
	
	/**
	 * checks whether the subscriber with subscriberId is registered with a topic or not
	 */
	public boolean existsSubscriber(String subscriberId) throws Exception{
		
		boolean exists = false;
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getSubscriberStoreStmt(SubscriberStoreIF.EXIST_SUBSCRIBER));
	
		stmt.setString(1, subscriberId);
		
		ResultSet rs = stmt.executeQuery();
		
		if (rs.next())
			exists = true;
			
		rs.close();
		stmt.close();
		connection.close();
		
		logger.info("Subscriber [" + subscriberId + "]  exists in the DB");
		return exists;
	}

}
