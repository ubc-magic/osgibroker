package ca.ubc.magic.broker.publisher.service.sms;

import ca.ubc.magic.broker.api.PublisherIF;

import org.apache.log4j.Logger;

import org.osgi.service.component.ComponentContext;

/**
 * The root class for the SMS Modem receiving information about the publisher and initializing the configurator, etc.
 * 
 * @author nima
 *
 */
public class SMSPublisherPolling implements ConfigurationListenerIF {

	private static final Logger logger = Logger
			.getLogger(SMSPublisherPolling.class);

	SMSConfigurator configurator = new SMSConfigurator();
	
	Thread thread = null;
	
//	static PortScan portScan = new PortScan("default.samba75.modem1", 230400, "Falcom", "Samba75");
	
	public SMSPublisherPolling() {
	
		SMSManagedService.getInstance().addConfigurationListener(this);
		
	}
	
	public void deactivate(){
		configurator.stop();
	}

	protected void bindPublisherServ(PublisherIF pub) {
		
		configurator.getBrokerConnector().setPublisher(pub);
		logger.debug("Publisher Service is bound to SMS Publisher Polling..");
	}

	protected void unbindPublisherServ(PublisherIF pub) {
		configurator.getBrokerConnector().setPublisher(null);
		logger.debug("Publisher Service is unbound to SMS Publisher Polling..");

	}
	
	public void reconfigure() {
		
		thread = new Thread (configurator);
		thread.start();
		
	}

}
