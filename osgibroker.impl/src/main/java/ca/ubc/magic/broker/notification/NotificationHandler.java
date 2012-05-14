package ca.ubc.magic.broker.notification;

import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.notification.NotificationHandlerIF;
import ca.ubc.magic.broker.api.notification.NotificationHelper;

public class NotificationHandler implements NotificationHandlerIF {
	
	private PublisherIF publisher = null;
	
	public NotificationHandler(PublisherIF _publisher){
		
		publisher = _publisher;
	}

	public synchronized void addNotification(String topic, String id, NotificationHandlerIF.Action action, NotificationHandlerIF.Type type) throws Exception {
		
		String notificationAction = null;
		String notificationType = null;
		
		switch (action){
		case added:
			notificationAction = NotificationHandlerIF.ACTION_ADD;
			break;
		case deleted:
			notificationAction = NotificationHandlerIF.ACTION_DELETE;
			break;
		case updated:
			notificationAction = NotificationHandlerIF.ACTION_UPDATE;
			break;
		default: 
			notificationAction = NotificationHandlerIF.ACTION_INVALID;
		}
		
		switch (type){
		case content:
			notificationType = NotificationHandlerIF.TYPE_CONTENT;
			break;
		case state:
			notificationType = NotificationHandlerIF.TYPE_STATE;
			break;
		case subscriber:
			notificationType = NotificationHandlerIF.TYPE_SUBSCRIBER;
			break;
		default:
			notificationType = NotificationHandlerIF.TYPE_INVALID;
		}
		
		Event event = new Event();
		event.addAttribute(NotificationHandlerIF.ACTION, notificationAction);
		event.addAttribute(NotificationHandlerIF.TYPE, notificationType);
		event.addAttribute(NotificationHandlerIF.MODIFIED_ID, id);
		
		String notificationTopic = NotificationHelper.getNotificationTopic(topic);
		publisher.deliver(event, notificationTopic);
	}

}
