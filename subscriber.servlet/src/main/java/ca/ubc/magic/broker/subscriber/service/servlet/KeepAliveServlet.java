package ca.ubc.magic.broker.subscriber.service.servlet;

import javax.servlet.http.HttpServlet;
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

public class KeepAliveServlet extends ExtendedHttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 693030015543774219L;
	
	private final String  SERVLET_ALIAS = "/osgibroker/keepAlive";
	
	private static final String KEEP_ALIVE_DONE_MSG = "Your subscription is successfully renewed for clientID: ";
	
	private static final Logger logger = Logger.getLogger(KeepAliveServlet.class);
	
	private SubscriberIF subscriber;
	
	/**
	 * initializing the servlet by defining the resolution directory, the alias for the servlet, and the resolution
	 * directory alias. Also, the servlet uses the singleton ContextProvider to get the subscriber
	 * found by the service to be registered with the servlet. 
	 * 
	 **/
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
	 * The doGet moethod is responsible for receiving the parameters from the keepAlive servlet 
	 * and renewing the behavior of the client in the DB.
	 */
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		
		try {
			
			String clientID = null;
			String topic    = null;
			long   expiresSeconds = -1;
			
			ParamCheckHelper.doPreCheck(request.getParameterMap());
			
			clientID = StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID));
			topic    = StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC));
			
			if (StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.EXPIRES)) != null)
				expiresSeconds = Long.parseLong(StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.EXPIRES)));
			
			if (StringEscapeUtils.escapeHtml(request.getParameter(request.getParameter(RemoteClientIF.EXPIRES))) != null)
				expiresSeconds = Long.parseLong(StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.EXPIRES)));
			
			if (subscriber.getClient(clientID, topic) == null)
				throw new BrokerException(HttpServletResponse.SC_EXPECTATION_FAILED, BrokerException.NO_CLIENT_ALIVE);
			
			if (expiresSeconds == -1)
				subscriber.renewSubscription(clientID, topic);
			else 
				subscriber.renewSubscription(clientID, topic, expiresSeconds);
			
			response.getWriter().write(KeepAliveServlet.KEEP_ALIVE_DONE_MSG + clientID);
			
		}catch (Exception e){
			ServletErrorResponse.doSendError(response, e);
		}
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		this.doGet(request, response);
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
