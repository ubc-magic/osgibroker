package ca.ubc.magic.broker.impl;

import java.net.SocketException;
import java.util.Dictionary;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.RemotePublisherIF;
import ca.ubc.magic.broker.api.cache.CacheIF;
import ca.ubc.magic.broker.api.cache.CacheStoreIF;
import ca.ubc.magic.broker.api.notification.NotificationHandlerIF;
import ca.ubc.magic.broker.api.storage.DBManagerIF;
import ca.ubc.magic.broker.cache.BrokerCache;
import ca.ubc.magic.broker.cache.CachingThread;
import ca.ubc.magic.broker.cache.content.ContentManager;
import ca.ubc.magic.broker.cache.state.StateManager;
import ca.ubc.magic.broker.notification.NotificationHandler;

/**
 * The class BrokerCore hooks the PublisherImpl class to the OSGi 
 * framework as an OSGi service
 * 
 * @author nima kaviani
 *
 */
public class BrokerCore implements BundleActivator,ManagedService{
	
	private PublisherImpl publisher;
	private CacheIF cache;
	private Thread cachingThread;
	
	private ServiceTracker tracker;
	private BundleContext context;
	
	private ServiceRegistration configReg;
	private static final String THREAD_PID = "ca.ubc.magic.broker.service.core";
	private String savingPeriod;

	private static final String BROKER_CORE_START_MSG = "Starting OSGiBroker Core Service ...";
	private static final String DONE_MSG = "[DONE].";
	
	private static final Logger logger = Logger.getLogger( BrokerCore.class );

	public void start(BundleContext _context) throws Exception {
		
		this.context = _context;
		System.out.print(BROKER_CORE_START_MSG);
		
		Properties props = new Properties();
		props.put(PublisherIF.PUBLISHER_NAME, PublisherImpl.NAME);
		
		//TODO nimak -	The publisher should be enhanced with the DBManager service
		//				so that the subscribers can also be recorded in the OSGiBroker database
		//				right now, no subscription is stored in the database and there is no record
		//				for what subscription is using what service from the database
		publisher = new PublisherImpl();
		context.registerService(PublisherIF.class.getName(), publisher, props);
		context.registerService(RemotePublisherIF.class.getName(), publisher, props );
		
		NotificationHandlerIF notification = new NotificationHandler (publisher);
		
		StateManager   stateManager = new StateManager(notification);
		ContentManager contentManager = new ContentManager(notification);
		
		Properties cacheProps = new Properties();
		props.put(CacheStoreIF.CACHE_NAME, BrokerCache.NAME);
		cache = new BrokerCache();
		cache.addCacheEntry(stateManager);
		cache.addCacheEntry(contentManager);
		
		context.registerService(CacheIF.class.getName(), cache, cacheProps);
		
		tracker = new ServiceTracker(context, DBManagerIF.class.getName(), new CoreServiceCustomizer());
		tracker.open();
		
		initCachingThread();
		
		System.out.println(DONE_MSG);
	}

	public void stop(BundleContext arg0) throws Exception {
		cache.commitCacheToDataStore();
		
		this.configReg = null;
		this.context = null;
		
		System.out.println("Stopping broker core service ...");
	}	
	
	private void initCachingThread() throws ConfigurationException {

		Properties configProps = new Properties();
		configProps.put(Constants.SERVICE_ID,  THREAD_PID + ".configuration");
		configProps.put(Constants.SERVICE_PID, THREAD_PID);
		configReg = context.registerService(ManagedService.class.getName(), this, configProps);
		
		this.updated(null);
	}
	
	public void updated(Dictionary dict) throws ConfigurationException {
		
		if (dict == null)
			return;
		
		// sets the saving period for the content of the cache in the broker
		this.savingPeriod = (String) dict.get("ca.ubc.magic.broker.service.cache.thread.savingPeriod");
		
		if (cachingThread != null && ((CachingThread) cachingThread).getSavingPeriod().equals(savingPeriod)){
			cachingThread.interrupt();
			cachingThread = new CachingThread(cache, this.savingPeriod);
			cachingThread.start();
		}
		
		// sets the ContentManager network interface based on what described in the config file
		try{
			((ContentManager) this.cache.getCacheEntry(ContentManager.class)).updateNetworkIF(
					(String) dict.get("ca.ubc.magic.broker.host.network.interface"));
		}catch(SocketException se){
			logger.error(se.getMessage());
		} catch (BrokerException be) {
			logger.error(be.getMessage());
		}

		logger.debug("SavingPeriod: " + this.savingPeriod);
	}
	
	private class CoreServiceCustomizer implements ServiceTrackerCustomizer {
		public Object addingService(ServiceReference reference) {
			
			publisher.getDBManager((DBManagerIF) context.getService(reference));
			cache.getCacheStore().getDBManager((DBManagerIF) context.getService(reference));
			
			try{
				cache.initCacheFromDataStore();
				
			}catch(Exception e){
				logger.error("Unable to read the cache information from the DataStore!");
				cache.initCacheWithoutDataStore();
			}
			return null;
		}
		
		public void modifiedService(ServiceReference reference, Object service) {
		}

		public void removedService(ServiceReference reference, Object service) {
			publisher.ungetDBManager((DBManagerIF) service);
			cache.getCacheStore().ungetDBManager((DBManagerIF) service);
		}
	};
}

