package ca.ubc.magic.broker.subscriber.service.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.http.ExtendedHttpServlet;
import ca.ubc.magic.broker.http.ServletErrorResponse;
import ca.ubc.magic.broker.impl.ParamCheckHelper;

public class EventsServlet extends ExtendedHttpServlet {
	
	private final String  SERVLET_ALIAS = "/osgibroker/event";
	
	
	public static final int POLLING_TIMEOUT = 30; 
	
	private SubscriberIF subscriber;
	private PublisherIF  publisher;
	private DBManagerIF  dbManager;
	
	private static final Logger logger = Logger.getLogger(EventsServlet.class);
	
	/**
	 * initializing the servlet by defining the resolution directory, the alias for the servlet, and the resolution
	 * directory alias. Also, the servlet uses the singleton ContextProvider to get the publisher and the subscriber
	 * found by the service to be registered with the servlet. 
	 * 
	 * TODO nimak -	At this point there is no startegy for supporting multiple publishers by one subscriber. This can 
	 * 				be later on added to the system to provide more flexibility in hooking different susbcribers
	 */
	public void init(){
		
		// The aliases to setup the servlet
		this.setServletAlias(SERVLET_ALIAS);
		
		// initializing the publisher and the subscriber to work with the servlet
		bindPublisher(ContextProvider.getInstance().getPublisher());
		bindSubscriber(ContextProvider.getInstance().getSubscriber());
		bindDBManager(ContextProvider.getInstance().getDBManager());
	}
	
	/**
	 * The destroy method is used to release the pointer to the publisher and subscriber services
	 */
	public void destroy(){
		// once the servlet is destroyed the publisher and the subscriber are released so that
		// they can be garbage collected
		unbindPublisher(ContextProvider.getInstance().getPublisher());
		unbindSubscriber(ContextProvider.getInstance().getSubscriber());
		unbindDBManager(ContextProvider.getInstance().getDBManager());
	}
	
