package ca.ubc.magic.broker.storage.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.storage.EventStoreIF;
import ca.ubc.magic.broker.storage.helper.StatementManager;

/**
 * 
 * @author nima
 *
 */
public class MySQLEventStore implements EventStoreIF {
	
	private DataSource ds = null;
	private StatementManager stmtManager = null;
	private static final Logger logger = Logger.getLogger( MySQLEventStore.class );
	
	public MySQLEventStore(DataSource _ds, StatementManager _stmtManager){
		this.ds = _ds;
		this.stmtManager = _stmtManager;
	}
	
	//TODO nimak - provide proper implementation for the function below
	
	public synchronized List<Event> findEvents(String queryString) throws Exception{
		
		logger.error("the findEvents method is not implemented");
		return null;
	}

	/**
	 * returns all the events associated with an event id when the timestamp is not known
	 */
	public Event getEvent(String id) throws Exception {

		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getEventStoreStmt(EventStoreIF.GET_EVENT_TIMESTAMP));
		stmt.setString(1, id);
		
		ResultSet rs = stmt.executeQuery();
		if (!rs.next()){
			stmt.close();
			throw new Exception("No column found for the specified event ID");
		}
		long time = rs.getLong("time");
		rs.close();
		stmt.close();
		connection.close();
		return getEvent(id, time);
	}

	/**
	 * returns the list of last <i>numEvents</i> events under a topic name
	 */
	public List<Event> getLastEvents(String topic, int numEvents) throws Exception {
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getEventStoreStmt(EventStoreIF.GET_LAST_EVENTS));
		stmt.setString(1, topic);
		stmt.setInt(2, numEvents);
		
		List<Event> eventsList = getEventsFromStatement(stmt);
		
		stmt.close();
		connection.close();
	
		if (eventsList.size() == 0 || eventsList == null){
			logger.debug("getLastEvents size = 0, returning nothing");
			return null;
		}
		
		logger.debug("getLastEvents topic=[" + topic + "] numEvents[" + numEvents +"]");
		return eventsList;
	}

	/**
	 * returns the list of last <i>LAST_EVENTS_NUM</i> events under a topic name
	 */
	public   List<Event> getLastEvents(String topic) throws Exception {
		
		return getLastEvents(topic, EventStoreIF.LAST_EVENTS_NUM);
	}
	
	/**
	 * adds an event to the database under a topic
	 */
	public void addEvent(String topic, Event event) throws Exception {
		
		Connection connection = ds.getConnection();
		connection.setAutoCommit(false);
		PreparedStatement stmt1 = connection.prepareStatement(stmtManager.getEventStoreStmt(EventStoreIF.ADD_EVENT_LOG),
				Statement.RETURN_GENERATED_KEYS);
		stmt1.setString(1, topic);
		stmt1.setString(2, (event.getTimeStamp() == -1) ? Long.toString(System.currentTimeMillis()) : Long.toString(event.getTimeStamp()));
		if (event.getAttribute(Event.CLIENT_EVENT_TIMESTAMP) != null && !event.getAttribute(Event.CLIENT_EVENT_TIMESTAMP).equals(""))
			stmt1.setString(3, event.getAttribute(Event.CLIENT_EVENT_TIMESTAMP));
		else
			stmt1.setLong(3, -1);
		
		stmt1.executeUpdate();
		
		int eventID = 0;
		ResultSet rs = stmt1.getGeneratedKeys();
		if (rs.next()) 
			eventID = rs.getInt(1);
		
		if (eventID == 0)
			throw new SQLException("The event is not stored in the event log");
		
		PreparedStatement stmt2 = connection.prepareStatement(stmtManager.getEventStoreStmt(EventStoreIF.ADD_EVENT));
		String[] eventAttributes = event.getAttributeNames();
		for (String attr : eventAttributes){
			stmt2.setString(1, String.valueOf(eventID));
			stmt2.setString(2, attr);
			stmt2.setString(3, event.getAttribute(attr));
			stmt2.executeUpdate();
		}
		connection.commit();
		connection.setAutoCommit(true);
		rs.close();
		stmt1.close();
		stmt2.close();
		connection.close();
		logger.debug("event is added to topic [" + topic + "]");
	}
	
	/**
	 * delets all the events under a topic from the database
	 */
	public void deleteEvents(String topic) throws Exception {
		
		Connection connection = ds.getConnection();
		connection.setAutoCommit(false);
		List<Event> eventsList = new ArrayList<Event>();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getEventStoreStmt(EventStoreIF.DELETE_EVENT));
		stmt.setString(1, topic);
		
		stmt.executeUpdate();
		connection.commit();
		
		stmt.close();
		connection.setAutoCommit(true);
		connection.close();
