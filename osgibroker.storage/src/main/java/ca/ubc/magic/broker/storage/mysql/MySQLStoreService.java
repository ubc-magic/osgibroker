package ca.ubc.magic.broker.storage.mysql;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;

import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.storage.helper.Configuration;

/**
 * The OSGi BundleActivator registering the DBManager with OSGi
 * 
 * @author nima
 *
 */
public class MySQLStoreService implements BundleActivator {
	
	private static final Logger logger = Logger.getLogger(MySQLStoreService.class);
	
	private static final String STORAGE_SRVC_START_MSG = "Starting OSGiBroker Storage Service ...";
	private static final String DONE_MSG = "[DONE].";		
		
	private final String STORAGE_PID = "ca.ubc.magic.osgibroker.storage.mysql";
	
	ServiceRegistration mysqlReg;
	ServiceRegistration configReg;

	public void start(BundleContext context) throws Exception {
		
		logger.debug("DBManagerService is starting");
		System.out.print(STORAGE_SRVC_START_MSG);
		
		Properties configProps = new Properties();
		configProps.put(Constants.SERVICE_ID, this.STORAGE_PID+".configuration");
		configProps.put(Constants.SERVICE_PID, this.STORAGE_PID);
		configReg = context.registerService(ManagedService.class.getName(), Configuration.getInstance(), configProps);
		
		Properties managerProps = new Properties();
		managerProps.put(DBManagerIF.DB_TYPE, MySQLDBManager.MYSQL_DBTYPE);
		managerProps.put(DBManagerIF.DB_URL, MySQLDBManager.MYSQL_URL);
		managerProps.put(DBManagerIF.DB_NAME, MySQLDBManager.MYSQL_DB);
		
		MySQLDBManager mysqlDBManager = new MySQLDBManager();
		mysqlReg = context.registerService(DBManagerIF.class.getName(), mysqlDBManager, managerProps);
		
		System.out.println(DONE_MSG);
		
	}

	public void stop(BundleContext context) throws Exception {
		logger.debug("DBManagerService is stopping");
		
		if (mysqlReg != null){
			mysqlReg.unregister();
			mysqlReg = null;
		}
		
		if (configReg != null){
			configReg.unregister();
			configReg = null;
		}
		
	}

}
