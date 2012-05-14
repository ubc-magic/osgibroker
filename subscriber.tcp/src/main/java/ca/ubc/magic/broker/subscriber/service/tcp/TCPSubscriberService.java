package ca.ubc.magic.broker.subscriber.service.tcp;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.RemotePublisherIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.cache.CacheIF;
import ca.ubc.magic.broker.api.storage.DBManagerIF;

/**
 * The TCPSubscriberService class provides a hook to OSGi for TCP subscription with the
 * channels. It enables the TCPServer to register with OSGi under SubscriberIF
 * 
 * @author nima
 *
 */

public class TCPSubscriberService implements BundleActivator{

	private ServiceTracker tracker;
	private BundleContext context;
	private SubscriberIF subscriber = null;

	private static final Logger logger = Logger.getLogger(TCPSubscriberService.class);
	
	public void start(BundleContext context) throws Exception {
		
		this.context = context;
		
		// Detecting the log service
		System.out.print("Starting OSGiBroker TCP Service ...");
		
		String filterString =	"(|" + "(" + Constants.OBJECTCLASS + "=" + DBManagerIF.class.getName() + ")" +
								"(|" + "(" + Constants.OBJECTCLASS + "=" + PublisherIF.class.getName() + ")" +
								"(" + Constants.OBJECTCLASS + "=" + RemotePublisherIF.class.getName() + ")))";
		
		Filter serviceFilter = context.createFilter(filterString);
		
		tracker = new ServiceTracker(context, serviceFilter, new TCPServiceCustomizer());
		tracker.open();
		System.out.println("[DONE]");
	}

	public void stop(BundleContext context) throws Exception {
		((TCPServer) subscriber).interrupt();
		((TCPServer) subscriber).close();
		tracker.close();	
	}
	
	/**
	 * the following private class is a the filter tracker to make the TCPSubscriber aware of
	 * appearance or loss of the PublisherIF service. Once the service goes down, the TCPService
	 * also unregisters itself from communication 
	 * 
	 * @author nima
	 *
	 */
	private class TCPServiceCustomizer implements ServiceTrackerCustomizer {
		
		private RemotePublisherIF rpublisher = null;
		private PublisherIF lpublisher = null;
		private DBManagerIF dbManager = null;
		
		public Object addingService(ServiceReference reference) {
			
			Object serviceObj = context.getService(reference);
			
			if (serviceObj instanceof RemotePublisherIF){
				rpublisher = (RemotePublisherIF) serviceObj;
				logger.debug("New RemotePublisherService found: " + reference.getProperty(PublisherIF.PUBLISHER_NAME));
			}
			
			if (serviceObj instanceof PublisherIF){
				lpublisher = (PublisherIF) serviceObj;
				logger.debug("New LocalPublisherService found:" + reference.getProperty(PublisherIF.PUBLISHER_NAME));
			}
			
			if (serviceObj instanceof DBManagerIF){
				dbManager = (DBManagerIF) serviceObj;
				logger.debug("New DBManagerService found: " + reference.getProperty(DBManagerIF.DB_TYPE) + ":" + reference.getProperty(DBManagerIF.DB_NAME));
			}
			
			// if the TCP Subscriber is just started, the object is initialized but if it has been
			// already up and running but the publisher has gone done and come back or a new publisher
			// has been detected, it only resubscribes with the publisher, preventing the connected services 
			// from being disconnected. Also, in case of a new publisher joining in, the clients already subscribed
			// with other subscribers under certain topics, automatically get resubscribed with the new publisher
			// without the change being noticed by the subscribed clients
			
			if (dbManager != null && rpublisher != null && lpublisher != null){
				if (subscriber == null){
					subscriber = new TCPServer(lpublisher, rpublisher, dbManager);
					// The Subscriber service registers the TCP server
					Properties props = new Properties();
					props.put(SubscriberIF.SUBSCRIBER_NAME, TCPServer.NAME);
					ServiceRegistration registration = context.registerService(SubscriberIF.class.getName(), subscriber, props);				
					
					((TCPServer) subscriber).start();
					
					return registration;
				}
				else{
					
					logger.debug("Resubscription");
					
					// TODO nimak - The server may not be suspended at this point. It doesn't cause any problem right now,
					//				but might be something to check for proper behavior later on. Basically resuming the 
					//				thread is not needed if the thread has not been suspended already
					((TCPServer) subscriber).resume();
					try {
						((TCPServer) subscriber).reSubscribe(rpublisher);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return null;
			
		}

		public void modifiedService(ServiceReference reference, Object service) {
			// TODO Auto-generated method stub
		}

		public void removedService(ServiceReference reference, Object service) {
			ServiceRegistration registration = (ServiceRegistration) service;
			
			registration.unregister();
			context.ungetService(reference);
			
			logger.debug("service was unregistered");
			
			try{
				((TCPServer) subscriber).suspend();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
