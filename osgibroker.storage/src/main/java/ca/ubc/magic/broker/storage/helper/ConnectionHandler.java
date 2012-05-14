package ca.ubc.magic.broker.storage.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * The connection handler manages the connections with the Database. 
 * 
 * @author nima
 *
 */
public class ConnectionHandler {
	
	private static ConnectionHandler connectionHandler = null;
	
	private ConnectionHandler(){
		
	}
	
	public static ConnectionHandler getInstance(){
		if (connectionHandler == null)
			synchronized (ConnectionHandler.class){
				if (connectionHandler == null)
					connectionHandler = new ConnectionHandler();
			}
		return connectionHandler;
	}
	
	/**
	 * Provides a connection to the database server when the name of the database is not clear
	 * @param dbURL		The URL to the database server
	 * @param dbUser	The username to access the database
	 * @param dbPwd		The password to access the database
	 * @return			The connection object to the database
	 */
	public synchronized Connection getConnection(String dbURL, String dbUser, String dbPwd){
		
		Connection tempCon = null;
		
		try{
			
			Properties dbProps = new Properties();
			dbProps.put("user", dbUser);
			dbProps.put("password", dbPwd);
			dbProps.put("autoReconnectForPools", Configuration.getInstance().getAutoReconnect() ? "true" : "false");
			
			Class.forName(Configuration.getInstance().getDbDriverName());//, true, dbLoader);
			tempCon = DriverManager.getConnection(dbURL, dbProps);
			
		}catch (Exception e){
			e.printStackTrace();
		}
		
		return tempCon;
	}
	
	/**
	 * returns the connection to the database when the name of the database is not defined. The 
	 * values for URL, username, and password are received from the Configuration singleton
	 * 
	 * @return		The connection object to the database
	 */
	public synchronized Connection getConnection(){
		
		String dbURL  = Configuration.getInstance().getDbURL();
		String dbUser = Configuration.getInstance().getDbUser();
		String dbPwd  = Configuration.getInstance().getDbPassword();
		
		return getConnection(dbURL, dbUser, dbPwd);
	}
	
	/**
	 * returns the connection to the database where the name of the database is defined. The name
	 * for the database is also received from the Configuration singleton.
	 * 
	 * @return 		The connection object to the database
	 */
	public synchronized Connection getDBConnection(){
		
		String dbURL  = Configuration.getInstance().getDbURL() + 
						Configuration.getInstance().getDBName();
		String dbUser = Configuration.getInstance().getDbUser();
		String dbPwd  = Configuration.getInstance().getDbPassword();
		
		return getConnection(dbURL, dbUser, dbPwd);
	}
	
}
