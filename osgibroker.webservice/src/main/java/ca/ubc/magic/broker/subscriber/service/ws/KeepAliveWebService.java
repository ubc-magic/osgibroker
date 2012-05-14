package ca.ubc.magic.broker.subscriber.service.ws;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.cache.CacheIF;
import ca.ubc.magic.broker.api.storage.DBManagerIF;

public class KeepAliveWebService {
	
	public KeepAliveWebService(){
		subscriber = ContextProvider.getInstance().getSubscriber();
	}
	
	private SubscriberIF subscriber = null;
	
	public void setSubscriber(SubscriberIF _subscriber) {
		this.subscriber = _subscriber;
	}

	private static final Logger logger = Logger.getLogger(KeepAliveWebService.class);
	
	public String keepAlive(String topic, String clientid, String expires) throws BrokerException
	{
		long   expiresSeconds = -1;
		
		if (expires != null)
			expiresSeconds = Long.parseLong(expires);

		if (subscriber.getClient(clientid, topic) == null)
			throw new BrokerException(BrokerException.NO_CLIENT_ALIVE);
		
		if (expiresSeconds == -1)
			subscriber.renewSubscription(clientid, topic);
		else 
			subscriber.renewSubscription(clientid, topic, expiresSeconds);
		
		return "Your subscription is successfully renewed for clientID: "+clientid;
	}
	
}
