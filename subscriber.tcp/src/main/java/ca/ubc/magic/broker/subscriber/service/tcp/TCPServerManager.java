package ca.ubc.magic.broker.subscriber.service.tcp;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class TCPServerManager  {
	
	private final TCPManagedService tcpManagedService = new TCPManagedService();
	
	protected class TCPManagedService implements ManagedService {

		public void updated(Dictionary properties)
				throws ConfigurationException {
			// TODO Auto-generated method stub
			
		}
		
	}

	public TCPManagedService getTCPManagedService(){
		return tcpManagedService;
	}

}
