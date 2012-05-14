package ca.ubc.magic.broker.api;
import ca.ubc.magic.broker.api.storage.DBManagerIF;

/**
 * The PersistenceIF interface is used to get and unget DBManager. It is used in combination with
 * declrative services in order to enable those services to get and release the DBManager for using
 * and accessing the DB 
 * 
 * @author nima
 *
 */

public interface PersistenceIF {
	
	public void getDBManager(DBManagerIF _dbManager);
	
	public void ungetDBManager (DBManagerIF _dbManager);

}
