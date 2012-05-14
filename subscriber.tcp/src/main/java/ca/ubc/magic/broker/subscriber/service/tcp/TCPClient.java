package ca.ubc.magic.broker.subscriber.service.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.ds.Client;
import ca.ubc.magic.broker.api.ds.Event;

/**
 * Class TCPClient extends the client class, enabling listeners to be connected to the subscriber
 * through TCP connections. The class also implements Runnable to execute on a separate Thread from
 * the TCP server for dealing with individual communications from client
 *
 * @author nima
 *
 */

class TCPClient implements Runnable, RemoteClientIF{

	public static final String TCP_CLIENT = "tcpClient";

	private Logger logger = Logger.getLogger(TCPClient.class);
	
	private Socket socket = null;
	private BufferedReader reader = null;
	private PrintWriter writer = null;

	private Client client;

//	private Thread asyncDelivery = null;

	private List<Serializable> messageList = null;

	private SubscriberIF subscriber;
	private PublisherIF  publisher;

	/**
	 * The TCPClient class constructor receives a reference to the subscriber class to register
	 * the connecting client with, as well as a socket connection to the client requesting data
	 * communication with the client
	 *
	 * @param subscriber	The subscriber object working with the TCP client, i.e., the TCPServer
	 * @param socket		The socket connection to the connecting client
	 */
	public TCPClient(SubscriberIF subscriber, PublisherIF publisher, Socket socket){
		
		client = new Client();

		this.subscriber = subscriber;
		this.publisher  = publisher;
		this.socket = socket;

		this.messageList = new CopyOnWriteArrayList<Serializable>();

//		this.asyncDelivery = new Thread (new AsyncMessageDelivery());
//		this.asyncDelivery.start();

		this.putProperty(Client.IP, socket.getInetAddress().getHostAddress());
		this.putProperty(Client.PORT, Integer.toString(socket.getPort()));
		this.putProperty(RemoteClientIF.CLIENT_TYPE, TCPClient.TCP_CLIENT);

		try {
			reader = new BufferedReader(new InputStreamReader (socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setClient(Client _client){
		this.client = _client;
	}

	public Client getClient(){
		return client;
	}

	/**
	 * The overwritten deliver message. sending the message to the requesting client
	 *
	 * @param message	the message to be delivered to the connected TCPClient
	 */
	public void deliver(Serializable message){

		synchronized(this){
			writer.write("<events>");
			writer.write(message.toString());
			writer.write("</events>\n\0");
			writer.flush();
		}
	}

	/**
	 * The internal body of the client thread listening for coming messages from the connected
	 * TCP client. The message could be any subscription or unsubscription message
	 */
	public void run() {
		
		int errorCount = 0;

		while (!Thread.interrupted()){
			
			try {

				//TODO nimak	we have a bottleneck here. I think the connection gets stuck at this
				//				point whenever no message is on the pipe for the socket to read from
				//				so, it is impossible to pass this line and continue with closure of the
				// 				socket when the socket is closed. Has to find a workaround for this
				String receivedMsg = reader.readLine();

				Map<String, String> params = tokenizer(receivedMsg);

				//throws an error is no message is received
				if (params == null)
					throw new BrokerException("Null parameters ... fail");

				//throws error if there is no action defined by the client
				if(params.get(RemoteClientIF.ACTION) == null)
					throw new BrokerException("No Action is specified");

				//throws error if there is no topic for the client to get registered with
				if (params.get(SubscriberIF.TOPIC) == null)
					throw new BrokerException("No Topic is defined");

				this.putProperty(SubscriberIF.TOPIC, params.get(SubscriberIF.TOPIC));

				//throws error if the client doesn't have any ID for its connection
				if (params.get(RemoteClientIF.CLIENT_ID) == null)
					throw new BrokerException("ClientID is missing");

				this.putProperty(RemoteClientIF.CLIENT_ID, params.get(RemoteClientIF.CLIENT_ID));

				// if the action is subscription, issues a subscription to the subscriber
				if (RemoteClientIF.SUBSCRIBE.equals(params.get(RemoteClientIF.ACTION))){

					subscriber.addListener(this, (String) this.getProperty(SubscriberIF.TOPIC));
					
					Event ackEvnt = new Event();
					ackEvnt.addAttribute(TCPServer.DLVRY_STATUS, TCPServer.SBSCRPTN_ACKNL);
					this.deliver(ackEvnt);

				}

				// if the action is unsubscription, issues a removal to the subscriber, removing
				// the client from the list of listeners
				if (RemoteClientIF.UNSUBSCRIBE.equals(params.get(RemoteClientIF.ACTION)) || !socket.isConnected()){
					subscriber.removeListener(this, (String) this.getProperty(SubscriberIF.TOPIC));
					
					Event ackEvnt = new Event();
					ackEvnt.addAttribute(TCPServer.DLVRY_STATUS, TCPServer.UNSBSCRPTN_ACKNL);
					this.deliver(ackEvnt);
					
					Thread.sleep(Client.MESSAGE_INTERVAL + 10);
					
					socket.close();
					return;
				}
				
				// if the action is send, use the publisher to deliver the message under the
				// specified topic and based on what is needed
				if (PublisherIF.SEND.equals(params.get(RemoteClientIF.ACTION))){
					
					Event event = new Event(params);
					publisher.deliver(event, (String) this.getProperty(SubscriberIF.TOPIC));

					Event ackEvnt = new Event();
					ackEvnt.addAttribute(TCPServer.DLVRY_STATUS, TCPServer.DLVRY_ACKNL);
					this.deliver(ackEvnt);
					
				}
				
			}catch (Exception e) {
				//delivers the error message to the client on demand
				Event failEvent = new Event();
				
				failEvent.addAttribute(TCPServer.DLVRY_STATUS, TCPServer.ACTION_FAILD);
				failEvent.addAttribute(TCPServer.MSG, e.getMessage());
				
				this.deliver(failEvent);
			}
		} 
	}

	/**
	 * sends the cross domain XML file to the client to solve the cross domain issues for the connecting client
	 */
	@SuppressWarnings("unused")
	private void sendCrossDomainPolicy(){
		String crossdomainXML = "<?xml version=\"1.0\"?><!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\"><cross-domain-policy><site-control permitted-cross-domain-policies=\"all\"/><allow-access-from domain=\"pspi.magic.ubc.ca:8080\" to-ports=\"5300\"/></cross-domain-policy>";
		this.deliver(crossdomainXML + "\0");
	}

	/**
	 * The tokenizer class receives the String from the connected TCP client and parses it to a
	 * hash map for use by the service manager
	 *
	 * @param data	the data string received from the TCP client
	 * @return		the HashMap for the parsed received data
	 */
	private Map<String, String> tokenizer(String data){

		if (data == null)
			return null;

		HashMap<String, String> params = new HashMap<String, String>();

		StringTokenizer tokenizer = new StringTokenizer(data, "=&");
		while (tokenizer.hasMoreElements()){
			params.put(tokenizer.nextToken(), tokenizer.nextToken());
		}
		return params;
	}

	/**
	 * The AsyncMessageDelivery class is used to deliver messages from the registered
	 * client socket to the subscribed TCP clients within specific intervals. This is
	 * to resolve the issue of having message jams sent from the client to the server.
	 *
	 * @author nima
	 *
	 */
	private class AsyncMessageDelivery implements Runnable {

		public void run() {

			while(!Thread.interrupted() && !socket.isClosed()){

				// if the messageList is not null or empty, it wraps the set of
				// events into the message list and then sends them off to the server
				if (messageList != null && !messageList.isEmpty()){

					writer.write("<events>");
					for (Serializable message : messageList)
						writer.write(message.toString());
					writer.write("</events>\n\0");
					writer.flush();

					messageList.clear();
				}
				try {
					// Thread sleeps for MESSAGE_INTERVAL milliseconds and then
					// looks into the queue of existing messages and sends them
					// off the to listening clients
					Thread.sleep(Client.MESSAGE_INTERVAL);
				} catch (InterruptedException e) {
					writer.write("message delivery interrupted at client");
				}
			}
		}
	}

	public long getExpirationTimeMillis() {
		return client.getExpirationTimeMillis();
	}

	public Object getProperty(String key) {
		return client.getProperty(key);
	}

	public Set<String> getPropertyNames() {
		return client.getPropertyNames();
	}

	public long getRegistrationTimeMillis() {
		return client.getRegistrationTimeMillis();
	}

	public boolean isExpired() {
		return client.isExpired();
	}

	public void putProperty(String key, Object value) {
		client.putProperty(key, value);
	}

	public void renewSubscription() {
		client.resetExpirationTime();
	}

	public void renewSubscription(long expiresIn) {
		client.resetExpirationTime(expiresIn);
	}
}
