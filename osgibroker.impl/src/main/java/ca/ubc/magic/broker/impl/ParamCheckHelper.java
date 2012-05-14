package ca.ubc.magic.broker.impl;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.notification.NotificationHelper;
import ca.ubc.magic.broker.http.ExtendedHttpServlet;

/**
 * ParamCheckHelper is a helper class allowing the input parameters to the input gateway for subscribed clients of
 * different types to be analyzed and checked consistently across all different gateways.
 *
 * @author nima
 *
 */
public class ParamCheckHelper {

	/**
	 * Checks for the correctness of input params to the subscriber gateway and doesn't allow for the topic
	 * to be in the form of a NotificationTopic. The method is originally written to support ParameterMap
	 * returned by the HttpServletRequest and hence recives Map<String, String[]> as the input. This should
	 * be kept in mind when using it for subscriber gateways other than the servlet subscriber.
	 *
	 * @param paramMap				The list of parameters entered to the subscriber gateway
	 * @throws BrokerException		The Exception thrown in case of failure
	 */
	public static void doPreCheck(Map<String, String[]> paramMap) throws BrokerException{
		doPreCheck(paramMap, false);
	}

	/**
	 * Checks for the correctness of input params to the subscriber gateway and does allow for the topic
	 * to be in the form of a NotificationTopic. The method is originally written to support ParameterMap
	 * returned by the HttpServletRequest and hence recives Map<String, String[]> as the input. This should
	 * be kept in mind when using it for subscriber gateways other than the servlet subscriber.
	 *
	 * @param paramMap				The list of parameters entered to the subscriber gateway
	 * @throws BrokerException		The Exception thrown in case of failure
	 */
	public static void doPrecheckAllowNotificationTopic(Map<String, String[]> paramMap) throws BrokerException {
		doPreCheck(paramMap, true);
	}

	public static void doPrecheckAllowTopicNull(Map<String, String[]> paramMap) throws BrokerException {
		doPrecheckParamSize(paramMap);
		doPrecheckClientNotNull(paramMap);
	}

	// ---- helper methods

	/**
	 * Checks the potential input parameters to the client gateway for the subscribed clients to see whether topic,
	 * clientID, etc, are entered properly.
	 *
	 * @param paramMap					The Map for the list of parameters and their values
	 * @param allowNotificationTopic	If true, a notification topic is allowed to be queried by the client, otherwise not.
	 * @throws BrokerException			The Exception thrown in case of any problem with the input data
	 */
	private static void doPreCheck(Map<String, String[]> paramMap, boolean allowNotificationTopic) throws BrokerException {

		doPrecheckParamSize(paramMap);
		doPrecheckTopicNotNull(paramMap);
		doPrecheckTimeStampProper(paramMap);

		if (!allowNotificationTopic)
			doPrecheckAllowForNotificationTopic(paramMap);

		doPrecheckClientNotNull(paramMap);
	}

	private static void doPrecheckParamSize(Map<String, String[]> paramMap) throws BrokerException{

		// No parameter has been submitted to the servlet
		if (paramMap.size() <= 0)
			throw new BrokerException (HttpServletResponse.SC_BAD_REQUEST, BrokerException.NO_PARAMETER_RECEIVED);
	}

	private static void doPrecheckTopicNotNull(Map<String, String[]> paramMap) throws BrokerException{

		// Not topic has been defined in the set of parameters sent by the client
		if (paramMap.get(SubscriberIF.TOPIC) == null ||
				StringEscapeUtils.escapeHtml(paramMap.get(SubscriberIF.TOPIC)[0]) == null)
			throw new BrokerException (HttpServletResponse.SC_NOT_FOUND, BrokerException.NO_TOPIC_DEFINED);

	}
	
	private static void doPrecheckTimeStampProper(Map<String, String[]> paramMap) throws BrokerException{
		
		//if timestamp is set, it should have the proper value
		if (paramMap.get(Event.CLIENT_EVENT_TIMESTAMP) != null && StringEscapeUtils.escapeHtml(paramMap.get(Event.CLIENT_EVENT_TIMESTAMP)[0]) != null)
			try{
				Long.parseLong(StringEscapeUtils.escapeHtml(paramMap.get(Event.CLIENT_EVENT_TIMESTAMP)[0]));
			}catch(NumberFormatException nfe){
				throw new BrokerException (HttpServletResponse.SC_BAD_REQUEST, BrokerException.BAD_CLIENT_TIMESTAMP);
			}
			
	}

	private static void doPrecheckAllowForNotificationTopic(Map<String, String[]> paramMap) throws BrokerException {

		if (paramMap.get(SubscriberIF.TOPIC) == null ||
				NotificationHelper.isNotificationTopic(paramMap.get(SubscriberIF.TOPIC)[0]))
			throw new BrokerException (HttpServletResponse.SC_FORBIDDEN, BrokerException.FORBIDDEN_NOTIFICATION_TOPIC);
	}

	private static void doPrecheckClientNotNull(Map<String, String[]> paramMap) throws BrokerException {

		// No clientID is received from the message sent by the client
		if (paramMap.get(RemoteClientIF.CLIENT_ID) == null ||
				StringEscapeUtils.escapeHtml(paramMap.get(RemoteClientIF.CLIENT_ID)[0]) == null)
			throw new BrokerException (HttpServletResponse.SC_NOT_FOUND, BrokerException.NO_CLIENT_ID_DEFINED);
	}
	
	public static boolean doPrecheckTimeFrameQuery(Map<String, String[]> paramMap) throws BrokerException {
		
		String queryStart   = paramMap.get(ExtendedHttpServlet.QUERY_START) != null ? 
				StringEscapeUtils.escapeHtml(paramMap.get(ExtendedHttpServlet.QUERY_START)[0]) : null;
		String queryEnd     =  paramMap.get(ExtendedHttpServlet.QUERY_END) != null ?
				StringEscapeUtils.escapeHtml(paramMap.get(ExtendedHttpServlet.QUERY_END)[0]) : null;
		String queryBeforeT =  paramMap.get(ExtendedHttpServlet.QUERY_BEFORE_TIME) != null ?
				StringEscapeUtils.escapeHtml(paramMap.get(ExtendedHttpServlet.QUERY_BEFORE_TIME)[0]) : null;
		String queryAfterT  =  paramMap.get(ExtendedHttpServlet.QUERY_AFTER_TIME) != null ?
				StringEscapeUtils.escapeHtml(paramMap.get(ExtendedHttpServlet.QUERY_AFTER_TIME)[0]) : null;
		String queryBeforeE =  paramMap.get(ExtendedHttpServlet.QUERY_BEFORE_EVENT) != null ?
				StringEscapeUtils.escapeHtml(paramMap.get(ExtendedHttpServlet.QUERY_BEFORE_EVENT)[0]) : null;
		String queryAfterE  =  paramMap.get(ExtendedHttpServlet.QUERY_AFTER_EVENT) != null ?
				StringEscapeUtils.escapeHtml(paramMap.get(ExtendedHttpServlet.QUERY_AFTER_EVENT)[0]) : null;
		
		if (queryStart == null)
			if (queryEnd != null || queryBeforeT != null || queryAfterT != null ||
					queryBeforeE != null || queryAfterE != null)
				return true;
			else 
				return false;
		else	
			if (queryEnd == null && queryBeforeT == null && queryAfterT == null &&
					queryBeforeE == null && queryAfterE == null)
				throw new BrokerException(HttpServletResponse.SC_NOT_ACCEPTABLE, BrokerException.NO_TIME_FRAME_END);
		return true;
	}

}
