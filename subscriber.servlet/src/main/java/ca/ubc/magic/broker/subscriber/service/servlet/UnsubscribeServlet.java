package ca.ubc.magic.broker.subscriber.service.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.ds.Client;
import ca.ubc.magic.broker.http.ExtendedHttpServlet;
import ca.ubc.magic.broker.http.ServletErrorResponse;
import ca.ubc.magic.broker.impl.ParamCheckHelper;

public class UnsubscribeServlet extends ExtendedHttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	
	private final String  SERVLET_ALIAS = "/osgibroker/unsubscribe";

	private static final String UNSUBSCRIPTION_DONE_MSG = "Unsubsbscription is done successfully for clientID: ";

	private SubscriberIF subscriber;

	private static final Logger logger = Logger.getLogger(UnsubscribeServlet.class);


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
	 * The method used to unsubscribe the client from the broker. The servlet requires the <i>topic</> and the
	 * <i>clientID</i> to be defined. The <i>topic</i> is used to identify the topic from which the client will
	 * be unsubscribed, and the <i>clientID</i> is the ID for the client to be identified.
	 */
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response){

		try{

			// No subscriber is bound to the servlet
			if (this.subscriber == null)
				throw new BrokerException(HttpServletResponse.SC_NO_CONTENT, BrokerException.NO_SUBSCRIBER_FOUND);

			if (request.getParameter(SubscriberIF.TOPIC) == null){

				ParamCheckHelper.doPrecheckAllowTopicNull(request.getParameterMap());

				try{
					doUnsubscribeAll(request, response);
				}catch(Exception e){

					logger.info("Unsubscribe All Executed.");
					//Message to be printed when the client is successfully unsubscribed
					response.getWriter().write("Unsubscription from all topics executed." +
							StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)));
				}
			}else{

				//checking the validity of the initial input to the doPost method
				ParamCheckHelper.doPreCheck(request.getParameterMap());
				doUnsubscribe(request, response);
			}

		}catch (Exception e){
			ServletErrorResponse.doSendError(response, e);
		}

	}

	/**
	 * similar to doGet
	 */
	public void doDelete(HttpServletRequest request, HttpServletResponse response){

		this.doGet(request, response);
	}

	/**
	 * The doUnsubscribe method receives the request and the response streams for a remote client
	 * and removes the client from the list of clients subscribed with the broker.
	 *
	 * @param request			The request received from the remote requesting client
	 * @param response			The response to be sent to the remote requesting client
	 * @throws Exception 		Exception is thrown if there is a problem with removing listener
	 */
	private void doUnsubscribe(HttpServletRequest request, HttpServletResponse response) throws Exception{

		logger.debug("unsubscribing client [" + StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)) + "]" +
				"from topic [" + StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC)) ); 
		
		RemoteClientIF client = subscriber.getClient(
				StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)),
				StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC)));

		if (client == null){
			logger.debug("Client trying to subscribe: " +
					StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)));
			logger.debug("Topic for the client: " +
					StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC)));
			throw new BrokerException(HttpServletResponse.SC_NOT_FOUND,
					BrokerException.NO_UNSUBSCRIPTION_POSSIBLE);
		}

		this.subscriber.removeListener(client,
				StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC)));

		//Message to be printed when the client is successfully unsubscribed
		response.getWriter().write(UNSUBSCRIPTION_DONE_MSG +
				StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)));
		logger.debug("unsubscription done for client [" + 
				StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)) + "]" );
	}

	private void doUnsubscribeAll(HttpServletRequest request, HttpServletResponse response) throws Exception {

		logger.debug("unsubscribing client [" + StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)) + "]"
				+ "from all topics");
		RemoteClientIF client = new ServletClientWrapper();
		client.putProperty(RemoteClientIF.CLIENT_ID, StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)));
		this.subscriber.removeAllListeners(client);

		//Message to be printed when the client is successfully unsubscribed
		response.getWriter().write(UNSUBSCRIPTION_DONE_MSG +
				StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)));

	}

	public void bindSubscriber (SubscriberIF _subscriber){
		this.subscriber = _subscriber;
	}

	public void unbindSubscriber (SubscriberIF _subscriber){
		this.subscriber = null;
	}
}
