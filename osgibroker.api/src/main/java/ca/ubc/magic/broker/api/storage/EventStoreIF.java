package ca.ubc.magic.broker.api.storage;

/*
 * RESTBroker Project
 * Copyright (c) UBC Media and Graphics Interdisciplinary Centre (MAGIC) 2007
 * http://www.magic.ubc.ca/
 * 
 */

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import ca.ubc.magic.broker.api.ds.Event;

public interface EventStoreIF {
	
	public static final int LAST_EVENTS_NUM = 15;
	public static final String ADD_EVENT_LOG = "add-event-log";
	public static final String ADD_EVENT = "add-event";
	public static final String GET_EVENT = "get-event";
	public static final String GET_LAST_EVENTS = "get-last-events";
	public static final String GET_LAST_EVENT_LOGS = "get-last-event-logs";
	public static final String DELETE_EVENT_LOG = "delete-event-log";
	public static final String DELETE_EVENT = "delete-event";
	public static final String GET_EVENT_TIMESTAMP = "get-event-timestamp";
	
	public static final String GET_CLIENT_BEFORE_E = "get-client-event-num-before";
	public static final String GET_CLIENT_BEFORE_T = "get-client-event-time-before";
	public static final String GET_CLIENT_AFTER_E  = "get-client-event-num-after";
	public static final String GET_CLIENT_AFTER_T  = "get-client-event-time-after";
	public static final String GET_CLIENT_EVENT_FRAME = "get-client-event-frame";
	
	public static final String GET_SERVER_BEFORE_E = "get-server-event-num-before";
	public static final String GET_SERVER_BEFORE_T = "get-server-event-time-before";
	public static final String GET_SERVER_AFTER_E  = "get-server-event-num-after";
	public static final String GET_SERVER_AFTER_T  = "get-server-event-time-after";
	public static final String GET_SERVER_EVENT_FRAME = "get-server-event-frame";
	
	/**
	 * Adds events to the data base
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void addEvent(String topic, Event event) throws Exception;

	/**
	 * Find events that match the query string.  Query string is of the form
	 * <attr>&<attr>=<value>&... events that have attributes with values
	 * (if specified) will be returned.
	 * 
	 * @param topic
	 * @param queryString
	 */
	public List<Event> findEvents(String queryString) throws Exception;
	
	/**
	 * Get an event from event store
	 * @param id
	 * @return
	 */
	public Event getEvent(String id) throws Exception;

	/**
	 * Get the last N events sent to the topic, most recent first
	 * 
	 * @param topic
	 * @param numEvents
	 * @return
	 */
	public List<Event> getLastEvents(String topic, int numEvents) throws Exception;
	
	/**
	 * Get the last N events sent to the topic, most recent first
	 * 
	 * @param topic
	 * @param numEvents
	 * @return
	 */
	public List<Event> getLastEvents(String topic) throws Exception;
	
	/**
	 * deletes from the database all the events sent over a topic
	 * 
	 * @param topic
	 * @throws Exception
	 */
	public void deleteEvents(String topic) throws Exception;
	
	public List<Event> getAfterEClient(String topic, long queryStart, int queryAfterE) throws SQLException, XPathExpressionException;
	
	public List<Event> getBeforeEClient(String topic, long queryStart, int queryBeforeE) throws SQLException, XPathExpressionException;
	
	public List<Event> getBeforeTClient(String topic, long queryStart, long queryBeforeT) throws SQLException, XPathExpressionException;
	
	public List<Event> getAfterTClient(String topic, long queryStart, long queryAfterT) throws SQLException, XPathExpressionException;
	
	public List<Event> getFrameClientTime(String topic, long queryStart, long queryEnd) throws XPathExpressionException, SQLException;
	
	public List<Event> getAfterEServer(String topic, long queryStart, int queryAfterE) throws SQLException, XPathExpressionException;
	
	public List<Event> getBeforeEServer(String topic, long queryStart, int queryBeforeE) throws SQLException, XPathExpressionException;
	
	public List<Event> getAfterTServer(String topic, long queryStart, long queryAfterT) throws SQLException, XPathExpressionException;

	public List<Event> getBeforeTServer(String topic, long queryStart, long queryBeforeT) throws SQLException, XPathExpressionException;
	
	public List<Event> getFrameServerTime(String topic, long queryStartT, long queryEnd) throws XPathExpressionException, SQLException;
}
