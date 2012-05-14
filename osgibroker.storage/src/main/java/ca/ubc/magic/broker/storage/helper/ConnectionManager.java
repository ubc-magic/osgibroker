package ca.ubc.magic.broker.storage.helper;

import org.apache.log4j.Logger;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.dbcp.*;

import javax.sql.DataSource;
import java.util.Date;
import java.sql.*;

/**
 * @ inadareishvili
 */
public class ConnectionManager {

    private static final Logger logger = Logger.getLogger( ConnectionManager.class );

    private DataSource ds = null;
    private static GenericObjectPool _pool = null;

    /**
    *  @param config configuration from an XML file.
    */
    public ConnectionManager(Configuration config)
    {
        try
        {
            connectToDB( config );
            logger.debug(config.toString());
        }
        catch(Exception e)
        {
            logger.error( "Failed to construct ConnectionManager", e );
        }
    }

    /**
    *  destructor
    */
    protected void finalize()
    {
        logger.debug("Finalizing ConnectionManager");
        try
        {
            super.finalize();
        }
        catch(Throwable ex)
        {
            logger.error( "ConnectionManager finalize failed" +
            "to disconnect from mysql: ", ex );
        }
    }


    /**
    *  connectToDB - Connect to the MySql DB!
    */
    private void connectToDB( Configuration config ) {

        try
        {
            java.lang.Class.forName( config.getDbDriverName());
//            		, true, 
//            		DriverManagerConnectionFactory.class.getClassLoader() );//.newInstance();
        }
        catch(Exception e)
        {
            logger.error("Error when attempting to obtain DB Driver: "
                    + config.getDbDriverName() + " on "
                    + new Date().toString(), e);
        }

        logger.debug("Trying to connect to database...");
        try
        {
            	ds = setupDataSource(
                    config.getDbURL() + config.getDBName() + "?autoReconnectForPools=" + ((config.getAutoReconnect()) ? "true" : "false"),
                    config.getDbUser(),
                    config.getDbPassword(),
                    config.getDbPoolMinSize(),
                    config.getDbPoolMaxSize(),
                    config.getMinEvitIdleTime(),
                    config.getTimeBetweenEvict(),
                    config.getNumTestsPerEvict(),
                    config.getValidationQuery());
            	
            logger.debug("Connection attempt to database succeeded.");
        }
        catch(Exception e)
        {
            logger.error("Error when attempting to connect to DB ", e);
        }
    }

    /**
     *
     * @param connectURI - JDBC Connection URI
     * @param username - JDBC Connection username
     * @param password - JDBC Connection password
     * @param minIdle - Minimum number of idel connection in the connection pool
     * @param maxActive - Connection Pool Maximum Capacity (Size)
     * @throws Exception
     */
    public DataSource setupDataSource(String connectURI, 
									  String username, 
									  String password,
									  int minIdle, int maxActive,
									  int minEvitIdleTime, 
									  int timeBetweenEvict, 
									  int numTestsPerEvict,
									  String validationQuery) throws Exception {
        //
        // First, we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        GenericObjectPool connectionPool = new GenericObjectPool(null);

        connectionPool.setMinIdle( minIdle );
        connectionPool.setMaxActive( maxActive );
        
        connectionPool.setMinEvictableIdleTimeMillis(minEvitIdleTime);
        connectionPool.setTimeBetweenEvictionRunsMillis(timeBetweenEvict);
        connectionPool.setNumTestsPerEvictionRun(numTestsPerEvict);
        
        if (validationQuery != null){
        	logger.debug("testing connections on browse is set to true");
        	connectionPool.setTestOnBorrow(true);
        }

        ConnectionManager._pool = connectionPool; 
        // we keep it for two reasons
      // #1 We need it for statistics/debugging
      // #2 PoolingDataSource does not have getPool()
      // method, for some obscure, weird reason.

        //
        // Next, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string from configuration
        //
        ConnectionFactory connectionFactory = 
        	new DriverManagerConnectionFactory(connectURI,username, password);

        //
        // Now we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(
        	connectionFactory,connectionPool,null,validationQuery,false,true);

        DataSource dataSource = 
        	new PoolingDataSource(connectionPool);
        
        return dataSource;
    }
    
    public DataSource getDataSource(){
    	return ds;
    }

    public static void printDriverStats() throws Exception {
        ObjectPool connectionPool = ConnectionManager._pool;
        logger.info("NumActive: " + connectionPool.getNumActive());
        logger.info("NumIdle: " + connectionPool.getNumIdle());
    }

    /**
    *  getNumLockedProcesses - gets the 
    *  number of currently locked processes on the MySQL db
    *
    *  @return Number of locked processes
    */
    public int getNumLockedProcesses()
    {
        int num_locked_connections = 0;
        java.sql.Connection con = null; 
        java.sql.PreparedStatement p_stmt = null;  ResultSet rs = null;
        try
        {
            con = ds.getConnection();
            p_stmt = con.prepareStatement("SHOW PROCESSLIST");
            rs = p_stmt.executeQuery();
            while(rs.next())
            {
                if(rs.getString("State") != 
                		null && rs.getString("State").equals("Locked"))
                {
                    num_locked_connections++;
                }
            }
        }
        catch(Exception e)
        {
            logger.debug("Failed to get get Locked Connections - Exception: " + e.toString());
        } finally {
            try {
                rs.close();
                p_stmt.close();
                con.close();
            }  catch ( java.sql.SQLException ex) {
                logger.error ( ex.toString() );
            }
        }
        return num_locked_connections;
    }
    
    public void displayDbProperties() throws ClassNotFoundException, SQLException {
        java.sql.Connection connection = ds.getConnection();
        java.sql.DatabaseMetaData dm = null;
        ResultSet rs = null;
        if (connection != null) {
            dm = connection.getMetaData();
            logger.info("\tDriver Name: " + dm.getDriverName());
            logger.info("\tDriver Version: " + dm.getDriverVersion());
            logger.info("\tDatabase Name: " + dm.getDatabaseProductName());
            logger.info("\tDatabase Version: " + dm.getDatabaseProductVersion());
            rs = dm.getCatalogs();
            while (rs.next()) {
                //System.out.println("\tcatalog: " + rs.getString(1));
                }
            connection.close();
        }
    }

}
