package ca.ubc.magic.broker.impl;
//package ca.ubc.magic.broker.subscriber.service;
//
//import org.osgi.framework.BundleActivator;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.ServiceReference;
//import org.osgi.framework.ServiceRegistration;
//import org.osgi.util.tracker.ServiceTracker;
//import org.osgi.util.tracker.ServiceTrackerCustomizer;
//
//import ca.ubc.magic.broker.publisher.api.RemotePublisherIF;
//import ca.ubc.magic.broker.subscriber.api.SubscriberIF;
//
//public class SubscriberService implements BundleActivator {
//
//	private BundleContext context = null;
//	private ServiceTracker tracker = null;
//	
//	public void start(BundleContext context) throws Exception {
//		
//		this.context = context;
//		
//		tracker = new ServiceTracker(context, RemotePublisherIF.class.getName(), new PublisherServiceCustomizer());
//		tracker.open();
//	}
//
//	public void stop(BundleContext context) throws Exception {
//		tracker.close();
//		
//	}
//	
//	private class PublisherServiceCustomizer implements ServiceTrackerCustomizer {
//
//		public Object addingService(ServiceReference reference) {
//			
//			RemotePublisherIF publisher = (RemotePublisherIF) context.getService(reference);
//			SubscriberIF subscriber = new SubscriberImpl(publisher);
//			
//			ServiceRegistration registration = context.registerService(SubscriberIF.class.getName(), subscriber, null);
//			
//			return registration;
//		}
//
//		public void modifiedService(ServiceReference reference, Object service) {
//			// TODO Auto-generated method stub
//		}
//
//		public void removedService(ServiceReference reference, Object service) {
//			ServiceRegistration registration = (ServiceRegistration) service;
//			
//			registration.unregister();
//			context.ungetService(reference);
//		}
//	}
//
//}
