package ca.ubc.magic.broker.subscriber.service.servlet;

import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.cache.CacheElementIF;
import ca.ubc.magic.broker.api.cache.CacheIF;
import ca.ubc.magic.broker.api.ds.ElementMap;
import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.cache.state.StateManager;
import ca.ubc.magic.broker.http.ExtendedHttpServlet;
import ca.ubc.magic.broker.http.ServletErrorResponse;
import ca.ubc.magic.broker.impl.ParamCheckHelper;

public class StateServlet extends ExtendedHttpServlet {
	
	private static final long serialVersionUID = 1L;
	private final String  SERVLET_ALIAS = "/osgibroker/state";
	
	private DBManagerIF  dbManager;
	private CacheIF cache;
	
	private static final Logger logger = Logger.getLogger(StateServlet.class);
	
	public void init(){
		
		// The aliases to setup the servlet
		this.setServletAlias(SERVLET_ALIAS);
		
		bindDBManager(ContextProvider.getInstance().getDBManager());
		bindCache(ContextProvider.getInstance().getCache());
	}
	
	public void destroy(){
		
		unbindDBManager(ContextProvider.getInstance().getDBManager());
		unbindCache(ContextProvider.getInstance().getCache());
	}
	
	
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		
		try{
			
			//checking the validity of the initial input to the doPost method
			ParamCheckHelper.doPreCheck(request.getParameterMap());
			
			logger.debug("State is being sent to the requesting clinet with the clientID: " + 					
						StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)));
			
			String method  = StringEscapeUtils.escapeHtml(request.getParameter(ExtendedHttpServlet.METHOD));
			String stateID = StringEscapeUtils.escapeHtml(request.getParameter(ElementMap.STATE_ID));
			String topic   = StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC));

			// support clients that can't PUT or DELETE using _method parameter
			if (method != null) {
				
				logger.debug("Action was taken for method call **" + method + "** in POST");
				if (method.equals(ExtendedHttpServlet.METHOD_DELETE)) {
					this.doDelete(request, response);
					return;
				} else if (method.equals(ExtendedHttpServlet.METHOD_PUT)) {
					this.doPut(request, response);
					return;
				}
			}
			
			response.setHeader("Cache-Control", "no-cache");
			
			StateManager stateManager = (StateManager) cache.getCacheEntry(StateManager.class);
			
			if (request.getParameter(ExtendedHttpServlet.FORMAT)==null || 
				request.getParameter(ExtendedHttpServlet.FORMAT).equalsIgnoreCase("xml"))
			{
				response.setContentType("text/xml");
				
				if (stateID == null)
					stateManager.writeXML(request.getParameter(SubscriberIF.TOPIC), response.getOutputStream());
				else
					stateManager.writeXMLElement(topic, stateID, response.getOutputStream());
			}
			else if (request.getParameter(ExtendedHttpServlet.FORMAT).equalsIgnoreCase("json"))
			{
				response.setContentType("application/json");
				
				if (stateID == null)
					stateManager.writeJSON(request.getParameter(SubscriberIF.TOPIC), response.getOutputStream());
				else
					stateManager.writeJSONElement(topic, stateID, response.getOutputStream());
			}
			response.getOutputStream().flush();
		
		}catch(Exception e){
			ServletErrorResponse.doSendError(response, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void doDelete(HttpServletRequest request, HttpServletResponse response){
		try{
			
			//checking the validity of the initial input to the doPost method
			ParamCheckHelper.doPreCheck(request.getParameterMap());
			
			String stateID = request.getParameter(ElementMap.STATE_ID);
			if (stateID == null){
				logger.debug("Deleting all states for the topic: " + request.getParameter(SubscriberIF.TOPIC));
				cache.getCacheEntry(StateManager.class).removeCacheObject(request.getParameter(SubscriberIF.TOPIC));
			}
			else{
				logger.debug("Deleting state " + stateID + " for the topic: " + request.getParameter(SubscriberIF.TOPIC));
				((ElementMap) cache.getCacheEntry(StateManager.class).getCacheObject(
						request.getParameter(SubscriberIF.TOPIC))).removeState(stateID);
			}
			
		}catch(Exception e){
			ServletErrorResponse.doSendError(response, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response){
	
		try{
			
			//checking the validity of the initial input to the doPost method
			ParamCheckHelper.doPreCheck(request.getParameterMap());
			
			String method = request.getParameter(ExtendedHttpServlet.METHOD);

			// support clients that can't PUT or DELETE using _method parameter
			if (method != null) {
				
				logger.debug("Action was taken for method call **" + method + "** in POST");
				if (method.equals(ExtendedHttpServlet.METHOD_DELETE)) {
					this.doDelete(request, response);
					return;
				} else if (method.equals(ExtendedHttpServlet.METHOD_PUT)) {
					this.doPut(request, response);
					return;
				}
			}
			
			this.doManageState(request, response);
			
		}catch(Exception e){
			ServletErrorResponse.doSendError(response, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void doPut(HttpServletRequest request, HttpServletResponse response){
		
		try{
			
			//checking the validity of the initial input to the doPost method
			ParamCheckHelper.doPreCheck(request.getParameterMap());
			
			this.doManageState(request, response);
			
		}catch(Exception e){
			ServletErrorResponse.doSendError(response, e);
		}
	}
	
	private void doManageState(HttpServletRequest request, HttpServletResponse response){
		
		Enumeration<?> iter = request.getParameterNames();
		String topic   = StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC));
		String stateID = StringEscapeUtils.escapeHtml(request.getParameter(ElementMap.STATE_ID));
		
		ElementMap stateMap = (ElementMap) cache.getCacheEntry(StateManager.class).getCacheObject(topic);
		while (iter.hasMoreElements()) {
			String paramName = (String) iter.nextElement();
			if (!paramName.equals(SubscriberIF.TOPIC) && !paramName.equals(ExtendedHttpServlet.METHOD) && !paramName.equals(ElementMap.STATE_ID))
				stateMap.setUntypedAttribute(paramName, StringEscapeUtils.escapeHtml(request.getParameter(paramName)));
		}
		cache.getCacheEntry(StateManager.class).updateCacheObject(topic, stateMap);
		logger.debug("ManageState is called and is just updated");
	}
	
	public void bindCache(CacheIF _cache){
		this.cache = _cache;
	}
	
	public void unbindCache(CacheIF _cache){
		if (this.cache.equals(_cache))
			this.cache = null;
	}
	
	public void bindDBManager(DBManagerIF _dbManager){
		this.dbManager = _dbManager;
	}
	
	public void unbindDBManager(DBManagerIF _dbManager){
		if (this.dbManager.equals(_dbManager))
			this.dbManager = null;
	}
}
