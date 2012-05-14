package ca.ubc.magic.broker.subscriber.service.ws;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.cache.CacheIF;
import ca.ubc.magic.broker.api.ds.Client;
import ca.ubc.magic.broker.api.ds.ElementMap;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.cache.state.StateManager;

public class UnsubscribeWebService {
	
	public UnsubscribeWebService(){
		subscriber = ContextProvider.getInstance().getSubscriber();
	}
	
	private SubscriberIF subscriber = null;
	
	public void setSubscriber(SubscriberIF _subscriber) {
		this.subscriber = _subscriber;
	}

	private static final Logger logger = Logger.getLogger(UnsubscribeWebService.class);
	
	public String unsubscribe(String topic, String clientid) throws Exception{
		
		RemoteClientIF client = subscriber.getClient(clientid, topic);

		if (client == null){
			logger.debug("Client trying to subscribe: " + clientid);
			logger.debug("Topic for the client: " + topic);
		}

		this.subscriber.removeListener(client, topic);
		
		return "Clientid '"+clientid+"' unsubscribed from topic '"+topic+"'";
	}
}
