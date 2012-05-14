package ca.ubc.magic.broker.subscriber.service.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import ca.ubc.magic.broker.http.ExtendedHttpServlet;
import ca.ubc.magic.broker.http.HttpWrapper;
import ca.ubc.magic.broker.http.TestHttpContext;
import ca.ubc.magic.broker.impl.SubscriberImpl;

public class SubscriberServletService implements BundleActivator{
	
	private ServiceTracker tracker;
	private BundleContext context;
	private SubscriberIF subscriber = null;
	private HttpWrapper httpWrapper = null;
	
	protected DBManagerIF dbManager = null;
	protected RemotePublisherIF rPublisher = null;
	protected PublisherIF       lPublisher = null;
	protected CacheIF brokerCache = null;

	private static final String SERVLET_SRVC_START_MSG = "Starting OSGiBroker Servlet Service ...";
	private static final String DONE_MSG = "[DONE].";	
	
		private static final Logger logger = Logger.getLogger(SubscriberServletService.class);
	
	public void start(BundleContext context) throws Exception {
		this.context = context;
		
		System.out.print(SERVLET_SRVC_START_MSG);
		
		String filterString = "(|" + "(" + Constants.OBJECTCLASS + "=" + CacheIF.class.getName() + ")" +
								"(|" + "(" + Constants.OBJECTCLASS + "=" + DBManagerIF.class.getName() + ")" +
								"(|" + "(" + Constants.OBJECTCLASS + "=" + PublisherIF.class.getName() + ")" +
								"(" + Constants.OBJECTCLASS + "=" + RemotePublisherIF.class.getName() + "))))";
		
		Filter serviceFilter = context.createFilter(filterString);
		
		tracker = new ServiceTracker(context, serviceFilter, new ServletServiceCustomizer());
		tracker.open();
		
		System.out.println(DONE_MSG);
		
	}

	public void stop(BundleContext context) throws Exception {
		tracker.close();	
	}
	
	HttpContext httpContext = new HttpContext() {
		
		  public URL getResource(String name) {
		    // and send the plain file
			URL url = null;
			try {  
			
			  if (name.contains("content")){
				  url = new URL("file:" + "_content/" + name.substring(name.lastIndexOf("/")+1, name.length()));
			  }
			  else
				  url = getClass().getResource(name);
			  
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			    
		    return url;
		  }
		
		  public String getMimeType(String reqEntry) {
		    return null; // server decides type
		  }
		
		  public boolean handleSecurity( HttpServletRequest request,
		                                 HttpServletResponse response )
		    throws IOException
		  {
		    // Security is handled by server
		    return true;
		  }
	};    
	
	/**
	 * the following private class is a the filter tracker to make the Servlet Subscriber aware of
	 * appearance or loss of the PublisherIF service. Once the publisher service goes down, the Servlet Service
	 * also unregisters itself from communication 
	 * 
	 * @author nima
	 *
	 */
	private class ServletServiceCustomizer implements ServiceTrackerCustomizer {
		
		public Object addingService(ServiceReference reference) {
			
			Object serviceObj = context.getService(reference);
			
			if (serviceObj instanceof RemotePublisherIF){
				rPublisher = (RemotePublisherIF) serviceObj;
				logger.debug("New PublisherService found: " + reference.getProperty(PublisherIF.PUBLISHER_NAME));
			}
			
			if (serviceObj instanceof PublisherIF){
				lPublisher = (PublisherIF) serviceObj;
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
			
			// if the Servlet Subscriber is just started, the object is initialized but if it has been
			// already up and running but the publisher has gone done and come back or a new publisher
			// has been detected, it only resubscribes with the publisher, preventing the connected services 
			// from being disconnected. Also, in case of a new publisher joining in, the clients already subscribed
			// with other subscribers under certain topics, automatically get resubscribed with the new publisher
			// without the change being noticed by the subscribed clients
			
			if (dbManager != null && rPublisher != null && lPublisher != null && brokerCache != null){
				if (subscriber == null){
					
					logger.debug("initiating the subscriber");
					
					subscriber = new ServletSubscriberImpl(rPublisher, dbManager);
					// The Subscriber service registers the required servlets for HTTP polling and interactions
					Properties props = new Properties();
					props.put(SubscriberIF.SUBSCRIBER_NAME, ServletSubscriberImpl.NAME);
					ServiceRegistration registration = context.registerService(SubscriberIF.class.getName(), subscriber, props);
					
					// The ContextProvider gets initialized at this point by the discovered publisher, dbManager, and the 
					// created subscriber. This is a very important step for the system to work properly as the clients
					// use this to initialize their communications.
					ContextProvider.getInstance().init(lPublisher, rPublisher, dbManager, subscriber, brokerCache);
					logger.debug("ContextProvider added");
					
					// adding servlets to a list in order to pass to the HttpWrapper.
					List<ExtendedHttpServlet> servletList = new ArrayList<ExtendedHttpServlet>();
					servletList.add(new SubscribeServlet());
					servletList.add(new UnsubscribeServlet());
					servletList.add(new EventsServlet());
					servletList.add(new TopicServlet());
					servletList.add(new StateServlet());
					servletList.add(new ContentServlet());
					servletList.add(new TestServlet());
					servletList.add(new KeepAliveServlet());
					logger.debug("Servlets added.");
					
					httpWrapper = new HttpWrapper(context, httpContext, servletList);
					httpWrapper.open();
					
					return registration;
				}
				else{
					
					try {
						((SubscriberImpl)subscriber).reSubscribe(rPublisher);
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

			httpWrapper.close();
			
			registration.unregister();
			context.ungetService(reference);
			subscriber = null;

			logger.debug("service was unregistered");
		}
	}
}
