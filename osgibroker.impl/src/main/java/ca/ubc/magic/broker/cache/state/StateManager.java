package ca.ubc.magic.broker.cache.state;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.cache.CacheElementIF;
import ca.ubc.magic.broker.api.cache.CacheEntryIF;
import ca.ubc.magic.broker.api.ds.Attribute;
import ca.ubc.magic.broker.api.ds.CacheObject;
import ca.ubc.magic.broker.api.ds.ElementMap;
import ca.ubc.magic.broker.api.notification.NotificationHandlerIF;

public class StateManager implements CacheEntryIF {
	
	private static final Logger logger = Logger.getLogger( StateManager.class );
	
	private static String STATE_MANAGER_NAME = "BasicStateManager"; 
	
	private Date lastAccessed;
	private int  expiresAfter = -1;
	
	private NotificationHandlerIF notificationHandler;
	
	private HashMap<String, CacheObject> topicStateMap;
	
	public StateManager(NotificationHandlerIF _notificationHandler){
		this(_notificationHandler, STATE_MANAGER_NAME, -1);
	}
	
	public StateManager(NotificationHandlerIF _notificationHandler, String _stateManagerName, int _expiresAfterMilliSecond){
		
		STATE_MANAGER_NAME = _stateManagerName;
		topicStateMap = new HashMap<String, CacheObject>();
		notificationHandler = _notificationHandler;
		
		this.expiresAfter = _expiresAfterMilliSecond;
		this.lastAccessed = new Date();
	}
	
	public String[] getCacheObjectTopics() {
		String[] topics = new String[topicStateMap.keySet().size()];
		return topicStateMap.keySet().toArray(topics);
	}
	
	public CacheObject	createCacheObject(String topic){
		
		this.setLastAccessed (new Date());
		CacheObject topicStateCacheObject = topicStateMap.get(topic);
		
		if (topicStateCacheObject == null){
			
			topicStateCacheObject = new ElementMap(topic);
			topicStateCacheObject.addNotificationListener(notificationHandler);
			topicStateMap.put(topic, topicStateCacheObject);
			
		}else
			logger.warn("createCacheObject unsuccessful. There is a cache object already registered under topic: " + topic);
		
		return topicStateCacheObject;
	}
	
	public boolean existsCacheObject(String topic){
		
		this.setLastAccessed (new Date());
		if (topicStateMap.get(topic) != null)
			return true;
		
		return false;
	}
	
	public CacheObject getCacheObject(String topic){
		
		this.setLastAccessed (new Date());
		
		ElementMap topicState = (ElementMap) topicStateMap.get(topic);
		if ( topicState == null)
			return createCacheObject(topic);
			
		return topicState;
	}
	
	public CacheObject updateCacheObject(String topic, CacheObject cacheObj) {
		
		this.setLastAccessed (new Date());
		
		if (cacheObj != null)
			cacheObj.addNotificationListener(notificationHandler);
		
		ElementMap oldState = (ElementMap) topicStateMap.get(topic);
		topicStateMap.put(topic, (ElementMap) cacheObj);
		
		return oldState;
	}
	
	public CacheObject  removeCacheObject(String topic) throws BrokerException {
		return topicStateMap.remove(topic);
	}
	
	public int getExpiresAfter() {
		// TODO Auto-generated method stub
		return -1;
	}

	public Date getLastAccessed() {
		
		return lastAccessed;
	}

	public boolean isExpired() {
		
		if (expiresAfter == -1)
			return false;
		
		return false;
	}

	public void setExpiresAfter(int milliseconds) {
		expiresAfter = milliseconds;
	}

	public void setLastAccessed(Date dateTime) {
		
		lastAccessed = dateTime;
		
	}

//	public Attribute[] removeCacheObjectElement(String topic, String stateID) throws BrokerException{
//		
//		this.setLastAccessed (new Date());
//		
//		ElementMap oldState = (ElementMap) topicStateMap.get(topic);
//		return oldState.removeAttribute(stateID);
//	}

	
	public void writeXML(String topic, OutputStream stream) throws BrokerException {
		
		ElementMap topicRec = (ElementMap) topicStateMap.get(topic); 
		
		PrintWriter out = new PrintWriter(stream);
		
		if (topicRec == null){
			out.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			out.print("<state/>");
			out.close();
			return;
		}
		
		// output topic state
		out.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		out.print("<state topic=\"" + topicRec.getName() + "\">");
		if (topicRec != null) {
			String[] names = topicRec.getElementAttributeNames();
			for (String name : names) {
				Attribute[] values = topicRec.getElementAttributes(name);
				for (Attribute attr : values) {
					out.print("<" + name);
					if (!attr.getType().equals("text"))
						out.print(" type='" + attr.getType()+"'");
					out.print(">");
					out.print(StringEscapeUtils.unescapeHtml(attr.getValue()));
					out.print("</" + name + ">");
				}
			}
		}
		out.print("</state>");
		out.flush();
	}
	
