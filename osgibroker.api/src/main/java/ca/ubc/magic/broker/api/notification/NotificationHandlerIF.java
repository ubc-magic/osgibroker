package ca.ubc.magic.broker.api.notification;

public interface NotificationHandlerIF {
	
	public enum Action {
		added, updated, deleted
	};
	
	public enum Type {
		content, state, subscriber
	};
	
	public static final String MODIFIED_ID = "id";
	public static final String MODIFIED_ALL = "all";
	public static final String MODIFIED_NONE = "none";
	
	public static final String ACTION = "action";
	public static final String ACTION_ADD = "added";
	public static final String ACTION_UPDATE = "updated";
	public static final String ACTION_DELETE = "deleted";
	public static final String ACTION_INVALID = "invalid";
	
	public static final String TYPE = "type";
	public static final String TYPE_CONTENT = "content";
	public static final String TYPE_STATE = "state";
	public static final String TYPE_SUBSCRIBER = "subscriber";
	public static final String TYPE_INVALID = "invalid";
	
	public static final String TOPIC_NOTIFICATION_EXTENSION = "__notification__"; 
	
	public void addNotification(String topic, String id, NotificationHandlerIF.Action action, NotificationHandlerIF.Type type) throws Exception;
}
