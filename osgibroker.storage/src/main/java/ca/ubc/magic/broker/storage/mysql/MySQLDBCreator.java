package ca.ubc.magic.broker.storage.mysql;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.storage.DBCreatorIF;
import ca.ubc.magic.broker.storage.helper.Configuration;
import ca.ubc.magic.broker.storage.helper.ConnectionHandler;

import com.ibatis.common.jdbc.ScriptRunner;

/**
 * If the Database for the OSGiBroker does not exist, creates the database with the DB server
 * 
 * @author nima
 *
 */
public class MySQLDBCreator implements DBCreatorIF {
	
	public final static String BROKER_MYSQL_SCRIPT = "sql/mysql/osgibroker.sql";
	public final static String BROKER_ALTER_SCRIPT = "sql/mysql/osgibroker-eventlogs-alter.sql";
	private static final Logger logger = Logger.getLogger(MySQLDBCreator.class);
	
	public MySQLDBCreator (){
		
	}
	
	public void createDB() {

		boolean dbFound = false;

		// first see if we need to create our database
		try {
			Connection tempCon = ConnectionHandler.getInstance().getConnection();
			Statement stmt = tempCon.createStatement();
			ResultSet rs = stmt.executeQuery("SHOW DATABASES;");

			// Check if OSGiBroker database exists in server
			while (rs.next() && !dbFound) {
				if (rs.getString("Database").equalsIgnoreCase(Configuration.getInstance().getDBName()))
					dbFound = true;
			}
			
			InputStream is;
			
			if (!dbFound) {
				logger.info("No database found, creating database");
				stmt = tempCon.createStatement();
				stmt.execute("CREATE DATABASE " + Configuration.getInstance().getDBName());
				stmt.execute("USE " + Configuration.getInstance().getDBName()); 
				
				is = this.getClass().getClassLoader().getResourceAsStream(
						MySQLDBCreator.BROKER_MYSQL_SCRIPT);
				Reader script = new InputStreamReader(is);

				ScriptRunner runner = new ScriptRunner(tempCon, false, false);

				// Run the sql script to create database
				runner.runScript(script);
				script.close();
			}else {
				
				boolean eventLogAlteration = true;
				
				logger.info("Checking if the eventlogs DB should be altered");
				rs = stmt.executeQuery("SELECT count(*) FROM information_schema.`COLUMNS` where " +
						"TABLE_SCHEMA='" + Configuration.getInstance().getDBName() + "' AND TABLE_NAME='eventlogs' AND COLUMN_NAME='client_time'");
				
				// if the column with the name "client_time" is already available,
				// continue without altering the DB
				
				while(rs.next())
					eventLogAlteration = (rs.getInt(1) == 0) ? true : false;
				
				if (eventLogAlteration){
					logger.info("Altering the DB");
					is = this.getClass().getClassLoader().getResourceAsStream(
							MySQLDBCreator.BROKER_ALTER_SCRIPT);
					Reader script = new InputStreamReader(is);
					ScriptRunner runner = new ScriptRunner(tempCon, false, false);
					runner.runScript(script);
					script.close();
				}else
					logger.info("No alteration to the DB");
			}
			stmt.close();
			rs.close();
			tempCon.close();
		} catch (Exception e) {
			logger.error("Error while setting up database", e);
		} 
	}
	
	public static void main(String[] args){
		new MySQLDBCreator().createDB();
	}
}
