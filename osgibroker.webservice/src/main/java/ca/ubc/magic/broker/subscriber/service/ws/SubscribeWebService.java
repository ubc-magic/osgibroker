package ca.ubc.magic.broker.subscriber.service.ws;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.SubscriberIF;

public class SubscribeWebService {
	
	public SubscribeWebService(){
		subscriber = ContextProvider.getInstance().getSubscriber();
	}
	
	private SubscriberIF subscriber = null;
	
	public void setSubscriber(SubscriberIF _subscriber) {
		this.subscriber = _subscriber;
	}

	private static final Logger logger = Logger.getLogger(SubscribeWebService.class);
	
	public String subscribe(String topic, String clientid, String expires) throws Exception
	{
		RemoteClientIF wsClient = null;
		if (expires != null){
			long expirationSecs = Long.parseLong(expires);
			wsClient = new WSClientWrapper(expirationSecs);
		}else
			wsClient = new WSClientWrapper();
		
		wsClient.putProperty(RemoteClientIF.CLIENT_ID, clientid);
		
		this.subscriber.addListener(wsClient, topic);
		
		return "Clientid '"+clientid+"' subscribed to topic '"+topic+"'";
	}
}
