package ca.ubc.magic.broker.api;

/**
 * The BrokerException class is used to throw exceptions whenever the broker faces problems with
 * handling information or parsing received information.
 * 
 * @author nima
 *
 */
public class BrokerException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int errorStatusMessage = -1;
	
	/**
	 * The set of exceptions thrown when the broker encounters a problem
	 */
	public static final String NO_PARAMETER_RECEIVED = "No parameters received by the servlet.";
	public static final String NO_TOPIC_DEFINED = "No topic is defined in the parameters";
	public static final String NO_CLIENT_ID_DEFINED = "No cientID is defined in the parameters";
	public static final String NO_QUERY_DEFINED = "No query is specified on the topic.";
	public static final String NO_SUBSCRIBER_FOUND = "No subscriber was found by the servlet";
	public static final String NO_PUBLISHER_FOUND = "No content found for the publisher";
	public static final String NO_CLIENT_REGISTRATION = "Client needs to be registered with the subscriber under this topic";
	public static final String BAD_QUERY = "The query is not properly defined.";
	public static final String CONFILCT_CLIENT = "Client is already subscribed.";
	public static final String CONFLICT_CLIENT_DB = "Client is already in the database";
	public static final String CONFLICT_RECIVER_ID_EXCLUDE_ID = "ReceiverID and exludeID are the same";
	public static final String NO_CLIENT_SUBSCRIPTION = "The clients need to be subscribed before being able to poll.";
	public static final String NO_UNSUBSCRIPTION_POSSIBLE = "The client is not subscribed, no unsubscription is possible.";
	public static final String NO_STATEID_DEFINED = "No stateID is fedine in the parameters";
	public static final String NO_CLIENT_ALIVE = "No Client with this ClientID is alive";
	public static final String FORBIDDEN_NOTIFICATION_TOPIC = "Your topic has the format of NotificationTopic. Its use as a custom topic name is only allowed for polling notifications.";
	public static final String CONFILCT_TIME_FRAME_QUERY = "A query should be issued either for a timeframe or for a number of events. It is not allowed to query for both";
//	public static final String NO_TIME_FRAME_START = "A time frame query should have a \"start\" attribute";
	public static final String NO_TIME_FRAME_END   = "A time frame query should have one of the \"end\", \"beforeT\", \"beforeE\", \"afterT\", or \"afterE\" attributes";
	public static final String INVALID_QUERY_BASE = "The base specified for the query is not valid";
	public static final String BAD_CLIENT_TIMESTAMP = "The timestamp specified by the client is not valid";
	public static final String NO_VALID_NETWORK_IF = "No valid network interface detected for the specified interface";

	public BrokerException (String message){
		super(message);
	}
	
	public BrokerException (Throwable cause){
		super(cause);
	}
	
	public BrokerException (String message, Throwable cause){
		super(message, cause);
	}
	
	public BrokerException (int errorStatus, String message){
		super (message);
		this.errorStatusMessage = errorStatus;
	}
	
	public int getStatus(){
		return this.errorStatusMessage;
	}
	
	public void setStatus(int errorStatus){
		this.errorStatusMessage = errorStatus;
	}
}
