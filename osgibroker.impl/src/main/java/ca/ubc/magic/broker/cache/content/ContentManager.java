package ca.ubc.magic.broker.cache.content;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.cache.CacheElementIF;
import ca.ubc.magic.broker.api.cache.CacheEntryIF;
import ca.ubc.magic.broker.api.cache.CacheElementIF.Status;
import ca.ubc.magic.broker.api.ds.Attribute;
import ca.ubc.magic.broker.api.ds.CacheObject;
import ca.ubc.magic.broker.api.ds.ContentObjElement;
import ca.ubc.magic.broker.api.ds.TopicContent;
import ca.ubc.magic.broker.api.notification.NotificationHandlerIF;
import ca.ubc.magic.broker.http.HttpWrapper;

public class ContentManager implements CacheEntryIF {
	
	private static final Logger logger = Logger.getLogger( ContentManager.class );
	
	private static String CONTENT_MANAGER_NAME = "BasicContentManager"; 
	private NotificationHandlerIF notificationHandler;
	
	private static String defaultNetworkIF="eth0";
	private static int    httpPort = 8800;
//	public static final String CONETNT_NAME = "contentName";
	
	private Date lastAccessed = null;
	private int  expiresAfter = -1;
	
	private HashMap<String, CacheObject> contentMap;
	
	public ContentManager(NotificationHandlerIF _notificationHandler){
		this(_notificationHandler, CONTENT_MANAGER_NAME, -1);
	}
	
	public ContentManager(NotificationHandlerIF _notificationHandler, String _contentManagerName, int _expiresAfterMilliSecond){
		
		CONTENT_MANAGER_NAME = _contentManagerName;
		contentMap = new HashMap<String, CacheObject>();
		notificationHandler = _notificationHandler;
		
		this.expiresAfter = _expiresAfterMilliSecond;
		this.lastAccessed = new Date();
	}

	public String[] getCacheObjectTopics() {
		String[] topics = new String[contentMap.keySet().size()];
		return contentMap.keySet().toArray(topics);
	}
	
	public CacheObject createCacheObject(String topic){
		
		this.setLastAccessed (new Date());
		CacheObject topicContentCacheObject = contentMap.get(topic);
		
		if (topicContentCacheObject == null){
			
			topicContentCacheObject = new TopicContent(topic);
			topicContentCacheObject.addNotificationListener(notificationHandler);
			contentMap.put(topic, topicContentCacheObject);
		}else
			logger.warn("createCacheObject unsuccessful. There is a cache object already registered under topic: " + topic);
		
		return topicContentCacheObject;
		
	}
	
	public boolean existsCacheObject(String topic){

		this.setLastAccessed (new Date());
		if (contentMap.get(topic) != null)
			return true;
		
		return false;
	}
	
	public CacheObject getCacheObject(String topic) {
		this.setLastAccessed (new Date());
		
		TopicContent content = (TopicContent) contentMap.get(topic);
		if (content == null)
			return createCacheObject(topic);
			
		return content;
	}
	
	public CacheObject updateCacheObject(String topic, CacheObject cacheObj) {
		
		this.setLastAccessed (new Date());
		
		if (cacheObj != null)
			cacheObj.addNotificationListener(notificationHandler);
		
		TopicContent oldState = (TopicContent) contentMap.get(topic);
		
		contentMap.put(topic, (TopicContent) cacheObj);
		return oldState;
	}
	
	
	@SuppressWarnings("unchecked")
	public void updateCacheObject(String topic, String clientID, List fis) throws BrokerException{
		
		Iterator fiterator = fis.iterator();
		
		while (fiterator.hasNext()){
			
			FileItem fi = (FileItem) fiterator.next();
			
			if (fi.isFormField())
				continue;
		
			try {
				
				List<Attribute> topicContentAttrList = new ArrayList<Attribute>();
				
				// creating the url attribute
				httpPort = (HttpWrapper.HTTP_PORT == 0) ? 8080 : HttpWrapper.HTTP_PORT; 
				String url = "http://"+ getHost(defaultNetworkIF) + ":" + httpPort + "/content/" + fi.getName();
				
				//adding attributes to the list
				topicContentAttrList.add(new Attribute("url", url));
				topicContentAttrList.add(new Attribute(RemoteClientIF.CLIENT_ID, clientID));
				topicContentAttrList.add(new Attribute(SubscriberIF.TOPIC, topic));
				
				TopicContent topicContent = (TopicContent) this.getCacheObject(topic);
				topicContent.setContentEntity(fi, topicContentAttrList);
				
				this.updateCacheObject(topic, topicContent);
				
			} catch (Exception e) {
				throw new BrokerException("Error when adding content information to Topic: " + topic, e);
			}
		}
	}

	
	/**
	 * The removeCacheObject method removes the list of all content elememnts from a topic. But further to
	 * this, it also removes the files associated with the content model of a topic. 
	 * 
	 * <i>Note:</i> Since the files are removed, the returned CacheObject element does not hold any reference to the 
	 * files related to the objects. Any query for the files, if they don't exist would result in a returning null.
	 */
	@SuppressWarnings("unchecked")
	public CacheObject removeCacheObject(String topic) throws BrokerException {
		this.setLastAccessed (new Date());
		
		TopicContent contentState = (TopicContent) contentMap.get(topic);
		
		String[] contentNames = contentState.getElementAttributeNames();
		
//		CacheObject copyTopicContent = new TopicContent(topic);
		for (String contentName : contentNames){
			
//			CacheElementIF contentElement = 
				contentState.removeContent(contentName);
//			((TopicContent) copyTopicContent).setElement(contentName, contentElement);
		}
		
//		return copyTopicContent;
		return null;
	}