	/**
	 * The HTTP GET method is used to query the EventServlet about the set of events available on the channel.
	 * The EventServlet requires the <i>topic</i> and the <i>clientID</i> to be sent as parameters to the servlet.
	 * If the <i>clientID</i> is already subscribed with the channel, then the EventServlet will allow polling
	 * of information by the client. The EventServlet uses the <i>Hangging Gets</i> strategy to receive events
	 * from the broker, i.e., it waits 30 seconds for an event to appear on a channel prior to returning an empty
	 * event. However, there is an optional parameter named <i>timeOut</i> that can be added to the set of parameters
	 * sent to the servlet to manually adjust the amount of time GET requests should hang on the server side. <i>timeOut</i>
	 * receives the desired amount of delay in seconds. Below is a sample example of how messages can be sent to the servlet.
	 * 
	 * <i>http://localhost:8080/servlet/subscribe?topic=test&clientID=234323&timeOut=2</i>
	 * 
	 * @parma request	the request received by the servlet when a GET message is issued by the client
	 * @param response	the response to be sent to the remote client issuing the GET message
	 */
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		
		try{
			
			String method = request.getParameter(ExtendedHttpServlet.METHOD);

			// support clients that can't PUT or DELETE using _method parameter
			if (method != null) {
				
				logger.debug("Action was taken for method call **" + method + "** in GET");
				if (method.equals(ExtendedHttpServlet.METHOD_POST)) {
					//checking the validity of the initial input to the doPost method. This doesn't allow for pushing information
					// to a NotificationTopic. A NotificationTopic can only be queried for reads
					ParamCheckHelper.doPreCheck(request.getParameterMap());
					this.doPost(request, response);
					return;
				}else
					//checking the validity of the initial input to the doGet method. This allows for querying the 
					// NotificationTopics
					ParamCheckHelper.doPrecheckAllowNotificationTopic(request.getParameterMap());
			}
			
			
			// No subscriber is bound to the servlet
			if (this.subscriber == null)
				throw new BrokerException(HttpServletResponse.SC_NO_CONTENT, BrokerException.NO_SUBSCRIBER_FOUND);
			
			// Not topic has been defined in the set of parameters sent by the client
			if (StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC)) != null)		
				doPolling(request, response);
			else
				doMassPolling(request, response);
				
		}catch (Exception e){
			ServletErrorResponse.doSendError(response, e);
		}
	}
	
	/**
	 * The doDeliver method re-initiates the request and response streams for the remote client
	 * and enables the client to receive a response with the set of events upon sending a get 
	 * request. The doClientMonitor function is re-initialized and prevents the client thread
	 * to the remote subscriber to be expired. 
	 * 
	 * @param request			The request received from the remote requesting client
	 * @param response			The response to be sent to the remote requesting client
	 * @throws BrokerException	The exception thrown in case the client is not subscribed but is trying to do polling
	 * @throws IOException		The exception thrown if opening the response header fails
	 * @throws InterruptedException The exception thrown if the concurrent queue list for the received events gets interrupted
	 */
	private void doPolling(HttpServletRequest request, HttpServletResponse response) throws BrokerException, IOException, InterruptedException{
		
		int timeOut = EventsServlet.POLLING_TIMEOUT;
		if (request.getParameter(ExtendedHttpServlet.TIME_OUT) != null){
			timeOut = Math.min(Integer.parseInt(request.getParameter(EventsServlet.TIME_OUT)), 
					EventsServlet.POLLING_TIMEOUT);
		}
		List<Serializable> eventsList = eventPolling( 
				StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)),
				StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC)),
				timeOut);
		
		logger.debug("event polling for client [" + 
				StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)) + "] " +
				"on topic [" + StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC)) +"] " +
				"with timeout [" + timeOut + "]");
		
		response.setHeader("Cache-Control", "no-cache");
		
		if (request.getParameter(FORMAT)==null || request.getParameter(FORMAT).equalsIgnoreCase("xml"))
			returnEventsWithXML(response, eventsList);
		else if (request.getParameter(FORMAT).equalsIgnoreCase("json"))
			returnEventsWithJSON(response, eventsList);
		//remove the events from the list		
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void doMassPolling(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		logger.debug("event mass polled");
		
		Set<String> topics = dbManager.getClientStore().getClientTopics(request.getParameter(RemoteClientIF.CLIENT_ID));

		Iterator<String> topicsItr = topics.iterator();
		List<Serializable> eventsList = null;
		
		while (topicsItr.hasNext()){
			String topic = topicsItr.next();
			if (eventsList == null)
				eventsList = eventPolling(request.getParameter(RemoteClientIF.CLIENT_ID), topic, 0 );
			else
				eventsList.addAll(eventPolling(request.getParameter(RemoteClientIF.CLIENT_ID), topic, 0 ));
		}
		
		response.setHeader("Cache-Control", "no-cache");
		
		if (request.getParameter(FORMAT)==null || request.getParameter(FORMAT).equalsIgnoreCase("xml"))
			returnEventsWithXML(response, eventsList);
		else if (request.getParameter(FORMAT).equalsIgnoreCase("json"))
			returnEventsWithJSON(response, eventsList);
	}
	
	private void returnEventsWithXML(HttpServletResponse response,
			List<Serializable> eventsList) throws IOException {
		
		response.setContentType("text/xml");
		response.getWriter().write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		
		PrintWriter writer = response.getWriter();
		writer.print("<events>");
		
		if (eventsList != null && !eventsList.isEmpty()){
			Iterator<Serializable> it = eventsList.iterator();
			while (it.hasNext())
				writer.print(it.next().toString());
		}
		writer.print("</events>");
	}

	private void returnEventsWithJSON(HttpServletResponse response,
			List<Serializable> eventsList) throws IOException {
		
		response.setContentType("application/json");
		
		//create xml event list
		StringBuilder sb = new StringBuilder();
		sb.append("<events>");
		if (eventsList != null && !eventsList.isEmpty()){
			Iterator<Serializable> it = eventsList.iterator();
			while (it.hasNext())
				sb.append(it.next().toString());
		}
		sb.append("</events>");
		
		//convert xml to json
		String xml = sb.toString();
		XMLSerializer xmlSerializer = new XMLSerializer();  
		JSON json = xmlSerializer.read( xml );  
		response.getWriter().write( json.toString(2) );
	}
	
	/**
	 * 
	 * @param clientID
	 * @param topic
	 * @param timeOut
	 * @return
	 * @throws BrokerException
	 * @throws InterruptedException
	 */
	private List<Serializable> eventPolling(String clientID, String topic, int timeOut) throws BrokerException, InterruptedException {
		
		ServletClientWrapper client = (ServletClientWrapper)subscriber.getClient(clientID, topic);
		
		if (client == null)
			throw new BrokerException(HttpServletResponse.SC_NOT_ACCEPTABLE, 
				BrokerException.NO_CLIENT_SUBSCRIPTION);
		
		List<Serializable> eventsList = (ArrayList<Serializable>) client.getEventsList(timeOut);
		
		client.clearEventsList();
		
		logger.debug("polling client ["+ clientID + "]" + " topic [" + topic + "]" + "timeout [" + timeOut + "]");
		
		return eventsList;
		
	}
	
	/**
	 * receives the request from the servlet and parses the variables into 
	 * Event messages that are then delivered to the publisher by calling the
	 * deliver method. The server requires the existance of the <i>topic</i> parameter among the
	 * set of parameters sent to the servlet using a POST method. This parameter defines the topic 
	 * under which the other variables will be delivered.
	 * 
	 * @param request			The request received from the remote requesting client
	 * @param response			The response to be sent to the remote requesting client
	 */
	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		try{ 

			//checking the validity of the initial input to the doPost method
			ParamCheckHelper.doPreCheck(request.getParameterMap());
			
			// No publisher is bound to the servlet
			if (this.publisher == null)
				throw new BrokerException(HttpServletResponse.SC_NO_CONTENT, BrokerException.NO_PUBLISHER_FOUND);
			
			Event event = new Event();
			
			//parsing the event attributes and storing them in the event data container
			Enumeration enumeration = request.getParameterNames();
			while (enumeration.hasMoreElements()){
				String key = StringEscapeUtils.escapeHtml((String) enumeration.nextElement());
				event.addAttribute(key, StringEscapeUtils.escapeHtml((String) request.getParameter(key)));	
			}
			
			// delivers the final events received by the servlet to the publisher to be distributed to the
			// subscribers of this publisher
			try {
				publisher.deliver(event, (String)request.getParameter(SubscriberIF.TOPIC));
			} catch (Exception e) {
				response.getWriter().write(e.getMessage());
				e.printStackTrace();
			}
			
			if (request.getParameter(FORMAT)==null || request.getParameter(FORMAT).equalsIgnoreCase("xml"))
			{
				// The xml output for the received events to be printed out on the output stream for the response
				PrintWriter out = response.getWriter();
				response.setHeader("Cache-Control", "no-cache");
				response.setContentType("text/xml");
				response.getWriter().write(
						"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
				out.print(event.toString());
			}
			else if (request.getParameter(FORMAT).equalsIgnoreCase("json"))
			{
				response.setContentType("application/json");
//				response.setContentType("text");
				
				//create xml event list
				StringBuilder sb = new StringBuilder();
				sb.append(event.toString());
				
				//convert xml to json
				String xml = sb.toString();
				XMLSerializer xmlSerializer = new XMLSerializer();  
				JSON json = xmlSerializer.read( xml );  
				response.getWriter().write( json.toString() );
			}
			
		}catch (Exception e){
			ServletErrorResponse.doSendError(response, e);
		}
	}
	
	/**
	 * receives the request from the servlet and parses the variables into 
	 * Event messages that are then delivered to the publisher by calling the
	 * deliver method. The server requires the existance of the <i>topic</i> parameter among the
	 * set of parameters sent to the servlet using a POST method. This parameter defines the topic 
	 * under which the other variables will be delivered.
	 * 
	 * @param request			The request received from the remote requesting client
	 * @param response			The response to be sent to the remote requesting client 
	 */
	public void doPut(HttpServletRequest request, HttpServletResponse response){
		doPost(request, response);
	}
	
	/**
	 * The method is used to bind a subscriber to the servlet. It can also be used with OSGi Declarative Services
	 * to easily bind a subscriber to a servlet.
	 * 
	 * @param _subscriber	The subscriber that will be used by the EventServlet
	 */
	public void bindSubscriber (SubscriberIF _subscriber){
		this.subscriber = _subscriber;
	}
	
	/**
	 * The method is used to release the reference to the subscriber so that it can be garbage collected
	 * 
	 * @param _subscriber	the subscriber to be released
	 */
	public void unbindSubscriber (SubscriberIF _subscriber){
		if (this.subscriber.equals(_subscriber))
			this.subscriber = null;
	}
	
	/**
	 * The method is used to bind a publisher to the servlet. It can also be used with OSGi Declarative Services
	 * to easily bind a publihser to a servlet.
	 * 
	 * @param _publisher	The publisher that will be used by the EventServlet
	 */
	public void bindPublisher (PublisherIF _publisher){
		this.publisher = _publisher;
	}
	
	/**
	 * The method is used to release the reference to the publisher so that it can be garbage collected
	 * 
	 * @param _publisher	the publisher to be released
	 */
	public void unbindPublisher (PublisherIF _publisher){
		if (this.publisher.equals(_publisher))
			this.publisher = null;
	}
	
	public void bindDBManager(DBManagerIF _dbManager){
		this.dbManager = _dbManager;
	}
	
	public void unbindDBManager(DBManagerIF _dbManager){
		if (this.dbManager.equals(_dbManager))
			this.dbManager = null;
	}
}
