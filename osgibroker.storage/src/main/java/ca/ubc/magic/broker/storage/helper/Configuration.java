package ca.ubc.magic.broker.storage.helper;

import org.jdom.*;
import org.jdom.input.*;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.apache.log4j.Logger;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.*;
import java.util.Dictionary;

/**
 * Taken from <a href=http://www.freshblurbs.com/jakarta-commons-dbcp-tutorial>http://www.freshblurbs.com/jakarta-commons-dbcp-tutorial</a> which
 * provides a simple configuration class to define the address, username, password, name, and other related configuration
 * variables for a database. The config.xml file is located in src/main/resources/conf
 *
 * 
 * @ inadareishvili
 */
public class Configuration implements ManagedService {

    private static final String CONFIG_FILENAME = "conf/config.xml";
    
    private static Configuration config = null;

    private String dbDriverName = null;
    private String dbUser = null;
    private String dbPassword = null;
    private String dbURL = null;
    private String dbName = null;
    private String validationQuery = null;
    
    private boolean autoReconnectForPools=false;

    private int dbPoolMinSize = 0;
    private int dbPoolMaxSize = 0;
    
    private int minEvitIdleTime  = 0;
    private int timeBetweenEvict = 0;
    private int numTestsPerEvict = 0;

    private static final Logger logger = 
    	Logger.getLogger( Configuration.class );
    
    public static Configuration getInstance(){
    	
    	if (config == null){
    		synchronized (Configuration.class) {
    			if (config == null)
    				config = new Configuration();
    		}
    	}
    	return config;
    }

    /**
     * Reads the configuration file and initializes the variables
     */
    public Configuration() {

        try {
        	
        	this.updated(null);

        }   catch ( Exception ex ) {
            logger.error( "Could not read configuration file: ", ex );
        }

    }


    public String getDbDriverName() {
        return dbDriverName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbURL() {
        return dbURL;
    }

    public int getDbPoolMinSize() {
        return dbPoolMinSize;
    }

    public int getDbPoolMaxSize() {
        return dbPoolMaxSize;
    }
    
    public int getMinEvitIdleTime() {
    	return minEvitIdleTime;
    }
    
    public int getTimeBetweenEvict() {
    	return timeBetweenEvict;
    }
    
    public int getNumTestsPerEvict(){
    	return numTestsPerEvict;
    }
    
    public String getDBName() {
    	return dbName;
    }
    
    public boolean getAutoReconnect(){
    	return autoReconnectForPools;
    }
    
    public String getValidationQuery(){
    	return validationQuery;
    }

    public String toString() {
        ReflectionToStringBuilder tsb = 
        	new ReflectionToStringBuilder(this);
        return tsb.toString();
    }

	public void updated(Dictionary dict) throws ConfigurationException {
		
		if (dict == null){
			configureDefaults();
		}else {
		
			dbDriverName = (String) dict.get("ca.ubc.magic.osgibroker.storage.dbDriverName");
	        dbUser = (String) dict.get("ca.ubc.magic.osgibroker.storage.dbUser");
	        dbPassword = (String) dict.get("ca.ubc.magic.osgibroker.storage.dbPassword");
	        dbURL = (String) dict.get("ca.ubc.magic.osgibroker.storage.dbURL");
	        dbName = (String) dict.get("ca.ubc.magic.osgibroker.storage.dbName");
	        validationQuery = (String) dict.get("ca.ubc.magic.osgibroker.storage.validationQuery");
	        
	        if (((String) dict.get("ca.ubc.magic.osgibroker.storage.autoReconnectForPools")).equals("true"))
	        	autoReconnectForPools = true;
	        
	        dbPoolMinSize = Integer.parseInt( (String) dict.get("ca.ubc.magic.osgibroker.storage.dbPoolMinSize") );
	        dbPoolMaxSize = Integer.parseInt( (String) dict.get("ca.ubc.magic.osgibroker.storage.dbPoolMaxSize") );
	        
	        minEvitIdleTime  = Integer.parseInt( (String) dict.get("ca.ubc.magic.osgibroker.storage.dbcp.minEvitIdleTime") );
	        timeBetweenEvict = Integer.parseInt( (String) dict.get("ca.ubc.magic.osgibroker.storage.dbcp.timeBetweenEvict") );
	        numTestsPerEvict = Integer.parseInt( (String) dict.get("ca.ubc.magic.osgibroker.storage.dbcp.numTestsPerEvict") );
		}
		
		logger.debug("dbDriverName: " + dbDriverName);
		logger.debug("dbUser: " + dbUser);
		logger.debug("dbPassword: " + dbPassword);
		logger.debug("dbURL: " + dbURL);
		logger.debug("dbName: " + dbName);
		logger.debug("validationQuery: " + validationQuery);
		
		logger.debug("dbPoolMinSize: " + dbPoolMinSize);
		logger.debug("dbPoolMaxSize: " + dbPoolMaxSize);
		logger.debug("minEvitIdleTime: " + minEvitIdleTime);
		logger.debug("timeBetweenEvict: " + timeBetweenEvict);
		logger.debug("numTestsPerEvict: " + numTestsPerEvict);
		
	}
	
	private void configureDefaults(){
		SAXBuilder builder = new SAXBuilder();

        try {

            InputStream is =
            this.getClass().getClassLoader().getResourceAsStream( CONFIG_FILENAME );

            Document doc = builder.build ( is );
            Element root = doc.getRootElement();

            dbDriverName = root.getChild("dbDriverName").getTextTrim();
            dbUser = root.getChild("dbUser").getTextTrim();
            dbPassword = root.getChild("dbPassword").getTextTrim();
            dbURL = root.getChild("dbURL").getTextTrim();
            dbName = root.getChild("dbName").getTextTrim();
            validationQuery = root.getChild("validationQuery").getTextTrim();
            
            if (root.getChild("autoReconnectForPools").getText().equals("true"))
            	autoReconnectForPools = true;
            
            dbPoolMinSize = 
            	Integer.parseInt( root.getChild("dbPoolMinSize").getTextTrim() );
            dbPoolMaxSize = 
            	Integer.parseInt( root.getChild("dbPoolMaxSize").getTextTrim() );
            
            minEvitIdleTime  = Integer.parseInt( root.getChild("minEvitIdleTime").getTextTrim() );
	        timeBetweenEvict = Integer.parseInt( root.getChild("timeBetweenEvict").getTextTrim() );
	        numTestsPerEvict = Integer.parseInt( root.getChild("numTestsPerEvict").getTextTrim() );

        }   catch ( Exception ex ) {
            logger.error( "Could not read configuration file: ", ex );
        }
	}
}

