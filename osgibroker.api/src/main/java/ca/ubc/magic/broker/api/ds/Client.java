package ca.ubc.magic.broker.api.ds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import ca.ubc.magic.broker.api.RemoteClientIF;

/**
 * The abstract client class that can store a series of properties for a
 * client or a listener that subscribes to a topic. The Client class keeps a 
 * HashMap for the properties that a client may want to store about its 
 * properties. This includes the port number, or the host, etc. 
 * 
 * @author nima kaviani
 *
 */

public class Client{

	public static final String PORT = "port";
	public static final String IP = "ip";
	public static final int MESSAGE_INTERVAL = 50;
	
	HashMap<String, Object> props = new HashMap<String, Object>();
	
	public Client(){
		this(-1);
	}
	
	public Client(long _expiresIn){
		
		this.putProperty(RemoteClientIF.REGISTRATION_TIME_MILLIS, Long.toString(System.currentTimeMillis()));
		this.putProperty(RemoteClientIF.EXPIRE_TIME_MILLIS, Long.toString(_expiresIn * 1000));
	}
	
	public void putProperty(String key, Object value){
		props.put(key, value);
	}
	
	public Object getProperty(String key){
		return props.get(key);
	}	
	
	public Set<String> getPropertyNames(){
		return props.keySet();
	}
	
	public long getRegistrationTimeMillis(){
		return Long.parseLong((String) this.getProperty(RemoteClientIF.REGISTRATION_TIME_MILLIS));
	}
	
	public long getExpirationTimeMillis(){
		return Long.parseLong((String) this.getProperty(RemoteClientIF.EXPIRE_TIME_MILLIS));
	}
	
	public boolean isExpired(){
		
		if (this.getExpirationTimeMillis() < 0)
			return false;
		
		long diffMillis = System.currentTimeMillis() - this.getRegistrationTimeMillis();
		return (diffMillis > this.getExpirationTimeMillis()) ? true : false;
	}
	
	public void resetExpirationTime(){
		this.putProperty(RemoteClientIF.REGISTRATION_TIME_MILLIS, Long.toString(System.currentTimeMillis()));
	}
	
	public void resetExpirationTime(long _expiresIn){
		this.putProperty(RemoteClientIF.REGISTRATION_TIME_MILLIS, Long.toString(System.currentTimeMillis()));
		this.putProperty(RemoteClientIF.EXPIRE_TIME_MILLIS, Long.toString(_expiresIn * 1000));
	}

}
