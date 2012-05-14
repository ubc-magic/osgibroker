package ca.ubc.magic.broker.subscriber.service.tcp;

import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.osgi.service.log.LogService;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.RemotePublisherIF;
import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.impl.SubscriberImpl;

public class TCPServer extends SubscriberImpl {

	public static final int DEFAULT_PORT = 5300;
	public static final int SLEEP_TIME  = 500;
	
	public static final String NAME="TCPSubscriber";
	
	public static final String DLVRY_STATUS = "status";
	public static final String MSG = "msg";
	public static final String DLVRY_ACKNL="DLVRY_ACKNL";
	public static final String ACTION_FAILD="ACTION_FAILD";
	public static final String SBSCRPTN_ACKNL = "SUBSCRIPTION_ACKNL";
	public static final String UNSBSCRPTN_ACKNL = "UNSUBSCRIPTION_ACKNL";
	

	private ServerSocket serverSocket = null;
	private Socket       socket =  null;
	
	private PublisherIF lpublisher;

	private static final Logger logger = Logger.getLogger( TCPServer.class );

	/**
	 * initializes the subscriber by passing the found publisher service to it
	 *
	 * @param publisher The publisher service found by the TCP subscriber service
	 */
	public TCPServer(PublisherIF _lpublisher, RemotePublisherIF _rpublisher, DBManagerIF _dbManager) {
		super(NAME, _rpublisher, _dbManager);
		this.lpublisher = _lpublisher;
	}

	/**
	 * runs the TCP server thread, listening for incoming TCP requests for subscribers
	 */
	public void run(){

		logger.debug("TCP server is running");

		try {
			serverSocket = new ServerSocket(DEFAULT_PORT);

			while (!Thread.interrupted()){

				socket = serverSocket.accept();

				Thread client =  new Thread(new TCPClient(this, lpublisher, socket));
				client.start();

				Thread.sleep(TCPServer.SLEEP_TIME);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() throws Exception{
		serverSocket.close();
	}

	@Override
	public void registerClients(String subscriberName) throws Exception {
		
//		System.out.println("[MSG:] RegisterClient is not implemented for TCPServer yet. \\n" +
//		" TCP Clients do not get automaically resubscribed when the broker starts up");
		
		throw new BrokerException("RegisterClient is not implemented for TCPServer yet." +
				" TCP Clients do not get automaically resubscribed");
	}
}
