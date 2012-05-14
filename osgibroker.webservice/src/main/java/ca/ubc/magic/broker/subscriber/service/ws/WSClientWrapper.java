package ca.ubc.magic.broker.subscriber.service.ws;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.ds.Client;

/**
 * The class defines a client the corresponds to each request received by the EventServlet.
 * The ServletClient takes care of managing the timeOut requst by the requesting client when
 * it requests for receiving event lists. It also makes sure that the delivery method from the
 * subscriber adds the set of events to a queue to be picked up by the client. This architecture 
 * enables different clients to define different timeOut times on one channel and receive their events
 * irrespective of how other clients would prefer to receive the events. The ServletClient object is 
 * created once a client subscribes with a channel using the subscribe servlet and is released to be garbage
 * collected once the client unsubscribes 
 * 
 * @author nima
 *
 */
class WSClientWrapper implements RemoteClientIF {
	
	private Client client;
	
	protected LinkedBlockingQueue<Serializable> queue;
	
	public static final long WAIT_TIME = 5000;
	public static final String TYPE = "wsClient";
	private static final Logger logger = Logger.getLogger( WSClientWrapper.class );
	
	/**
	 * 
	 * @param _request		The request received from a servlet client
	 * @param _response		The response to tbe delivered to the servlet client
	 */
	public WSClientWrapper (){
		
		client = new Client();
		this.putProperty(RemoteClientIF.CLIENT_TYPE, WSClientWrapper.TYPE);

		queue = new LinkedBlockingQueue<Serializable>(10000);
	}
	
	public WSClientWrapper(long expirationSecs){
		client = new Client(expirationSecs);
		this.putProperty(RemoteClientIF.CLIENT_TYPE, WSClientWrapper.TYPE);
 
		queue = new LinkedBlockingQueue<Serializable>(10000);
	}
	
//	public WSClientWrapper(){
//		this(_request, _response, -1);
//	}
//	
//	public WSClientWrapper(){
//		this(null, null, -1);
//	}
	
	
	// -------------
	// The set of setters and getters for important objects in the ServletClientWrapper
	
//	public void setRequest(HttpServletRequest _request){
//		this.request = _request;
//	}
//	
//	public HttpServletRequest getRequest(){
//		return this.request;
//	}
//	
//	public void setResponse(HttpServletResponse _response){
//		this.response = _response;
//	}
//	
//	public HttpServletResponse getResponse(){
//		return this.response;
//	}
	
	public void setClient(Client _client){
		this.client = _client;
	}
	
	public Client getClient(){
		return client;
	}
	
	/**
	 * The deliver method overwrites the deliver method of RemoteClientIF. It adds received events to a queue
	 * to be collected by the client once the request for that client comes in. 
	 * 
	 */
	public void deliver(Serializable event) {
		
		if (this.getProperty(RemoteClientIF.URL_SUBSCRIBER) != null){
			try {				
				logger.debug("message to be delivered to the client's URL");
				logger.debug("clientURL: " + (String)this.getProperty(RemoteClientIF.URL_SUBSCRIBER));
				String eventList = "<events>" + event.toString() + "</events>";
				String responseBody = SendService.sendHttpPostBody((String)this.getProperty(RemoteClientIF.URL_SUBSCRIBER), eventList);
				logger.debug("responseBody: " + responseBody);
			} catch (Exception e) {
				logger.error("Problem occured while trying to deliver the event to a remote URL");
				e.printStackTrace();
			}
		}
		else 
			queue.add(event);
		
	}
	
	/**
	 * The method returns the list of all events waiting on a channel for a client, or returns an empty event
	 * list if there is no event on the channel within the specified timeOut time. default is 30 seconds.
	 * 
	 * @param timeoutSec				The timeout in seconds before the Hanging Get strategy returns null
	 * @return							List of stored events piled up on a channel for a client
	 * @throws InterruptedException		thrown when the polling is interuptted for any reason
	 */
	public List<Serializable> getEventsList() throws InterruptedException {
		
		ArrayList<Serializable> returnEvents = new ArrayList<Serializable>();
		
		if (this.queue.peek() != null){
			
			this.queue.drainTo(returnEvents);
			return returnEvents;
			
		}
		else {
			
//			Serializable event = queue.poll(timeoutSec, TimeUnit.SECONDS);
			Serializable event = queue.poll();
			if (event != null)
				returnEvents.add(event);
			return returnEvents;
		}
	}
	
	/**
	 * removes the set of events from the event list once they are picked up by the client
	 */
	public void clearEventsList(){
		while (!this.queue.isEmpty())
			this.queue.poll();
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

	public void renewSubscription(long _expiresIn) {
		client.resetExpirationTime(_expiresIn);
	}
}
