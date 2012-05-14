package ca.ubc.magic.broker.api.notification;


public interface NotificationListenerIF {

	public void addNotificationListener(NotificationHandlerIF notification);
	
	public void removeNotificationListener();
	
}
