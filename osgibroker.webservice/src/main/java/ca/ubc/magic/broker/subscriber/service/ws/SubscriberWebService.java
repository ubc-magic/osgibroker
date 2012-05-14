package ca.ubc.magic.broker.subscriber.service.ws;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.apache.axis2.osgi.deployment.tracker.WSTracker;

import java.util.Dictionary;
import java.util.Properties;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.RemotePublisherIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.api.cache.CacheIF;
import ca.ubc.magic.broker.api.storage.DBManagerIF;

import ca.ubc.magic.broker.impl.SubscriberImpl;
import ca.ubc.magic.broker.subscriber.service.ws.ContextProvider;

public class SubscriberWebService implements BundleActivator{
	
	private ServiceTracker tracker;
	private BundleContext context;
	
	private SubscriberIF subscriber = null;
	
	private Object subscribeWrapper = null;
	private Object unsubscribeWrapper = null;
	
	//private Operations subscribeWS = null;
	private SubscribeWebService subscribeWS = null;
	private UnsubscribeWebService unsubscribeWS = null;
	private EventsWebService eventsWS = null;
	private TopicWebService topicWS = null;
	private StateWebService stateWS = null;
	private KeepAliveWebService keepAliveWS = null;
	
	protected DBManagerIF dbManager = null;
	protected RemotePublisherIF publisher = null;
	protected CacheIF brokerCache = null;

	private static final String WEB_SRVC_START_MSG = "Starting OSGiBroker Web Service ...";
	private static final String DONE_MSG = "[DONE].";	
	
	private static final Logger logger = Logger.getLogger(SubscriberWebService.class);
	
	public void start(BundleContext context) throws Exception {
		this.context = context;
		
		System.out.print(WEB_SRVC_START_MSG);
		
		String filterString = "(|" + "(" + Constants.OBJECTCLASS + "=" + CacheIF.class.getName() + ")" +
								"(|" + "(" + Constants.OBJECTCLASS + "=" + DBManagerIF.class.getName() + ")" +
								"(" + Constants.OBJECTCLASS + "=" + RemotePublisherIF.class.getName() + ")))";
		
		Filter serviceFilter = context.createFilter(filterString);
		
		tracker = new ServiceTracker(context, serviceFilter, new WebServiceCustomizer());
		tracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		tracker.close();	
	}
	
	/**
	 * the following private class is a the filter tracker to make the web service subscriber aware of
	 * appearance or loss of the PublisherIF service. Once the publisher service goes down, the web service
	 * also unregisters itself from communication 
	 * 
	 * @author nima
	 *
	 */
	public class WebServiceCustomizer implements ServiceTrackerCustomizer {
		
		public Object addingService(ServiceReference reference) {
			
			Object serviceObj = context.getService(reference);
			
			if (serviceObj instanceof RemotePublisherIF){
				publisher = (RemotePublisherIF) serviceObj;
				logger.debug("New PublisherService found: " + reference.getProperty(PublisherIF.PUBLISHER_NAME));
			}
			
			if (serviceObj instanceof DBManagerIF){
				dbManager = (DBManagerIF) serviceObj;
				logger.debug("New DBManagerService found: " + reference.getProperty(DBManagerIF.DB_TYPE) + ":" + 
						reference.getProperty(DBManagerIF.DB_NAME));
			}
			
			if (serviceObj instanceof CacheIF){
				brokerCache = (CacheIF) serviceObj;
				logger.debug("New BrokerCache found:");
			}
			
			// if the web service subscriber is just started, the object is initialized but if it has been
			// already up and running but the publisher has gone done and come back or a new publisher
			// has been detected, it only resubscribes with the publisher, preventing the connected services 
			// from being disconnected. Also, in case of a new publisher joining in, the clients already subscribed
			// with other subscribers under certain topics, automatically get resubscribed with the new publisher
			// without the change being noticed by the subscribed clients
			
			if (dbManager != null && publisher != null && brokerCache != null){
				if (subscriber == null){
					subscriber = new WSSubscriberImpl(publisher, dbManager);
					// The Subscriber service registers the required servlets for HTTP polling and interactions
					Properties props = new Properties();
					props.put(SubscriberIF.SUBSCRIBER_NAME, WSSubscriberImpl.NAME);
					ServiceRegistration registration = context.registerService(SubscriberIF.class.getName(), subscriber, props);
					
					//register the subscribe web service
					subscribeWS = new SubscribeWebService();
					ContextProvider.getInstance().init(publisher, dbManager, subscriber, brokerCache);
			        Dictionary subscribeProperty = new Properties();
			        subscribeProperty.put(WSTracker.AXIS2_WS, "subscribe");
			        context.registerService(SubscribeWebService.class.getName(), subscribeWS, subscribeProperty);
					
					//register the unsubscribe web service
					unsubscribeWS = new UnsubscribeWebService();
					ContextProvider.getInstance().init(publisher, dbManager, subscriber, brokerCache);
			        Dictionary unsubscribeProperty = new Properties();
			        unsubscribeProperty.put(WSTracker.AXIS2_WS, "unsubscribe");
			        context.registerService(UnsubscribeWebService.class.getName(), unsubscribeWS, unsubscribeProperty);
					
					//register the events web service
					eventsWS = new EventsWebService();
					ContextProvider.getInstance().init(publisher, dbManager, subscriber, brokerCache);
			        Dictionary eventsProperty = new Properties();
			        eventsProperty.put(WSTracker.AXIS2_WS, "events");
			        context.registerService(EventsWebService.class.getName(), eventsWS, eventsProperty);
			        
					//register the topic web service
					topicWS = new TopicWebService();
					ContextProvider.getInstance().init(publisher, dbManager, subscriber, brokerCache);
			        Dictionary topicProperty = new Properties();
			        topicProperty.put(WSTracker.AXIS2_WS, "topic");
			        context.registerService(TopicWebService.class.getName(), topicWS, topicProperty);
			        
					//register the states web service
					stateWS = new StateWebService();
					ContextProvider.getInstance().init(publisher, dbManager, subscriber, brokerCache);
			        Dictionary stateProperty = new Properties();
			        stateProperty.put(WSTracker.AXIS2_WS, "state");
			        context.registerService(StateWebService.class.getName(), stateWS, stateProperty);
			        
					//register the keepalive web service
					keepAliveWS = new KeepAliveWebService();
					ContextProvider.getInstance().init(publisher, dbManager, subscriber, brokerCache);
			        Dictionary keepAliveProperty = new Properties();
			        keepAliveProperty.put(WSTracker.AXIS2_WS, "keepAlive");
			        context.registerService(KeepAliveWebService.class.getName(), keepAliveWS, keepAliveProperty);
			        
			        System.out.println(DONE_MSG);
			        
					return registration;
				}
				else{
					
					try {
						((SubscriberImpl)subscriber).reSubscribe(publisher);
						logger.debug("Resubscription");
					} catch (Exception e) {
						logger.debug("Resubscriptioni in the SubscriberServletService throws Exception");
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
			subscriber = null;
			
			logger.debug("service was unregistered");
		}
	}
	
	public void bindSubscriber (SubscriberIF _subscriber){
		this.subscriber = _subscriber;
	}
	
	public void unbindSubscriber (SubscriberIF _subscriber){
		if (this.subscriber.equals(_subscriber))
			this.subscriber = null;
	}
}
