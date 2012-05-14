package ca.ubc.magic.broker.subscriber.service.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.http.ExtendedHttpServlet;
import ca.ubc.magic.broker.http.ServletErrorResponse;
import ca.ubc.magic.broker.impl.ParamCheckHelper;

public class SubscribeServlet extends ExtendedHttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String  SERVLET_ALIAS = "/osgibroker/subscribe";
	
	private static final String SUBSCRIPTION_DONE_MSG = "Subsbscription is done successfully for clientID: ";
	
	private SubscriberIF subscriber;
	
	private static final Logger logger = Logger.getLogger(SubscribeServlet.class);
	
	/**
	 * initializing the servlet by defining the resolution directory, the alias for the servlet, and the resolution
	 * directory alias. Also, the servlet uses the singleton ContextProvider to get the subscriber
	 * found by the service to be registered with the servlet. 
	 * 
	 * TODO nimak -	At this point there is no startegy for supporting multiple publishers by one subscriber. This can 
	 * 				be later on added to the system to provide more flexibility in hooking different susbcribers
	 */
	public void init(){
		
		this.setServletAlias(SERVLET_ALIAS);
		
		bindSubscriber(ContextProvider.getInstance().getSubscriber());
		
	}
	
	/**
	 * The destroy method is used to release the pointer to the subscriber service
	 */
	public void destroy(){
		unbindSubscriber(ContextProvider.getInstance().getSubscriber());
	}
	
	/**
	 * To subscribe to the broker. There are two required parameters. The <i>topic</i> under which the client would
	 * like to subscribe, and a unique <i>clientID</i> to be used as the identifier for the client.
	 * 
	 * @parma request	the request received by the servlet when a GET message is issued by the client
	 * @param response	the response to be sent to the remote client issuing the GET message
	 */
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		
		try{
			
			//checking the validity of the initial input to the doPost method
			ParamCheckHelper.doPreCheck(request.getParameterMap());
			
			// No subscriber is bound to the servlet
			if (this.subscriber == null)
				throw new BrokerException(HttpServletResponse.SC_NO_CONTENT, BrokerException.NO_SUBSCRIBER_FOUND);
			
			doSubscribe(request, response);
				
		}catch (Exception e){
			ServletErrorResponse.doSendError(response, e);
		}
		
	}
	
	/**
	 * Similar to doGet
	 * 
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		this.doGet(request, response);
	}
	
	/**
	 * The doSubscribe method receives the request and response streams to the client and
	 * subscribes the client with the servlet. This includes assigning a servletClient to the
	 * remote client as well as adding the client to the list of connected clients. The servletClient
	 * is assigned with a monitoring object that controls the behavior of the client and how
	 * response it is. If the servletClient doesn't show any responsiveness for a long time, the
	 * monitor releases the hook to the client and leaves it for garbage collection. 
	 * 
	 * @param request			The request received from the remote requesting client
	 * @param response			The response to be sent to the remote requesting client
	 * @throws Exception 		Throws exception if there is a problem with adding listener to the subscriber
	 */
	public void doSubscribe(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		if (subscriber.getClient(StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)), 
				StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC))) != null){
			logger.debug("Client trying to subscribe: " + 
					StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)));
			logger.debug("Topic for the client: " + 
					StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC)));
			throw new BrokerException(HttpServletResponse.SC_CONFLICT, 
					BrokerException.CONFILCT_CLIENT);
		}
		
		RemoteClientIF servletClient = null;
		if (StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.EXPIRES)) != null){
			
			long expirationSecs = Long.parseLong(request.getParameter(RemoteClientIF.EXPIRES));
			servletClient = new ServletClientWrapper(expirationSecs);
			
		}else
			servletClient = new ServletClientWrapper();
		servletClient.putProperty(RemoteClientIF.CLIENT_ID, 
				StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)));
		
		if (request.getParameter(RemoteClientIF.URL_SUBSCRIBER) != null)
			servletClient.putProperty(RemoteClientIF.URL_SUBSCRIBER, request.getParameter(RemoteClientIF.URL_SUBSCRIBER));

		// The topic to which the client is going to be subscribed is extracted from the request
		// received by the requesting client
		this.subscriber.addListener(servletClient, 
				StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC)));
		
		// Message when the subscription is complete
		response.getWriter().write(SUBSCRIPTION_DONE_MSG + 
				StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)));
		logger.debug("Subscription done!");
	}
	
	/**
	 * binds the subscriber to the SubscribeServlet
	 * 
	 * @param _subscriber	the subscribe object
	 */
	public void bindSubscriber (SubscriberIF _subscriber){
		this.subscriber = _subscriber;
	}
	
	/**
	 * releases the reference to the subscriber object
	 * 
	 * @param _subscriber	the subscriber to be used
	 */
	public void unbindSubscriber (SubscriberIF _subscriber){
		if (this.subscriber.equals(_subscriber))
			this.subscriber = null;
	}

}
