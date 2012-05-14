package ca.ubc.magic.broker.publisher.service.sms.broker;

/**
 * The model class for the Broker. keeping information about the topic and other related information about the core
 * of the broker, if any.
 * 
 * @author nima
 *
 */
public class BrokerConfig {
	
	String topic = null;
	
	public String getTopic(){
		return topic;
	}
	
	public void setTopic(String _topic) {
		this.topic = _topic;
	}

}
