package ca.ubc.magic.broker.publisher.service.sms;

import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import ca.ubc.magic.broker.api.PublisherIF;

/**
 * The activator class for the OSGiBroker.SMS bundle. The bundle is no longer using a declarative service
 * model in order to implement the required functionality for the bundle. It is easier to manage the 
 * behavior of bundle when it is implemented using a bundle activator as it gives a lower level of
 * control to the programmer.
 * 
 * @author nima
 *
 */
public class SMSActivator implements BundleActivator {

	public static String SMS_PID = "ca.ubc.magic.broker.publisher.service.sms.gateway";
	
	private ServiceRegistration smsReg = null;
	private ServiceTracker tracker;
	private SMSPublisherPolling smsPolling = null;
	private BundleContext context;
	
	public void start(BundleContext _context) throws Exception {
		
		this.context = _context;
		
		smsPolling = new SMSPublisherPolling();
		
		Properties configProps = new Properties();
		configProps.put(Constants.SERVICE_ID, SMSActivator.SMS_PID + ".configuration");
		configProps.put(Constants.SERVICE_PID, SMSActivator.SMS_PID);
		smsReg = context.registerService(ManagedService.class.getName(), SMSManagedService.getInstance(), configProps);
		
		tracker = new ServiceTracker(context, PublisherIF.class.getName(), new PublisherServiceTracker());
		tracker.open();
		
	}

	public void stop(BundleContext context) throws Exception {
		
		tracker.close();
		smsReg.unregister();
		smsPolling.unbindPublisherServ(null);
		smsPolling.deactivate();
		
	}
	
	private class PublisherServiceTracker implements ServiceTrackerCustomizer {

		public Object addingService(ServiceReference ref) {
			
			PublisherIF publisher = (PublisherIF) context.getService(ref);
			smsPolling.bindPublisherServ(publisher);
			return smsPolling;
			
		}

		public void modifiedService(ServiceReference ref, Object srv) {
			
			((SMSPublisherPolling) srv).bindPublisherServ((PublisherIF) context.getService(ref));
			
		}

		public void removedService(ServiceReference ref, Object srv) {
			
			((SMSPublisherPolling) srv).unbindPublisherServ((PublisherIF) context.getService(ref));
			
		}
		
	}

}
