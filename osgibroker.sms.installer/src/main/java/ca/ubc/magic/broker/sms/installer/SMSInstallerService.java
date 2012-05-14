package ca.ubc.magic.broker.sms.installer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import ca.ubc.magic.broker.sms.installer.InstallerConfig.InstallerState;

public class SMSInstallerService implements BundleActivator, ManagedService {
	
	private static final Logger logger = Logger.getLogger(SMSInstallerService.class);
	
	private static final String INSTALLER_PID = "ca.ubc.magic.broker.service.sms.installer";
	
	private BundleContext context;
	private ServiceRegistration configReg;
	
	private SMSInstaller installer = null;
	
	public void start(BundleContext _context) throws Exception {
		
		this.context = _context;
		
		installer = new SMSInstaller(this.context);
		
		Properties configProps = new Properties();
		configProps.put(Constants.SERVICE_ID,  SMSInstallerService.INSTALLER_PID + ".configuration");
		configProps.put(Constants.SERVICE_PID, SMSInstallerService.INSTALLER_PID);
		configReg = context.registerService(ManagedService.class.getName(), this, configProps);
		
		this.updated(null);
		
	}

	public void stop(BundleContext _context) throws Exception {
		
		this.configReg = null;
		this.context = null;
		this.installer = null;
		
	}
	
	public void updated(Dictionary dict) throws ConfigurationException {
		
		if (dict == null)
			return;
		
		final String FILE_PREFIX = "file:"; 
		
		InstallerConfig config = new InstallerConfig();
		
		config.linuxCOMMLocation           = FILE_PREFIX + (String) dict.get("ca.ubc.magic.broker.service.sms.installer.linux");
		config.windowsCOMMLocation         = FILE_PREFIX + (String) dict.get("ca.ubc.magic.broker.service.sms.installer.win32");
		config.smsLibBundleLocation        = FILE_PREFIX + (String) dict.get("ca.ubc.magic.broker.service.sms.installer.smslib");
		config.osgibrokerSMSBundleLocation = FILE_PREFIX + (String) dict.get("ca.ubc.magic.broker.service.sms.installer.osgibrokersms");
		
		if ("ON".equals((String) dict.get("ca.ubc.magic.broker.service.sms.installer")))
			config.installerState = InstallerState.ON;
		else if ("OFF".equals((String) dict.get("ca.ubc.magic.broker.service.sms.installer")))
			config.installerState = InstallerState.OFF;
		else 
			config.installerState = InstallerState.UNKNOWN;

		
		logger.debug("Installation Status: " + config.installerState);
		logger.debug("Linux SMS Lib: " + config.linuxCOMMLocation);
		logger.debug("WINDOWS SMS Lib: " + config.windowsCOMMLocation);
		logger.debug("SMS Lib Bundle: " + config.smsLibBundleLocation);
		logger.debug("OSGiBroker SMS Lib: " + config.osgibrokerSMSBundleLocation);
		
		installer.run(config);
		
	}
}