//		logger.debug(eventCounter + " events are deleted from topic: [" + topic + "]");
		
	}
	
	
	// ======================= CLIENT INTERVAL QUERIES ==========================
	public List<Event> getAfterEClient(String topic, long queryStart, int queryAfterE) throws SQLException, XPathExpressionException {
		logger.debug("getAfterEClient topic=[" + topic + "]");
		return getE(topic, EventStoreIF.GET_CLIENT_AFTER_E, queryStart, queryAfterE, true);
	}
	
	public List<Event> getBeforeEClient(String topic, long queryStart, int queryBeforeE) throws SQLException, XPathExpressionException {
		logger.debug("getBeforeEClient topic=[" + topic + "]");
		return getE(topic, EventStoreIF.GET_CLIENT_BEFORE_E, queryStart, queryBeforeE, false);
	}
	
	public List<Event> getBeforeTClient(String topic, long queryStart, long queryBeforeT) throws SQLException, XPathExpressionException {
		logger.debug("getBeforeTClient topic=[" + topic + "]");
		return getT(topic, EventStoreIF.GET_CLIENT_BEFORE_T, queryStart, queryBeforeT, false);
	}
	
	public List<Event> getAfterTClient(String topic, long queryStart, long queryAfterT) throws SQLException, XPathExpressionException {
		logger.debug("getAfterTClient topic=[" + topic + "]");
		return getT(topic, EventStoreIF.GET_CLIENT_AFTER_T, queryStart, queryAfterT, true);
	}
	
	public List<Event> getFrameClientTime(String topic, long queryStart, long queryEnd) throws XPathExpressionException, SQLException{
		logger.debug("getFrameClient topic=[" + topic + "]");
		return getFrame(topic, EventStoreIF.GET_CLIENT_EVENT_FRAME, queryStart, queryEnd);
	}
	
	// ======================= SERVER INTERVAL QUERIES ==========================
	public List<Event> getAfterEServer(String topic, long queryStart, int queryAfterE) throws SQLException, XPathExpressionException {
		logger.debug("getAfterEServer topic=[" + topic + "]");
		return getE(topic, EventStoreIF.GET_SERVER_AFTER_E, queryStart, queryAfterE, true);
	}
	
	public List<Event> getBeforeEServer(String topic, long queryStart, int queryBeforeE) throws SQLException, XPathExpressionException {
		logger.debug("getBeforeEServer topic=[" + topic + "]");
		return getE(topic, EventStoreIF.GET_SERVER_BEFORE_E, queryStart, queryBeforeE, false);
	}
	
	public List<Event> getAfterTServer(String topic, long queryStart, long queryAfterT) throws SQLException, XPathExpressionException {
		logger.debug("getAfterTServer topic=[" + topic + "]");
		return getT(topic, EventStoreIF.GET_SERVER_AFTER_T, queryStart, queryAfterT, true);
	}

	public List<Event> getBeforeTServer(String topic, long queryStart, long queryBeforeT) throws SQLException, XPathExpressionException {
		logger.debug("getBeforeTServer topic=[" + topic + "]");
		return getT(topic, EventStoreIF.GET_SERVER_BEFORE_T, queryStart, queryBeforeT, false);
	}
	
	public List<Event> getFrameServerTime(String topic, long queryStart, long queryEnd) throws XPathExpressionException, SQLException{
		logger.debug("getFrameServerTime topic=[" + topic + "]");
		return getFrame(topic, EventStoreIF.GET_SERVER_EVENT_FRAME, queryStart, queryEnd);
	}
	
	private List<Event> getE(String topic, String queryName, long queryStart, int interval, boolean afterE) throws SQLException, XPathExpressionException {
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getEventStoreStmt(queryName));
		stmt.setLong(1, queryStart);
		stmt.setString(2, topic);
		stmt.setInt(3, interval);
		
		List<Event> eventsList = (afterE) ? getEventsFromStatement(stmt, false) : getEventsFromStatement(stmt, true);		
		
		stmt.close();
		connection.close();
	
		if (eventsList.size() == 0){
			logger.debug("getE size = 0, returning nothing");
			return null;
		}
		
		logger.debug("getE topic=[" + topic + "]");
		return eventsList;
		
	}
	
	private List<Event> getFrame(String topic, String queryName, long queryStart, long queryEnd) throws SQLException, XPathExpressionException {
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getEventStoreStmt(queryName));
		stmt.setString(1, topic);
		stmt.setLong(2, queryStart);
		stmt.setLong(3, queryEnd);
		
		List<Event> eventsList = getEventsFromStatement(stmt);		
		
		stmt.close();
		connection.close();
	
		if (eventsList.size() == 0){
			logger.debug("getE size = 0, returning nothing");
			return null;
		}
		
		logger.debug("getE topic=[" + topic + "]");
		return eventsList;
		
	}

	private List<Event> getT(String topic, String queryName, long queryStart, long interval, boolean afterT) throws SQLException, XPathExpressionException {
		
		Connection connection = ds.getConnection();
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getEventStoreStmt(queryName));
		stmt.setString(1, topic);
		stmt.setLong(2, queryStart);
		
		if (afterT){
			stmt.setLong(3, queryStart);
			stmt.setLong(4, interval);
		}else {
			stmt.setLong(3, interval);
			stmt.setLong(4, queryStart);
		}
		
		List<Event> eventsList = getEventsFromStatement(stmt);		
		
		stmt.close();
		connection.close();
		
		if (eventsList.size() == 0){
			logger.debug("getAfterT size = 0, returning nothing");
			return null;
		}
		
		logger.debug("getAfterT topic=[" + topic + "]");
		return eventsList;
	}

	//===================== private methods =========================
	
	private List<Event> getEventsFromStatement(PreparedStatement stmt) throws SQLException{
		// default is to get events in a reverse order from earlier time to the later time, 
		// so the reverse equal to true is returned by default if not said otherwise
		return getEventsFromStatement(stmt, true);
	}
	
	private List<Event> getEventsFromStatement(PreparedStatement stmt, boolean reverse) throws SQLException{
		
		List<Event> eventsList = new ArrayList<Event>();
		ResultSet rs = stmt.executeQuery();
		
		long eventID = -1;
		Event event = null;
		
		while (rs.next()) {

			// the condition  below is used to group the events properly and based 
			// on their event_id. Whenever the event_id changes, it is assumed that
			// a new event is being processed and thus a new Event is created to be
			// added to the list
			if (eventID != rs.getLong("event_id")){
				
				if (event != null){
					if (reverse)
						eventsList.add(0,event);
					else
						eventsList.add(event);
				}
				
				eventID = rs.getLong("event_id");
				event = new Event();
				event.setTimeStamp(rs.getLong("server_time"));
			}
				event.addAttribute(rs.getString("name"), rs.getString("value"));
		}
		if (eventID != -1 && reverse)
			eventsList.add(0,event);
		else if (eventID !=-1)
			eventsList.add(event);
		
		rs.close();
		return eventsList;
	}
	
	/**
	 * returns all the events associated with the eventID when the timestamp is known <i>id</i>
	 */
	private Event getEvent(String id, long timestamp) throws Exception {
		
		Connection connection = ds.getConnection();
		
		Event event = new Event();
		
		event.setTimeStamp(timestamp);
		
		PreparedStatement stmt = connection.prepareStatement(stmtManager.getEventStoreStmt(EventStoreIF.GET_EVENT));
		stmt.setString(1, id);
		
		ResultSet rs = stmt.executeQuery();
		while(rs.next()){
			event.addAttribute(rs.getString("name"), rs.getString("value"));
		}
		rs.close();
		stmt.close();
		connection.close();
		
		logger.debug("getEvent id[" + id + "] timestamp [" + timestamp +"] for event  [" + event.toString() +"]");
		
		return event;
	}
}
