package ca.ubc.magic.broker.subscriber.service.ws;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.cache.CacheIF;
import ca.ubc.magic.broker.api.ds.ElementMap;
import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.cache.state.StateManager;

public class StateWebService {
	
	public StateWebService(){
		subscriber = ContextProvider.getInstance().getSubscriber();
		publisher = ContextProvider.getInstance().getPublisher();
		dbManager = ContextProvider.getInstance().getDBManager();
		cache	  = ContextProvider.getInstance().getCache();
	}
	
	private SubscriberIF subscriber = null;
	private PublisherIF  publisher = null;
	private DBManagerIF  dbManager = null;
	private CacheIF 	 cache		= null;
	
	public void setSubscriber(SubscriberIF _subscriber) {
		this.subscriber = _subscriber;
	}

	private static final Logger logger = Logger.getLogger(StateWebService.class);
	
	public void putState(String topic, String clientid, String attr, String value){
		
		ElementMap stateMap = (ElementMap) cache.getCacheEntry(StateManager.class).getCacheObject(topic);
	
		stateMap.setUntypedAttribute(attr, value);
		
		cache.getCacheEntry(StateManager.class).updateCacheObject(topic, stateMap);
	}
	
	public OMElement getState(String topic, String clientid, String stateid) throws Exception {
	
		StateManager stateManager = (StateManager) cache.getCacheEntry(StateManager.class);
		String states = "";
		
		if (stateid == null)
			states = stateManager.getXML(topic);
		else 
			states = stateManager.getXMLElement(topic, stateid);
		
		OMElement elem = AXIOMUtil.stringToOM(states);
		
		return elem;
	}
	
	public String deleteState(String topic, String clientid, String stateid) throws BrokerException {

		String stateID = stateid;
		if (stateID == null){
			logger.debug("Deleting all states for the topic: " + topic);
			cache.getCacheEntry(StateManager.class).removeCacheObject(topic);
			
			return "All states for topic "+topic+" deleted";
		}
		else{
			logger.debug("Deleting state " + stateID + " for the topic: " + topic);
			((ElementMap) cache.getCacheEntry(StateManager.class).getCacheObject(
					topic)).removeState(stateID);
			
			return stateID + " state deleted";
		}
	}
}