	public void writeJSON(String topic, OutputStream stream) throws BrokerException {
		
		ElementMap topicRec = (ElementMap) topicStateMap.get(topic); 
		
		PrintWriter out = new PrintWriter(stream);
		StringBuilder sb = new StringBuilder();
		
		if (topicRec == null){
			String xml = "<state/>";
			XMLSerializer xmlSerializer = new XMLSerializer();  
			JSON json = xmlSerializer.read( xml );  
			out.write( json.toString() );
			
			out.flush();
			out.close();
			return;
		}
		
		// output topic state
		sb.append("<state topic=\"" + topicRec.getName() + "\">");
		if (topicRec != null) {
			String[] names = topicRec.getElementAttributeNames();
			for (String name : names) {
				Attribute[] values = topicRec.getElementAttributes(name);
				for (Attribute attr : values) {
					sb.append("<" + name);
					if (!attr.getType().equals("text"))
						sb.append(" type='" + attr.getType()+"'");
					sb.append(">");
					sb.append(StringEscapeUtils.escapeHtml(attr.getValue()));
					sb.append("</" + name + ">");
				}
			}
		}
		sb.append("</state>");
		
		//convert xml to json
		String xml = sb.toString();
		XMLSerializer xmlSerializer = new XMLSerializer();  
		JSON json = xmlSerializer.read( xml );  
		out.write( json.toString() );
		
		out.flush();
	}
	
	
	@SuppressWarnings("unchecked")
	public void writeXMLElement(String topic, String stateID, OutputStream stream){
		
		ElementMap topicRec = (ElementMap) topicStateMap.get(topic); 
		
		CacheElementIF stateElement = topicRec.getElement(stateID);
		
		PrintWriter out = new PrintWriter(stream);
		
		if (topicRec == null || stateElement == null){
			out.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			out.print("<state/>");
			out.close();
			return;
		} 
		
		out.print("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		out.print("<state topic=\"" + topicRec.getName() + "\">");
			
		List<Attribute> values = stateElement.getList();
		for (Attribute attr : values) {
			out.print("<" + attr.getName());
			if (!attr.getType().equals("text"))
				out.print(" type='" + attr.getType()+"'");
			out.print(">");
			out.print(StringEscapeUtils.unescapeHtml(attr.getValue()));
			out.print("</" + attr.getName() + ">");
		}
		
		out.print("</state>");
		out.flush();
		
	}
	
	@SuppressWarnings("unchecked")
	public void writeJSONElement(String topic, String stateID, OutputStream stream){
		
		ElementMap topicRec = (ElementMap) topicStateMap.get(topic); 
		
		CacheElementIF stateElement = topicRec.getElement(stateID);
		
		PrintWriter out = new PrintWriter(stream);
		StringBuilder sb = new StringBuilder();
		
		if (topicRec == null || stateElement == null){
			String xml = "<state/>";
			XMLSerializer xmlSerializer = new XMLSerializer();  
			JSON json = xmlSerializer.read( xml );  
			out.write( json.toString() );
			
			out.flush();
			out.close();
			return;
		} 
		
		sb.append("<state topic=\"" + topicRec.getName() + "\">");
			
		List<Attribute> values = stateElement.getList();
		for (Attribute attr : values) {
			sb.append("<" + attr.getName());
			if (!attr.getType().equals("text"))
				sb.append(" type='" + attr.getType()+"'");
			sb.append(">");
			sb.append(StringEscapeUtils.escapeHtml(attr.getValue()));
			sb.append("</" + attr.getName() + ">");
		}
		
		sb.append("</state>");
		
		//convert xml to json
		String xml = sb.toString();
		XMLSerializer xmlSerializer = new XMLSerializer();  
		JSON json = xmlSerializer.read( xml );  
		out.write( json.toString() );
		
		out.flush();
	}
	
	public String getXML(String topic) throws BrokerException {
		
		ElementMap topicRec = (ElementMap) topicStateMap.get(topic); 
		
		StringBuilder sb = new StringBuilder();
		
		if (topicRec == null){
			sb.append("<state/>");
			return sb.toString();
		}
		
		// output topic state
		sb.append("<state topic=\"" + topicRec.getName() + "\">");
		if (topicRec != null) {
			String[] names = topicRec.getElementAttributeNames();
			for (String name : names) {
				Attribute[] values = topicRec.getElementAttributes(name);
				for (Attribute attr : values) {
					sb.append("<" + name);
					if (!attr.getType().equals("text"))
						sb.append(" type='" + attr.getType()+"'");
					sb.append(">");
					sb.append(StringEscapeUtils.escapeHtml(attr.getValue()));
					sb.append("</" + name + ">");
				}
			}
		}
		sb.append("</state>");
		
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public String getXMLElement(String topic, String stateID){
		
		ElementMap topicRec = (ElementMap) topicStateMap.get(topic); 
		
		CacheElementIF stateElement = topicRec.getElement(stateID);
		
		StringBuilder sb = new StringBuilder();
		
		if (topicRec == null || stateElement == null){
			sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			sb.append("<state/>");
			return sb.toString();
		} 
		
		sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
		sb.append("<state topic=\"" + topicRec.getName() + "\">");
			
		List<Attribute> values = stateElement.getList();
		for (Attribute attr : values) {
			sb.append("<" + attr.getName());
			if (!attr.getType().equals("text"))
				sb.append(" type='" + attr.getType()+"'");
			sb.append(">");
			sb.append(StringEscapeUtils.escapeHtml(attr.getValue()));
			sb.append("</" + attr.getName() + ">");
		}
		
		sb.append("</state>");
		return sb.toString();
	}
	
	
	public void clean(){
		String[] topics = this.getCacheObjectTopics();
		for (String topic: topics){
			ElementMap topicState = (ElementMap) this.getCacheObject(topic);
			try{
					topicState.clean();
			}catch(Exception e){
				logger.error("Cleaning State failed for topic: " + topic);
			}
		}
	}

	public void addNotificationListener(NotificationHandlerIF notification) {
		this.notificationHandler = notification;
	}

	public void removeNotificationListener() {
		this.notificationHandler = null;
	}
}
