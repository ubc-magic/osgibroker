package ca.ubc.magic.broker.api.storage;

/**
 * The DBCreator class generating the required DB in the DB server
 * 
 * @author nima
 *
 */
public interface DBCreatorIF {
	
	/**
	 * The method to create a DB in the DBManager
	 */
	public void createDB();
}
