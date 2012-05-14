package ca.ubc.magic.broker.api.notification;

/**
 * The NotificationHelper is a stateless class providing the notification topic names for a particular topic or determining whether 
 * a topic name is a notification topic name or a regular topic name.
 * 
 * TODO nimak -	It might not be the proper package for this class to be located in. Preferrably it should be moved
 * 				to the IMPL package, but since it needs to be used also by the storage module, for the time being we
 * 				place it here. A better re-arrangement may come in handy at a later point.
 * 
 * @author nima
 *
 */

public class NotificationHelper {
	
	public static String getNotificationTopic(String topic){
		return topic + NotificationHandlerIF.TOPIC_NOTIFICATION_EXTENSION;
	}
	
	public static boolean isNotificationTopic(String topic){
		return topic.endsWith(NotificationHandlerIF.TOPIC_NOTIFICATION_EXTENSION) ? true : false; 
	}
}
