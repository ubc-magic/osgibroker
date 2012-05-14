package ca.ubc.magic.broker.publisher.service.sms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import ca.ubc.magic.broker.publisher.service.sms.broker.BrokerConfig;

/**
 * The managed service for receiving configuration information about a modem from the configuration file.
 * 
 * @author nima
 *
 */
public class SMSManagedService implements ManagedService {
	
	Logger logger = Logger.getLogger(SMSManagedService.class);
	
	private static SMSManagedService smsManagedService = null;
	private static SMSModem modem = new SMSModem();
	private static BrokerConfig config = new BrokerConfig();
	
	private List<ConfigurationListenerIF> listenerList = null;
	
	public static SMSManagedService getInstance(){
		
			if (smsManagedService == null) {
				synchronized(SMSManagedService.class) {
					if (smsManagedService == null)
						smsManagedService = new SMSManagedService();
				}
			}
		
		return smsManagedService;
	}
	
	public SMSModem getModemInfo(){
		return modem;
	}
	
	public BrokerConfig getBrokerConfigInfo(){
		return config;
	}
	
	public void addConfigurationListener(ConfigurationListenerIF listener){
		
		if (listenerList == null)
			listenerList = new ArrayList<ConfigurationListenerIF>();
		
		if (!listenerList.contains(listener))
			listenerList.add(listener);
	}
	
	public void removeConfigurationListener(ConfigurationListenerIF listener){
		
		if (listenerList != null && listenerList.contains(listener))
			listenerList.remove(listener);
	}
	
	@SuppressWarnings("unchecked")
	public void updated(Dictionary dict) throws ConfigurationException {

		SMSModem tempModem = new SMSModem();
		if (dict == null) {
//			configureDefaults();
		} else {
			
			tempModem.setID((String) dict.get("ca.ubc.magic.broker.sms.publisher.modemgateway.id"));
			tempModem.setBaudRate(Integer.parseInt((String) dict.get("ca.ubc.magic.broker.sms.publisher.modemgateway.baudrate")));
			tempModem.setManufacturer((String) dict.get("ca.ubc.magic.broker.sms.publisher.modemgateway.manufacturer"));
			tempModem.setModel((String) dict.get("ca.ubc.magic.broker.sms.publisher.modemgateway.model"));
			tempModem.setDiscoveryInterval(Integer.parseInt((String) dict.get("ca.ubc.magic.broker.sms.publisher.modemgateway.discovery.interval")));
			tempModem.setSkipPortList((String) dict.get("ca.ubc.magic.broker.sms.publisher.modemgateway.comport.skiplist"));
			
			config.setTopic((String) dict.get("ca.ubc.magic.broker.sms.publisher.topic"));
			
			if (!modem.equalsPortExcluded(tempModem) && listenerList != null){
				
				modem = tempModem;
				for (ConfigurationListenerIF listener : listenerList)
					listener.reconfigure();
			}
			
		}

		logger.debug("id: " + modem.getID());
		logger.debug("comport: " + modem.getComPort());
		logger.debug("baudRate: " + modem.getBaudRate());
		logger.debug("manufacturer: " + modem.getManufacturer());
		logger.debug("model: " + modem.getModel());
		logger.debug("topic: " + config.getTopic());
	}

	private void configureDefaults() {

		SMSModem tempModem = new SMSModem();
		
		Properties modemprops = new Properties();
		Properties brokerprops = new Properties();

		try {
			modemprops.load(this.getClass().getClassLoader()
					.getResourceAsStream("conf/modemgateway.properties"));
			brokerprops.load(this.getClass().getClassLoader()
					.getResourceAsStream("conf/topic.properties"));

		} catch (IOException e) {
			logger.error("Default configuration could not be read for the SMS Gateway Modem");
		}

		tempModem.setID( modemprops.getProperty("ca.ubc.magic.broker.sms.publisher.modemgateway.id"));
		tempModem.setBaudRate(Integer.parseInt(modemprops.getProperty("ca.ubc.magic.broker.sms.publisher.modemgateway.baudrate")));
		tempModem.setManufacturer(modemprops.getProperty("ca.ubc.magic.broker.sms.publisher.modemgateway.manufacturer"));
		tempModem.setModel(modemprops.getProperty("ca.ubc.magic.broker.sms.publisher.modemgateway.model"));
		tempModem.setDiscoveryInterval(Integer.parseInt(modemprops.getProperty("ca.ubc.magic.broker.sms.publisher.modemgateway.discovery.interval")));

		if (!modem.equals(tempModem)){
			modem = tempModem;
		}
		
		config.setTopic(brokerprops.getProperty("ca.ubc.magic.broker.sms.publisher.topic"));
		
	}

}