	public int getExpiresAfter() {
		return -1;
	}

	public Date getLastAccessed() {
		return lastAccessed;
	}

	public boolean isExpired() {
		
		return (expiresAfter == 0) ? true : false;
	}

	public void setExpiresAfter(int milliseconds) {
		expiresAfter = milliseconds;
	}

	public void setLastAccessed(Date dateTime) {
		lastAccessed = dateTime;
	}
	
	public void updateNetworkIF (String networkIF) throws BrokerException, SocketException{
		if (defaultNetworkIF.equals(networkIF))
			return;
		
		logger.info("Content URL Address Updating ....");
		
		// updating the networkIF and the associated host name
		defaultNetworkIF = networkIF;
		String newHost = getHost(networkIF);
		
		String[] topics = this.getCacheObjectTopics();
		for (String topic : topics){	
			TopicContent topicContent = (TopicContent) this.getCacheObject(topic);
			for (CacheElementIF topicContentObjMap : topicContent.getAllContent().values()){
				Attribute attr = topicContentObjMap.get("url");
				String host = attr.getValue().substring(attr.getValue().indexOf("http://") + 7, 
						attr.getValue().indexOf(":"+Integer.toString(httpPort)));
				attr.setValue(attr.getValue().replace(host, newHost));
				topicContentObjMap.setCacheElemStatus(Status.updated);
			}
		}
		logger.info("[DONE]");
	}
	
	private String getHost(String networkIF) throws SocketException, BrokerException{
		
		for (Enumeration<NetworkInterface> ifaces = 
			NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();){
			
			NetworkInterface iface = ifaces.nextElement();
			if (!iface.getName().equals(networkIF))
				continue;
			for (Enumeration<InetAddress> addresses =
                iface.getInetAddresses();
              addresses.hasMoreElements(); )
	         {
				InetAddress address = addresses.nextElement();
				
				if (address != null && (address instanceof Inet4Address))
					return address.getHostAddress();
	         }
			break;
		}
		throw new BrokerException(BrokerException.NO_VALID_NETWORK_IF);
	}

	//TODO nimak -	This part should be done ASAP
	public void clean() {
		
		String[] topics = this.getCacheObjectTopics();
		
		for (String topic : topics){
			
			TopicContent topicContent = (TopicContent) this.getCacheObject(topic);
			topicContent.clean();
		}
	}
	
	public void writeXML(String topic, OutputStream stream) throws BrokerException, UnknownHostException{
		
		PrintWriter out = new PrintWriter(stream);
		// output topic state
		out.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		out.print("<contents>");
		
		TopicContent topicContent = (TopicContent) this.getCacheObject(topic);
		
		if (topicContent == null)
			throw new BrokerException("Unable to find the state for: " + topic);
		
		String[] contentIDs = topicContent.getElementAttributeNames();
		for (String contentID : contentIDs){
			
			out.print("<content id=\"" + contentID + "\">");
			
			ContentObjElement content = topicContent.getContent(contentID);
			
			List<Attribute> attrs = content.getList();
			Iterator itr = attrs.iterator();
			while (itr.hasNext()){
				Attribute attr = (Attribute) itr.next();
				out.print("<" + attr.getName() + ">");
				out.print(attr.getValue());
				out.print("</" + attr.getName() + ">");
			}
			
			out.print("</content>");
		}
		
		out.print("</contents>");
		out.flush();
	}
	
