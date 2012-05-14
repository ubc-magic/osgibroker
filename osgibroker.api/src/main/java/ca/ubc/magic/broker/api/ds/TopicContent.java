package ca.ubc.magic.broker.api.ds;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.cache.CacheElementIF;
import ca.ubc.magic.broker.api.cache.CacheElementIF.Status;
import ca.ubc.magic.broker.api.notification.NotificationHandlerIF;
import ca.ubc.magic.broker.api.notification.NotificationHelper;

public class TopicContent extends CacheObject implements Serializable {
	
	public static final String FILE_NAME = "fileName";
	public static final String FILE_SIZE = "fileSize";
	public static final String FILE_CONTENT_TYPE = "fileContentType";
	
	private static final String  CONTENT_DIR   = "./_content/";
	public  static final String  CONTENT_ID = "contentID";
	
	public static final Logger logger = Logger.getLogger(TopicContent.class);
	
	private static final long serialVersionUID = -89587154927977135L;
	
	
	public TopicContent(String topicName){
		super(topicName);
	}
	
	public void setContentEntity(Serializable serializable, List<Attribute> attrList) throws Exception{
		
		List<Attribute> contentAttrList = attrList;
		NotificationHandlerIF.Action notificationAction = null;
		
		FileItem fi = (FileItem) serializable;
		File file = new File(CONTENT_DIR + fi.getName());
		
		contentAttrList.add( new Attribute(FILE_NAME, fi.getName()) );
		contentAttrList.add( new Attribute(FILE_SIZE, new Long(fi.getSize()).toString()) );
		contentAttrList.add( new Attribute(FILE_CONTENT_TYPE, fi.getContentType()) );

		fi.write(file);
		
		CacheElementIF<Attribute> content = new ContentObjElement<Attribute>();
		((ContentObjElement<Attribute>) content).setList(contentAttrList);
		
		if (objectMap.containsKey(fi.getName()) && objectMap.get(fi.getName()).getCacheElemStatus() != Status.deleted){
			content.setCacheElemStatus(Status.updated);
			notificationAction = NotificationHandlerIF.Action.updated;
		}else{
			content.setCacheElemStatus(Status.added);
			notificationAction = NotificationHandlerIF.Action.added;
		}
		
		objectMap.put(fi.getName(), content);
		addContentNotification(fi.getName(), notificationAction);
	}
	
	
	@SuppressWarnings("unchecked")
	public CacheElementIF setElement(String contentID, CacheElementIF element) {
		
		NotificationHandlerIF.Action notificationAction = null;
		
		CacheElementIF<Attribute> oldElement = objectMap.get(contentID);
		
		if (oldElement == null) {
			element.setCacheElemStatus(Status.added);
			notificationAction = NotificationHandlerIF.Action.added;
		}
		else if (oldElement.getCacheElemStatus() != Status.added){
			element.setCacheElemStatus(Status.updated);
			if (oldElement.getCacheElemStatus() != Status.deleted)
				notificationAction = NotificationHandlerIF.Action.updated;
			else
				notificationAction = NotificationHandlerIF.Action.added;
		}
		
		objectMap.put(contentID, element);
		addContentNotification(contentID, notificationAction);
		
		return oldElement;
	}
	
	public String getContentID(Serializable item){
		FileItem fitem = (FileItem) item;
		return fitem.getName();
	}
	
	@SuppressWarnings("unchecked")
	public CacheElementIF removeContent(ContentObjElement<Attribute> content) throws BrokerException{
		
		Attribute attr = content.get(TopicContent.FILE_NAME);
		return removeContent(attr.getValue());
		
	}

	@SuppressWarnings("unchecked")
	public CacheElementIF removeContent(String contentID) throws BrokerException {
		
		String contentName = ((ContentObjElement) objectMap.get(contentID)).get(TopicContent.FILE_NAME).getValue();

		CacheElementIF contentAttributes = this.removeElement(contentID);
		
		File file = new File(TopicContent.CONTENT_DIR + contentName);
		if (!file.delete())
			logger.error("The file with the fileName: " + contentName + " could not be found / deleted");

		addContentNotification(contentID, NotificationHandlerIF.Action.deleted);
		
		return contentAttributes;
	}
	
	@SuppressWarnings("unchecked")
	public ContentObjElement<Attribute> getContent(String contentID) {
		return (objectMap.get(contentID) == null || ((ContentObjElement<Attribute>) objectMap.get(contentID)).getCacheElemStatus() == Status.deleted ) ? 
				null : (ContentObjElement<Attribute>) objectMap.get(contentID);
	}
	
	public Map<String, CacheElementIF> getAllContent(){
		return objectMap;
	}
	
	//TODO nimak -	We have to find a workaround for this for sure
	public String getContentLink(String contentID) throws BrokerException {
		
		String contentName = ((ContentObjElement) objectMap.get(contentID)).getCacheElemStatus() != Status.deleted ?
				((ContentObjElement) objectMap.get(contentID)).get(TopicContent.FILE_NAME).getValue() : null;
				
		if (contentName != null){
		
			File file = new File(TopicContent.CONTENT_DIR + contentName);
			if (file.exists())
				return TopicContent.CONTENT_DIR + contentName;
		}
		
		return null;
	}
	
	public boolean hasContent(String contentID, ContentObjElement<Attribute> content){
		return ((ContentObjElement<Attribute>) this.getElement(contentID)).equals(content) ?
				 true : false;
	}
	
	public boolean hasContent(FileItem content){
		return (this.getElement(content.getName()) != null) ? true : false;
	}
	
	public void clean(){
		this.cleanElementMap();
		addContentNotification(NotificationHandlerIF.MODIFIED_ALL, NotificationHandlerIF.Action.deleted);
	}
	
	private void addContentNotification(String id, NotificationHandlerIF.Action action) {
		
		// it doesn't allow notification topics to also emit notifications
		if (NotificationHelper.isNotificationTopic(this.getName()))
			return;
		
		try {
			// notification comes from the super class where the notification gets registered with
			// the observer.
			if (notification == null)
				throw new Exception ("No notification object is added to this content");
			
			notification.addNotification(this.getName(), id, action, NotificationHandlerIF.Type.content);
		}catch (Exception e){
			logger.error("Notification failed for content: " + e.getMessage());
		}
	}
}
