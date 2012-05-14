package ca.ubc.magic.broker.storage.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import ca.ubc.magic.broker.api.storage.DBCreatorIF;
import ca.ubc.magic.broker.storage.helper.Configuration;
import ca.ubc.magic.broker.storage.helper.ConnectionManager;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MySQLDataSourceTest extends TestCase {
	
	private ConnectionManager connectionManager;
	private DBCreatorIF  dbCreator  = null;
	
	public void setUp() throws Exception {
		
		super.setUp();
		
		dbCreator = new MySQLDBCreator();
		dbCreator.createDB();
		
		connectionManager = new ConnectionManager(Configuration.getInstance());
		
	}
	
	public void tearDown() throws Exception {
		
		super.tearDown();
		connectionManager = null;
		
	}
	
	public MySQLDataSourceTest(String name){
		super(name);
	}
	
	public void testConnectionManager() throws Exception {
		
		Connection conn = connectionManager.getDataSource().getConnection();
		
		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM topics");
		
		ResultSet rs = stmt.executeQuery();
		while (rs.next()){
			System.err.println(rs.getString("name"));
		}
	}
	
	public static Test suite(){
		
		TestSuite suite = new TestSuite();
		suite.addTest(new MySQLDataSourceTest("testConnectionManager"));
		return suite;
	}

}
