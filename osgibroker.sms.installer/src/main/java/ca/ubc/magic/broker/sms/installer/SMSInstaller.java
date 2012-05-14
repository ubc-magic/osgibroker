package ca.ubc.magic.broker.sms.installer;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import ca.ubc.magic.broker.sms.installer.InstallerConfig.InstallerState;

class SMSInstaller {
	
	private static Logger logger = Logger.getLogger(SMSInstaller.class);
	
	public enum OperatingSystem {LINUX, MAC, WINDOWS};
	public enum BundleStatus {INSTALL, UNINSTALL, UPDATE, REINSTALL, START, STOP};
	
	
	private class BundleContainer {
		
		Bundle bundle = null;
		BundleStatus stat;
		
		public void install(OperatingSystem OS, String bundleLocation) throws BundleException {
			
			if (this.stat.equals(BundleStatus.INSTALL) || this.stat.equals(BundleStatus.REINSTALL)){
				if (this.stat.equals(BundleStatus.REINSTALL))
					this.uninstall();
				
				this.bundle = context.installBundle(bundleLocation);
			}
			this.bundle.start();
			this.bundle.update();			
		}
		
		public void stop() throws BundleException {
			
			if (this.bundle != null && this.stat.equals(BundleStatus.STOP)){
				this.bundle.stop();
			}
			
		}
		
		public void uninstall() throws BundleException {
			
			if (this.bundle != null)
				this.bundle.uninstall();
		}
		
		public void markBundle(OperatingSystem OS, InstallerConfig config) throws BundleException {
			
			if (InstallerState.ON.equals(config.installerState)){
				
				if (this.bundle == null)
					this.stat = BundleStatus.INSTALL;
				else if (this.bundle.getLocation().equals(config.windowsCOMMLocation) && OperatingSystem.LINUX.equals(OS))
					this.stat = BundleStatus.REINSTALL;
				else
					if (this.bundle.getState() == Bundle.ACTIVE)
						this.stat = BundleStatus.UPDATE;
					else if ((this.bundle.getState() == Bundle.RESOLVED) || (this.bundle.getState() == Bundle.INSTALLED))
						this.stat = BundleStatus.START;
			}
			else if (InstallerState.OFF.equals(config.installerState)){
				
				if (this.bundle != null)
					this.stat = BundleStatus.STOP;
			}
			else
				throw new BundleException("Wrong State for the SMS Installer State");
		}
		
	}
	
	
	private BundleContext context = null;
	
	private BundleContainer serialCommBundle	= null;
	private BundleContainer smsLibBundle  		= null;
	private BundleContainer smsOSGiBrokerBundle	= null;
	
	private OperatingSystem os = null;
	
	public SMSInstaller (BundleContext _context) throws Exception {
		
		String OS = System.getProperty("os.name","").toLowerCase();
		
		if (OS.startsWith("windows")) 	
			this.os = OperatingSystem.WINDOWS;
		else if ("linux".equals(OS) || "freebsd".equals(OS))	
			this.os = OperatingSystem.LINUX;
		else if (OS.startsWith("mac"))		
			this.os = OperatingSystem.MAC;
		else throw new Exception ("Operating System not detected!");
		
		serialCommBundle 	= new BundleContainer();
		smsLibBundle		= new BundleContainer();
		smsOSGiBrokerBundle	= new BundleContainer();
		
		this.context = _context;
	}
	
	public void run(InstallerConfig config) {
		
		try{
			
			checkFrameworkBundleStatus(config);
			markBundleInstallationStatus (config);
			
			if (InstallerState.ON.equals(config.installerState))
				install(config);
			else if (InstallerState.OFF.equals(config.installerState))
				stop(config);
			
		} catch (BundleException be){
			logger.error(be.getMessage());
		}
	}
	
	protected void checkFrameworkBundleStatus(InstallerConfig config){
		
		serialCommBundle.bundle = smsLibBundle.bundle = smsOSGiBrokerBundle.bundle = null;
		
		for (Bundle bundle : context.getBundles())
		{	
			if (bundle.getLocation().equals(config.linuxCOMMLocation) || bundle.getLocation().equals(config.windowsCOMMLocation))
				serialCommBundle.bundle = bundle;
			
			if (bundle.getLocation().equals(config.smsLibBundleLocation))
				smsLibBundle.bundle = bundle;
			
			if (bundle.getLocation().equals(config.osgibrokerSMSBundleLocation))
				smsOSGiBrokerBundle.bundle  = bundle;
		}
	}
	
	protected void markBundleInstallationStatus (InstallerConfig config){
		
		try{
			
			serialCommBundle.markBundle(this.os, config);
			smsLibBundle.markBundle(this.os, config);
			smsOSGiBrokerBundle.markBundle(this.os, config);
			
		}catch (BundleException be){
			
			System.out.println(be.getMessage());
			logger.error(be.getMessage());
			
		}
		
	}
	
	protected void install(InstallerConfig config) throws BundleException {
		
		if (!OperatingSystem.WINDOWS.equals(this.os) && !OperatingSystem.LINUX.equals(this.os))
			throw new BundleException ("The COMM library does not support your Operating System.");
			
		if (OperatingSystem.WINDOWS.equals(this.os))
			serialCommBundle.install(this.os, config.windowsCOMMLocation);
		else if (OperatingSystem.LINUX.equals(this.os))
			serialCommBundle.install(this.os, config.linuxCOMMLocation);
		
		smsLibBundle.install(this.os, config.smsLibBundleLocation);
		smsOSGiBrokerBundle.install(this.os, config.osgibrokerSMSBundleLocation);
		
	}
	
	protected void stop(InstallerConfig config) throws BundleException{
		
		smsOSGiBrokerBundle.stop();
		smsLibBundle.stop();
		serialCommBundle.stop();
		
	}
		
}