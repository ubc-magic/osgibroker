package ca.ubc.magic.broker.subscriber.service.ws;

import java.util.List;
import java.util.Set;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.cache.CacheIF;
import ca.ubc.magic.broker.api.ds.Client;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.storage.DBManagerIF;

public class TopicWebService {
	
	public TopicWebService(){
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

	private static final Logger logger = Logger.getLogger(TopicWebService.class);
	
	public OMElement queryEvents(String topic, String clientid, String querySize) throws Exception{

		List<Event> eventList = null;
		
		if (querySize == null)
			eventList = this.dbManager.getEventStore().getLastEvents(topic);
		else
			eventList = this.dbManager.getEventStore().getLastEvents(topic, Integer.parseInt(querySize));
		
		StringBuilder stringbuilder = new StringBuilder();
		
		stringbuilder.append("<events>");
		
		if (eventList != null)
			for (Event evt : eventList)
			{
				stringbuilder.append(evt.toString());
				stringbuilder.append("\n");
			}
		stringbuilder.append("</events>");
		
		OMElement elem = AXIOMUtil.stringToOM(stringbuilder.toString());
		return elem;
		
	}
	
	public OMElement queryClients(String topic, String clientid) throws Exception
	{
		List<Client> clientList = this.dbManager.getClientStore().getClientsByTopic(topic);
		StringBuilder sb = new StringBuilder();
		
		sb.append("<clients>");
		if (clientList != null)
			
			for (Client client : clientList)
			{
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
		
		OMElement elem = AXIOMUtil.stringToOM(sb.toString());
		return elem;
	}
}
