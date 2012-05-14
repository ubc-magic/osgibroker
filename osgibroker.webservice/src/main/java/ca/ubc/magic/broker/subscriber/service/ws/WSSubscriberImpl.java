package ca.ubc.magic.broker.subscriber.service.ws;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

//import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.RemotePublisherIF;
import ca.ubc.magic.broker.api.ds.Client;
import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.impl.SubscriberImpl;

class WSSubscriberImpl extends SubscriberImpl {
	
	public static final String NAME = "WSSubscriber";
	
	private static final Logger logger = Logger.getLogger( WSSubscriberImpl.class );

	public WSSubscriberImpl(RemotePublisherIF _publisher,	DBManagerIF _manager) {
		super(NAME, _publisher, _manager);
	}

	public void registerClients(String _subscriberName) throws Exception {
		
		List<Client> clients = dbManager.getClientStore().getClientsBySubscription(_subscriberName);
		
		if (clients == null)
			return;
		
		for (Client client : clients){
			Set<String> topics = dbManager.getClientStore().getClientTopics((String) client.getProperty(RemoteClientIF.CLIENT_ID)); 
			
			WSClientWrapper wsClient = new WSClientWrapper();
			wsClient.setClient(client);
			
			for (String topic : topics)
				if (this.existsClient(wsClient, topic))
					continue;
				else
					this.addListener(wsClient, topic, true);
		}
	}
}
