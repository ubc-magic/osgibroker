package ca.ubc.magic.broker.sms.installer;

class InstallerConfig {
	
	public enum InstallerState {OFF, ON, UNKNOWN};
	
	protected InstallerState installerState      = null;
	protected String windowsCOMMLocation         = null; 
	protected String linuxCOMMLocation           = null;
	protected String smsLibBundleLocation        = null;
	protected String osgibrokerSMSBundleLocation = null; 
	
	public boolean equals(Object o){
		
		if (o == null || !o.getClass().equals(this.getClass()))
			return false;
		
		InstallerConfig another = (InstallerConfig) o;
		
		if (this.windowsCOMMLocation.equals(another.windowsCOMMLocation) && 
			this.linuxCOMMLocation.equals(another.linuxCOMMLocation) &&
			this.smsLibBundleLocation.equals(another.smsLibBundleLocation) && 
			this.osgibrokerSMSBundleLocation.equals(another.osgibrokerSMSBundleLocation) &&
			this.installerState.equals(another.installerState))
			
			return true;
		
		return false;
	}
	
}
