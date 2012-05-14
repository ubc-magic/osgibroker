package ca.ubc.magic.broker.subscriber.service.servlet;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.cache.CacheIF;
import ca.ubc.magic.broker.api.ds.TopicContent;
import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.cache.content.ContentManager;
import ca.ubc.magic.broker.http.ExtendedHttpServlet;
import ca.ubc.magic.broker.http.ServletErrorResponse;
import ca.ubc.magic.broker.impl.ParamCheckHelper;

public class ContentServlet extends ExtendedHttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private final String  SERVLET_ALIAS = "/osgibroker/content";
	
	private DBManagerIF dbManager;
	private CacheIF		cache;
	
	private static final Logger logger = Logger.getLogger( ContentServlet.class );
	
	private static final long MAX_FILE_SIZE = 50000000;
	static final private int THRESHOLD = 4096;
	
	public void init() {
		
		this.setServletAlias(SERVLET_ALIAS);
		
		bindDBManager(ContextProvider.getInstance().getDBManager());
		bindCache(ContextProvider.getInstance().getCache());
	}
	
	public void destroy() {
		unbindDBManager(ContextProvider.getInstance().getDBManager());
		unbindCache(ContextProvider.getInstance().getCache());
	}
	
	
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		
		try{
			
			ParamCheckHelper.doPreCheck(request.getParameterMap());
			
			logger.debug("State is being sent to the requesting clinet with the clientID: " + 
					((StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID)) == null) ? "unknown" :
						StringEscapeUtils.escapeHtml(request.getParameter(RemoteClientIF.CLIENT_ID))));
			
			String method = StringEscapeUtils.escapeHtml(request.getParameter(ExtendedHttpServlet.METHOD));
			String contentID = StringEscapeUtils.escapeHtml(request.getParameter(TopicContent.CONTENT_ID));
			String topic   = StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC));

			// support clients that can't PUT or DELETE using _method parameter
			if (method != null) {
				
				logger.debug("Action was taken for method call **" + method + "** in POST");
				if (method.equals(ExtendedHttpServlet.METHOD_DELETE)) {
					this.doDelete(request, response);
					return;
				}
			}
			
			response.setHeader("Cache-Control", "no-cache");
			
			
			ContentManager contentManager = (ContentManager) cache.getCacheEntry(ContentManager.class);
			
			if (request.getParameter(this.FORMAT)==null || request.getParameter(ExtendedHttpServlet.FORMAT).equalsIgnoreCase("xml"))
			{
				response.setContentType("text/xml");
				
				if (contentID == null)
					contentManager.writeXML(request.getParameter(SubscriberIF.TOPIC), response.getOutputStream());
				else
					contentManager.writeXMLElement(topic, contentID, response.getOutputStream());
			}
			else if (((String) request.getParameter(ExtendedHttpServlet.FORMAT)).equalsIgnoreCase("json"))
			{
				response.setContentType("application/json");
				
				if (contentID == null)
					contentManager.writeJSON(request.getParameter(SubscriberIF.TOPIC), response.getOutputStream());
				else
					contentManager.writeJSONElement(topic, contentID, response.getOutputStream());
			}

			response.getOutputStream().flush();
			
		}catch(Exception e){
			ServletErrorResponse.doSendError(response, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			
			// Create a factory for disk-based file items
			DiskFileItemFactory factory = new DiskFileItemFactory();

			// maximum size stored in memory
			factory.setSizeThreshold(THRESHOLD);

			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			// maximum size before a FileUploadException will be thrown
			upload.setSizeMax(MAX_FILE_SIZE);

			// get the file items as a list and set up an iterator
			List<FileItem> fileItems = null;			
			
			try {
				fileItems = (List<FileItem>)upload.parseRequest(request);
			} catch (FileUploadException e) {
				throw new BrokerException("FileUploadException"); 
			}
			
			// get the topic where content is going to be uploaded
			String topic = request.getParameter(SubscriberIF.TOPIC);
			
			String clientID = request.getParameter(RemoteClientIF.CLIENT_ID);
			
			if (topic == null) {
				//Try and find the channel parameter in the form fields
				Iterator<FileItem> i = (Iterator<FileItem>) fileItems.iterator();
				FileItem fi = null;
				
				// search form data for channel
				while (i.hasNext()) {
					fi = i.next();
					
					if (fi.isFormField()) {
						if (fi.getFieldName().equals("topic"))
							topic = fi.getString();
						else if (fi.getFieldName().equals("clientID"))
							clientID = fi.getString();
					} 
				}
				if (topic == null)
					throw new BrokerException (HttpServletResponse.SC_NOT_FOUND, BrokerException.NO_TOPIC_DEFINED);
				if (clientID == null)
					throw new BrokerException (HttpServletResponse.SC_NOT_FOUND, BrokerException.NO_CLIENT_ID_DEFINED);
				
				doAddContent(topic, clientID, fileItems);
				
			}		
			
			// get the file path to the directory to store content
			// this may be changed since it is deleted every time the app is redeployed
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} catch (Exception e) {
			ServletErrorResponse.doSendError(response, e);
		} finally {
			// Close the performance monitor
		}
	}
	
	@SuppressWarnings("unchecked")
	public void doDelete(HttpServletRequest request, HttpServletResponse response) {
		
		try{
			
			//checking the validity of the initial input to the doPost method
			ParamCheckHelper.doPreCheck(request.getParameterMap());
			
			String contentID = StringEscapeUtils.escapeHtml(request.getParameter(TopicContent.CONTENT_ID));
			if (contentID == null){
				logger.debug("Deleting all states for the topic: " + request.getParameter(SubscriberIF.TOPIC));
				doMassRemoveContent(StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC)));
			}
			else{
				logger.debug("Deleting state " + contentID + " for the topic: " + request.getParameter(SubscriberIF.TOPIC));
				doRemoveContent(StringEscapeUtils.escapeHtml(request.getParameter(SubscriberIF.TOPIC)), contentID);
			}
			
		}catch(Exception e){
			ServletErrorResponse.doSendError(response, e);
		}
		
	}
	
	public void doAddContent(String topic, String clientID, List<FileItem> fis) throws BrokerException{
		
		ContentManager contentManager = (ContentManager) cache.getCacheEntry(ContentManager.class);
		contentManager.updateCacheObject(topic, clientID, fis);
	}
	
	public void doRemoveContent(String topic, String contentID) throws BrokerException{
		
		((TopicContent) cache.getCacheEntry(ContentManager.class).getCacheObject(topic)).removeContent(contentID);
	}
	
	public void doMassRemoveContent(String topic) throws BrokerException {
		cache.getCacheEntry(ContentManager.class).removeCacheObject(topic);
	}
	
	public void bindDBManager(DBManagerIF _dbManager){
		this.dbManager = _dbManager;
	}
	
	public void unbindDBManager(DBManagerIF _dbManager){
		if (this.dbManager.equals(_dbManager))
			this.dbManager = null;
	}
	
	public void bindCache(CacheIF _cache){
		this.cache = _cache;
	}
	
	public void unbindCache(CacheIF _cache){
		if (this.cache.equals(_cache))
			this.cache = null;
	}
}
