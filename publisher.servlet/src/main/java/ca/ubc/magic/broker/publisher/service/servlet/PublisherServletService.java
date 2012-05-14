package ca.ubc.magic.broker.publisher.service.servlet;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.http.ExtendedHttpServlet;
import ca.ubc.magic.broker.http.HttpWrapper;

/**
 * The class to hook the servlet to the OSGi system as a service and bring it
 * up for communication 
 * 
 * @author nima kaviani
 *
 */

public class PublisherServletService implements BundleActivator {
	
	private static final Logger logger = Logger.getLogger(PublisherServletService.class);
	
	private HttpWrapper wrapper = null;
	private ServiceTracker tracker = null;
	private PublisherIF publisherService = null;
	private ExtendedHttpServlet servlet = null;
	private BundleContext bc;

	public void start(BundleContext bc) throws Exception {
		logger.info("Starting the publisher servlet...");
		
		this.bc = bc;
		// opens a service tracker to track the publisher service and bring it
		// into its use 
		tracker = new ServiceTracker(bc, PublisherIF.class.getName(), 
									 new PublisherCustomizer());
		tracker.open();				
		
	}

	public void stop(BundleContext bc) throws Exception {
		logger.info("Stopping the publisher servlet...");
		tracker.close();
	}
	
	HttpContext context = new HttpContext() {
		  public URL getResource(String name) {
		    // and send the plain file
		    URL url = getClass().getResource(name);
		
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
    
    private class PublisherCustomizer implements ServiceTrackerCustomizer {

		public Object addingService(ServiceReference reference) {			
			
			// creates the HttpServlet for the PublisherServlets and wraps it with the 
			// OSGi Http Server provided for Knopflerfish
			
			publisherService = (PublisherIF) bc.getService(reference);
			servlet = new PublisherServlet(publisherService);
			wrapper = new HttpWrapper(bc, servlet, context);
			wrapper.open();
			
			logger.info("Publisher detected by the PublisherServlet");
			
			return publisherService;
		}

		public void modifiedService(ServiceReference reference, Object service) {
			// TODO Auto-generated method stub
			
		}

		public void removedService(ServiceReference reference, Object service) {
			publisherService = null;
			servlet = null;
			wrapper.close();	
			bc.ungetService(reference);
			
			logger.info("Publisher lost!");
		}
    	
    }
}
