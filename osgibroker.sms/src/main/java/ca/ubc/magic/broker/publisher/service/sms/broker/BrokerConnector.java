package ca.ubc.magic.broker.publisher.service.sms.broker;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.publisher.service.sms.SMSManagedService;

/**
 * The BrokerConnector class holds the logic for communication with the broker.
 * 
 * @author nima
 *
 */

public class BrokerConnector {
	
	private static final Logger logger = Logger.getLogger( BrokerConnector.class );
	
	PublisherIF  publisher;
	
	public PublisherIF getPublisher(){
		return publisher;
	}
	
	public void setPublisher(PublisherIF _publisher){
		this.publisher = _publisher;
	}
	
	public void deliver(Map<String, String> message) throws BrokerException{
		
		if (publisher == null)
			throw new BrokerException("No publisher is assigned to the SMS Bundle");
		if (SMSManagedService.getInstance().getBrokerConfigInfo().getTopic() == null)
			throw new BrokerException("No topic is assigned to the SMS Bundle");
		
		Event event = new Event();
		
		event.addAttribute("topic", SMSManagedService.getInstance().getBrokerConfigInfo().getTopic());
		
		Set<String> keys = message.keySet();
		for (String key : keys )
			event.addAttribute(key, message.get(key));
		
		try {
			publisher.deliver(event, SMSManagedService.getInstance().getBrokerConfigInfo().getTopic());
		} catch (Exception e) {
			logger.error(e);
		}
	}

}
