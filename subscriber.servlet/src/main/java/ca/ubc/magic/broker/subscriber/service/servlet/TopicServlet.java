package ca.ubc.magic.broker.subscriber.service.servlet;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.ds.Client;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.http.ExtendedHttpServlet;
import ca.ubc.magic.broker.http.ServletErrorResponse;
import ca.ubc.magic.broker.impl.ParamCheckHelper;

public class TopicServlet extends ExtendedHttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final String  SERVLET_ALIAS = "/osgibroker/topic";
	
	private static final String  SERVER_BASED_QUERY = "server";
	private static final String  CLIENT_BASED_QUERY = "client";
	
	private DBManagerIF dbManager;
	private SubscriberIF subscriber;
	
	private static final Logger logger = Logger.getLogger(TopicServlet.class);
	
	public void init(){
		
		// The aliases to setup the servlet
		this.setServletAlias(SERVLET_ALIAS);
		
		bindDBManager(ContextProvider.getInstance().getDBManager());
		bindSubscriber(ContextProvider.getInstance().getSubscriber());
		
	}
	
	public void destroy(){
		unbindDBManager(ContextProvider.getInstance().getDBManager());
		unbindSubscriber(ContextProvider.getInstance().getSubscriber());
	}
	
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		
		try {
			
			//checking the validity of the initial input to the doPost method
			ParamCheckHelper.doPreCheck(request.getParameterMap());
			
			if (request.getPathInfo() == null)
				throw new BrokerException(HttpServletResponse.SC_BAD_REQUEST, BrokerException.NO_QUERY_DEFINED);
			
			// No subscriber is bound to the servlet
			if (this.subscriber == null)
				throw new BrokerException(HttpServletResponse.SC_NO_CONTENT, BrokerException.NO_SUBSCRIBER_FOUND);
			
			String[] queryCommand = request.getPathInfo().split("/");
			
			if (queryCommand.length > 2)
				throw new BrokerException(HttpServletResponse.SC_BAD_REQUEST, BrokerException.BAD_QUERY);
			
			if (queryCommand[1].equals(ExtendedHttpServlet.EVENTS_QUERY)){
				logger.debug("Client requested a query on events");
				queryEvents(request, response);
			}
			if (queryCommand[1].equals(ExtendedHttpServlet.CLIENTS_QUERY)){
				logger.debug("Client requested a query on clients");
				queryClients(request, response);
			}
			
		}catch(Exception e){
			logger.error(e.getMessage());
			logger.error(e.getStackTrace());
			ServletErrorResponse.doSendError(response, e);
		}
	}
	
	public  void doPost (HttpServletRequest request, HttpServletResponse response){
		doGet(request, response);
	}
	
	
	@SuppressWarnings("unchecked")
	private void queryEvents(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		List<Event> eventList = null;
		boolean isTimeFrameQuery = ParamCheckHelper.doPrecheckTimeFrameQuery(request.getParameterMap());
		
		String topic = StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC));
		String querySize = StringEscapeUtils.escapeHtml(request.getParameter(ExtendedHttpServlet.QUERY_SIZE));

		String queryStart   = StringEscapeUtils.escapeHtml(request.getParameter(ExtendedHttpServlet.QUERY_START));
		String queryEnd     = StringEscapeUtils.escapeHtml(request.getParameter(ExtendedHttpServlet.QUERY_END));
		String queryBeforeT = StringEscapeUtils.escapeHtml(request.getParameter(ExtendedHttpServlet.QUERY_BEFORE_TIME));
		String queryAfterT  = StringEscapeUtils.escapeHtml(request.getParameter(ExtendedHttpServlet.QUERY_AFTER_TIME));
		String queryBeforeE = StringEscapeUtils.escapeHtml(request.getParameter(ExtendedHttpServlet.QUERY_BEFORE_EVENT));
		String queryAfterE  = StringEscapeUtils.escapeHtml(request.getParameter(ExtendedHttpServlet.QUERY_AFTER_EVENT));
		String queryBase    = StringEscapeUtils.escapeHtml(request.getParameter(ExtendedHttpServlet.QUERY_BASE));
		
		// checks whether a time query is requested
		if (isTimeFrameQuery){
			
			//check if querStart is null and if so, consider the current time as the start for the query
			if (queryStart == null)
				queryStart = Long.toString(System.currentTimeMillis());
			
			// a null or server-based query would return the query based on the specified time by the server
			if (queryBase == null || queryBase.equals(TopicServlet.SERVER_BASED_QUERY)){
				
				if (queryEnd != null)
					eventList = dbManager.getEventStore().getFrameServerTime(topic, Long.parseLong(queryStart), Long.parseLong(queryEnd));
				else if (queryBeforeE != null)
					eventList = dbManager.getEventStore().getBeforeEServer(topic, Long.parseLong(queryStart), Integer.parseInt(queryBeforeE));
				else if (queryAfterE != null)
					eventList = dbManager.getEventStore().getAfterEServer(topic, Long.parseLong(queryStart), Integer.parseInt(queryAfterE));
				else if (queryBeforeT != null)
					eventList = dbManager.getEventStore().getBeforeTServer(topic, Long.parseLong(queryStart), Long.parseLong(queryBeforeT));
				else if (queryAfterT != null)
					eventList = dbManager.getEventStore().getAfterTServer(topic, Long.parseLong(queryStart), Long.parseLong(queryAfterT));
				
			// a client based query would return the query based on the specified time by the client
			}else if (queryBase.equals(TopicServlet.CLIENT_BASED_QUERY)){
				
				if (queryEnd != null)
					eventList = dbManager.getEventStore().getFrameClientTime(topic, Long.parseLong(queryStart), Long.parseLong(queryEnd));
				else if (queryBeforeE != null)
					eventList = dbManager.getEventStore().getBeforeEClient(topic, Long.parseLong(queryStart), Integer.parseInt(queryBeforeE));
				else if (queryAfterE != null)
					eventList = dbManager.getEventStore().getAfterEClient(topic, Long.parseLong(queryStart), Integer.parseInt(queryAfterE));
				else if (queryBeforeT != null)
					eventList = dbManager.getEventStore().getBeforeTClient(topic, Long.parseLong(queryStart), Long.parseLong(queryBeforeT));
				else if (queryAfterT != null)
					eventList = dbManager.getEventStore().getAfterTClient(topic, Long.parseLong(queryStart), Long.parseLong(queryAfterT));
			}else
				throw new BrokerException(HttpServletResponse.SC_BAD_REQUEST, BrokerException.INVALID_QUERY_BASE);
			
		}else{
			if (querySize == null)
				eventList = this.dbManager.getEventStore().getLastEvents(topic);
			else
				eventList = this.dbManager.getEventStore().getLastEvents(topic, Integer.parseInt(querySize));
		}
		
		response.setHeader("Cache-Control", "no-cache");
		
		if (request.getParameter(FORMAT)==null || request.getParameter(FORMAT).equalsIgnoreCase("xml"))
		{
			response.setContentType("text/xml");
			response.getWriter().write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			response.getWriter().write("<events>");
			if (eventList != null)
				for (Event evt : eventList)
					response.getWriter().write(evt.toString());
			response.getWriter().write("</events>");
		}
		else if (request.getParameter(FORMAT).equalsIgnoreCase("json"))
		{
			StringBuilder sb = new StringBuilder();
			
			response.setContentType("application/json");
			
			sb.append("<events>");
			if (eventList != null)
				for (Event evt : eventList)
					sb.append(evt.toString());
			sb.append("</events>");
			
			//convert xml to json
			String xml = sb.toString();
			XMLSerializer xmlSerializer = new XMLSerializer();  
			JSON json = xmlSerializer.read( xml );  
			response.getWriter().write( json.toString() );
		}
	}
	
	private void queryClients(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String topic = StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC));
		
		logger.debug("queryClients topic [" + topic + "]");
		
		List<Client> clientList = this.dbManager.getClientStore().getClientsByTopic(topic);
		
		response.setHeader("Cache-Control", "no-cache");
		
		if (request.getParameter(FORMAT)==null || request.getParameter(FORMAT).equalsIgnoreCase("xml"))
		{
			response.setContentType("text/xml");
			response.getWriter().write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			response.getWriter().write("<clients>");
			if (clientList != null)
				for (Client client : clientList){
					Set<String> clientPropNameSet = client.getPropertyNames();
					response.getWriter().write("<client>");
					for(String clientPropName : clientPropNameSet){
						response.getWriter().write("<" + clientPropName + ">");
						response.getWriter().write((String)client.getProperty(clientPropName));
						response.getWriter().write("</" + clientPropName + ">");
					}
					response.getWriter().write("</client>");
				}
			response.getWriter().write("</clients>");
		}
		else if (request.getParameter(FORMAT).equalsIgnoreCase("json"))
		{
			StringBuilder sb = new StringBuilder();
			
			response.setContentType("application/json");
			sb.append("<clients>");
			if (clientList != null)
				for (Client client : clientList){
					Set<String> clientPropNameSet = client.getPropertyNames();
					sb.append("<client>");
					for(String clientPropName : clientPropNameSet){
						sb.append("<" + clientPropName + ">");
						sb.append((String)client.getProperty(clientPropName));
						sb.append("</" + clientPropName + ">");
					}
					sb.append("</client>");
				}
			sb.append("</clients>");
			
			//convert xml to json
			String xml = sb.toString();
			XMLSerializer xmlSerializer = new XMLSerializer();  
			JSON json = xmlSerializer.read( xml );  
			response.getWriter().write( json.toString() );
		}
	}
	
	public void bindDBManager(DBManagerIF _dbManager){
		this.dbManager = _dbManager;
	}
	
	public void unbindDBManager(DBManagerIF _dbManager){
		if (this.dbManager.equals(_dbManager))
			this.dbManager = null;
	}
	
	public void bindSubscriber(SubscriberIF _subscriber){
		this.subscriber = _subscriber;
	}
	
	public void unbindSubscriber(SubscriberIF _subscriber){
		if (this.subscriber.equals(_subscriber))
			this.subscriber = null;
	}

}
