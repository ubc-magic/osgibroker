package ca.ubc.magic.broker.publisher.service.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * The model class holding information about the SMS modem 
 * 
 * @author nima
 *
 */
public class SMSModem {
	
	private static final Logger logger = Logger.getLogger( SMSModem.class );
	
	String id = null;
	String comport = null;
	int baudRate = -1;
	String manufacturer = null;
	String model = null;
	int discoveryInterval = 1000;
	List<String> skipPortList = new ArrayList<String>();
	
	public String getID (){
		return id;
	}
	
	public void setID(String _id){
		this.id = _id;
	}
	
	public String getComPort(){
		return comport;
	}
	
	public void setComPort(String _comport){
		this.comport = _comport;
	}
	
	public int getBaudRate(){
		return baudRate;
	}
	
	public void setBaudRate(int _baudRate){
		this.baudRate = _baudRate;
	}
	
	public String getManufacturer(){
		return manufacturer;
	}
	
	public void setManufacturer(String _manufacturer){
		this.manufacturer = _manufacturer;
	}
	
	public String getModel(){
		return model;
	}
	
	public void setModel(String _model){
		this.model = _model;
	}
	
	public int getDiscoveryInterval(){
		return discoveryInterval;
	}
	
	public void setDiscoveryInterval(int _discoveryInterval){
		this.discoveryInterval = _discoveryInterval;
	}
	
	public void setSkipPortList(String _skipPortList){
		
		StringTokenizer tokenizer = new StringTokenizer(_skipPortList, ",");
		
		while(tokenizer.hasMoreTokens()){
			
			String nextToken = tokenizer.nextToken();
			this.skipPortList.add(nextToken.trim());

		}
	}
	
	public List<String> getSkipPortList(){
		return this.skipPortList;
	}
	
	public boolean equals(Object o) {
		
		if (o == null || !o.getClass().equals(this.getClass()))
			return false;
		
		SMSModem another = (SMSModem) o;
		
		if (this.baudRate == another.getBaudRate() && this.id.equals(another.getID()) &&
				this.manufacturer.equals(another.getManufacturer()) && this.model.equals(another.getModel()) &&
				this.comport == another.getComPort() && this.getSkipPortList().equals(another.getSkipPortList()))
			return true;
			
		return false;
	}
	
	public boolean equalsPortExcluded(Object o){
		
		if (o == null || !o.getClass().equals(this.getClass()))
			return false;
		
		SMSModem another = (SMSModem) o;
		
		if (this.baudRate == another.getBaudRate() && this.id.equals(another.getID()) &&
				this.manufacturer.equals(another.getManufacturer()) && this.model.equals(another.getModel()) &&
				this.getSkipPortList().equals(another.getSkipPortList()))
			return true;
			
		return false;
		
	}
	
	public boolean isEmpty(){
		
		if (id == null && comport == null && baudRate == -1 &&
				manufacturer == null && model == null)
			return true;
		
		return false;
	}
}
