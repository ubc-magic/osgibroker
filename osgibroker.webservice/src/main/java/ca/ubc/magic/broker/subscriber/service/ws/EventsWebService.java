package ca.ubc.magic.broker.subscriber.service.ws;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.storage.DBManagerIF;

public class EventsWebService {
	
	public EventsWebService(){
		subscriber = ContextProvider.getInstance().getSubscriber();
		publisher = ContextProvider.getInstance().getPublisher();
		dbManager = ContextProvider.getInstance().getDBManager();
	}
	
	private SubscriberIF subscriber = null;
	private PublisherIF  publisher = null;
	private DBManagerIF  dbManager = null;
	
	public void setSubscriber(SubscriberIF _subscriber) {
		this.subscriber = _subscriber;
	}

	private static final Logger logger = Logger.getLogger(EventsWebService.class);
	
	public void sendEvent(String topic, String clientid, String attr, String value, String receiverid, String excludeid){
		
		try {
			
			if (this.publisher == null)
				throw new BrokerException(HttpServletResponse.SC_NO_CONTENT,BrokerException.NO_PUBLISHER_FOUND);
			
			Event event = new Event();
			
			//parse the event attributes and store them in the event data container
			event.addAttribute("topic", topic);
			event.addAttribute("clientID", clientid);
			event.addAttribute(attr, value);
			event.addAttribute("receiverID", receiverid);
			event.addAttribute("excludeID", excludeid);
			
			// delivers the final events received by the servlet to the publisher to be distributed to the
			// subscribers of this publisher
			publisher.deliver(event, topic);
			
		} catch (Exception e) {
		}
	}
	
	public OMElement receiveEvents(String topic, String clientid) throws Exception{
		
		if (topic != null)
		{
			List<Serializable> eventsList = eventPolling(clientid, topic);
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("<events>");
			if (eventsList != null && !eventsList.isEmpty()){
				Iterator<Serializable> it = eventsList.iterator();
				while (it.hasNext())
					sb.append(it.next().toString());
			}
			sb.append("</events>");
			
			OMElement elem = AXIOMUtil.stringToOM(sb.toString());
			return elem;
		}
		else
		{
			Set<String> topics = dbManager.getClientStore().getClientTopics(clientid);

			Iterator<String> topicsItr = topics.iterator();
			String current_topic = topicsItr.next();
			List<Serializable> eventsList = eventPolling(clientid, current_topic);

			StringBuilder sb = new StringBuilder();
			
			sb.append("<events>");
			if (eventsList != null && !eventsList.isEmpty()){
				Iterator<Serializable> it = eventsList.iterator();
				while (it.hasNext())
					sb.append(it.next().toString());
			}
			sb.append("</events>");
			
			OMElement elem = AXIOMUtil.stringToOM(sb.toString());
			return elem;
		}
	}

	private List<Serializable> eventPolling(String clientID, String topic) throws BrokerException, InterruptedException {
		
		WSClientWrapper client = (WSClientWrapper)subscriber.getClient(clientID, topic);
		
		if (client == null)
			throw new BrokerException(HttpServletResponse.SC_NOT_ACCEPTABLE, 
				BrokerException.NO_CLIENT_SUBSCRIPTION);
		
		List<Serializable> eventsList = (ArrayList<Serializable>) client.getEventsList();
		
		client.clearEventsList();
		
		return eventsList;
	}
}