	public void writeJSON(String topic, OutputStream stream) throws BrokerException, UnknownHostException{
		
		PrintWriter out = new PrintWriter(stream);
		StringBuilder sb = new StringBuilder();
		
		// output topic state
		sb.append("<contents>");
		
		TopicContent topicContent = (TopicContent) this.getCacheObject(topic);
		
		if (topicContent == null)
			throw new BrokerException("Unable to find the state for: " + topic);
		
		String[] contentIDs = topicContent.getElementAttributeNames();
		for (String contentID : contentIDs){
			
			sb.append("<content id=\"" + contentID + "\">");
			
			ContentObjElement content = topicContent.getContent(contentID);
			
			List<Attribute> attrs = content.getList();
			Iterator itr = attrs.iterator();
			while (itr.hasNext()){
				Attribute attr = (Attribute) itr.next();
				sb.append("<" + attr.getName() + ">");
				sb.append(attr.getValue());
				sb.append("</" + attr.getName() + ">");
			}
			
			sb.append("</content>");
		}
		
		sb.append("</contents>");
		
		//convert xml to json
		String xml = sb.toString();
		XMLSerializer xmlSerializer = new XMLSerializer();  
		JSON json = xmlSerializer.read( xml );  
		out.write( json.toString() );
		
		out.flush();
	}
	
	
	public void writeXMLElement(String topic, String contentID, OutputStream stream) throws BrokerException {
		
		PrintWriter out = new PrintWriter(stream);
		out.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		out.print("<contents>");
		
		TopicContent topicContent = (TopicContent) this.getCacheObject(topic);
		
		if (topicContent == null){
			out.print("</contents>");
			throw new BrokerException("Unable to find the state for: " + topic);
		}
		
		String[] contentIDs = topicContent.getElementAttributeNames();
		
		for (String tmpContentID : contentIDs){
			if (!tmpContentID.equals(contentID))
				continue;
			
			out.print("<content id=\"" + contentID + "\">");
			
			ContentObjElement content = topicContent.getContent(contentID);
			
			List<Attribute> attrs = content.getList();
			Iterator itr = attrs.iterator();
			while (itr.hasNext()){
				Attribute attr = (Attribute) itr.next();
				out.print("<" + attr.getName() + ">");
				out.print(attr.getValue());
				out.print("</" + attr.getName() + ">");
			}
			out.print("</content>");
		}
		
		out.print("</contents>");
		out.flush();
	}

	public void writeJSONElement(String topic, String contentID, OutputStream stream) throws BrokerException {
		
		PrintWriter out = new PrintWriter(stream);
		StringBuilder sb = new StringBuilder();
		
		sb.append("<contents>");
		
		TopicContent topicContent = (TopicContent) this.getCacheObject(topic);
		
		if (topicContent == null){
			String xml = "</contents>";
			XMLSerializer xmlSerializer = new XMLSerializer();  
			JSON json = xmlSerializer.read( xml );  
			out.write( json.toString() );
			
			throw new BrokerException("Unable to find the state for: " + topic);
		}
		
		String[] contentIDs = topicContent.getElementAttributeNames();
		
		for (String tmpContentID : contentIDs){
			if (!tmpContentID.equals(contentID))
				continue;
			
			sb.append("<content id=\"" + contentID + "\">");
			
			ContentObjElement content = topicContent.getContent(contentID);
			
			List<Attribute> attrs = content.getList();
			Iterator itr = attrs.iterator();
			while (itr.hasNext()){
				Attribute attr = (Attribute) itr.next();
				sb.append("<" + attr.getName() + ">");
				sb.append(attr.getValue());
				sb.append("</" + attr.getName() + ">");
			}
			sb.append("</content>");
		}
		
		sb.append("</contents>");
		
		//convert xml to json
		String xml = sb.toString();
		XMLSerializer xmlSerializer = new XMLSerializer();  
		JSON json = xmlSerializer.read( xml );  
		out.write( json.toString() );
		
		out.flush();
	}
	
	public void addNotificationListener(NotificationHandlerIF notification) {
		this.notificationHandler = notification;
	}

	public void removeNotificationListener() {
		this.notificationHandler = null;
	}
}
