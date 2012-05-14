/*
 * OSGiBroker Project
 * Copyright (c) UBC Media and Graphics Interdisciplinary Centre (MAGIC) 2007
 * http://www.magic.ubc.ca/
 * 
 */
package ca.ubc.magic.broker.api.ds;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;


import ca.ubc.magic.broker.api.cache.CacheElementIF;
import ca.ubc.magic.broker.api.cache.CacheElementIF.Status;
import ca.ubc.magic.broker.api.notification.NotificationHandlerIF;
import ca.ubc.magic.broker.api.notification.NotificationHelper;

/**
 * 
 * AttributeMap used for events and state.  Attributes are mapped by their name.
 * A name may have more than one attribute to support ordered lists of attributes.
 * 
 * @author mike
 *
 */
public class ElementMap extends CacheObject implements Serializable {

//	protected Map<String, CacheElementIF<Attribute>> attrMap = new HashMap<String, CacheElementIF<Attribute>>();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String STATE_ID = "stateID";
	
	public static final Logger logger = Logger.getLogger(ElementMap.class);
	
	public ElementMap(String topicName) {
		super(topicName);
	}

	/**
	 * Set the attribute value in the attribute map, and remove old
	 * values
	 * 
	 * @param name
	 * @param value
	 * @return old values
	 */
	public String[] setUntypedAttribute(String name, String value) {

		return setUntypedAttribute(name, value, true);
	}
	
	/**
	 * Set the attribute value in the attribute map, and remove old values with 
	 * a cache status indicating if the cache needs to be updated or not
	 * 
	 * @param name
	 * @param value
	 * @param cacheUpdate
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String[] setUntypedAttribute(String name, String value, boolean cacheUpdate){
		
		CacheElementIF<Attribute> values = new StateObjElement<Attribute>();
		values.add(new Attribute(name, value));
		
		if (!cacheUpdate)
			values.setCacheElemStatus(Status.intact);
		else if (objectMap.containsKey(name)){
			if (objectMap.get(name).getCacheElemStatus() != Status.deleted)
				addStateNotification(name, NotificationHandlerIF.Action.updated);
			else
				addStateNotification(name, NotificationHandlerIF.Action.added);
			values.setCacheElemStatus(Status.updated);
		}else{
			values.setCacheElemStatus(Status.added);
			addStateNotification(name, NotificationHandlerIF.Action.added);
		}
		
		return attributeValuesToStringArray(objectMap.put(name, values));
		
	}
	
	@SuppressWarnings("unchecked")
	public void setAttribute(String stateID, Attribute attribute) {
		CacheElementIF<Attribute> values = objectMap.get(stateID);
		NotificationHandlerIF.Action action = null;
		
		if (values == null) {
			values = new StateObjElement<Attribute>();
			values.setCacheElemStatus(Status.added);
			action = NotificationHandlerIF.Action.added;
		}
		if (values.getCacheElemStatus() != Status.added){
			values.setCacheElemStatus(Status.updated);
			if (objectMap.get(attribute.getName()).getCacheElemStatus() != Status.deleted)
				action = NotificationHandlerIF.Action.updated;
			else
				action = NotificationHandlerIF.Action.added;
		}
		
		values.add(attribute);
		objectMap.put(stateID, values);		
		addStateNotification(stateID, action);
	}

	
	/**
	 * Set the attribute value list in the attribute map, and remove old
	 * values
	 * 
	 * @param name
	 * @param value
	 * @return old values
	 */
	public String[] setUntypedAttributeValues(String name, String[] newValues) {
		return setUntypedAttributeValues(name, newValues, true);
	}
	
	@SuppressWarnings("unchecked")
	public String[] setUntypedAttributeValues(String name, String[] newValues, boolean cacheUpdate) {
		
		StateObjElement<Attribute> values = new StateObjElement<Attribute>();
		for(String newVal: newValues) {
			values.add(new Attribute(name, newVal));
		}
		
		if (!cacheUpdate)
			values.setCacheElemStatus(Status.intact);
		else if (objectMap.containsKey(name)){
			if (objectMap.get(name).getCacheElemStatus() != Status.deleted)
				addStateNotification(name, NotificationHandlerIF.Action.updated);
			else
				addStateNotification(name, NotificationHandlerIF.Action.added);
			values.setCacheElemStatus(Status.updated);
		}
		else{
			addStateNotification(name, NotificationHandlerIF.Action.added);
			values.setCacheElemStatus(Status.added);
		}
		
		return attributeValuesToStringArray(objectMap.put(name, values));
	}
	
	/**
	 * Add an attribute to an existing attribute name, or create it if
	 * it doesn't exist.
	 * 
	 * @param name
	 * @param value
	 */
	public void addUntypedAttribute(String name, String value) {
		
		Attribute attr = new Attribute(name, value);
		setElementAttribute(attr);
	}

	/**
	 * Set untyped name value pairs in the attribute map
	 * 
	 * @param objectMap
	 */
	public void setUntypedAttributeMapValues(Map<String, String[]> objectMap) {
		this.setUntypedAttributeMapValues(objectMap, true);
	}
	
	public void setUntypedAttributeMapValues(Map<String, String[]> objectMap, boolean cacheLocalUpdate) {
		for (String name : objectMap.keySet()) {
			this.setUntypedAttributeValues(name, objectMap.get(name), cacheLocalUpdate);
		}
	}
	
	/**
	 * Set untyped name value pairs in the attribute map
	 * 
	 * @param objectMap
	 */
	public void setUntypedAttributeMap(Map<String, String> objectMap) {
		setUntypedAttributeMap(objectMap, true);
	}
	
	public void setUntypedAttributeMap(Map<String, String> objectMap, boolean cacheLocalUpdate){
		for (String name : objectMap.keySet()) {
			this.setUntypedAttribute(name, objectMap.get(name), cacheLocalUpdate);
		}
	}
	

	/**
	 * Remove all values of the named attribute
	 * 
	 * @param name
	 */
	@SuppressWarnings("unchecked")
	public String[] removeUntypedAttribute(String name) {
		
		StateObjElement<Attribute> attributeStateList = (StateObjElement<Attribute>) objectMap.get(name);
		
		attributeStateList.setCacheElemStatus(Status.deleted);
		addStateNotification(name, NotificationHandlerIF.Action.deleted);
		
		objectMap.put(name, attributeStateList);
		
		return this.attributeValuesToStringArray(attributeStateList);
	}
	
	/**
	 * Get the first value of the named attribute
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getUntypedValue(String name, boolean cacheLocalUpdate) {
		StateObjElement<Attribute> attrs = (StateObjElement<Attribute>) objectMap.get(name);
		if (attrs == null || (cacheLocalUpdate && attrs.getCacheElemStatus() == Status.deleted))
			return null;
		return ((Attribute) ((StateObjElement) objectMap.get(name)).get(0)).getValue();
	}
	
	public String getUntypedValue(String name){
		return getUntypedValue(name, true);
	}
	
	/**
	 * Get all of the values of the named attribute
	 * 
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String[] getUntypedValues(String name, boolean cacheLocalUpdate) {
		StateObjElement<Attribute> val = (StateObjElement<Attribute>) objectMap.get(name);
		
		if (val == null || (cacheLocalUpdate && val.getCacheElemStatus() == Status.deleted))
			return null;
		
		String[] valArray = new String[val.size()];
		for (int i=0; i<valArray.length; i++) {
			valArray[i] = val.get(i).getValue();
		}
		
		return valArray;
	}
	
	public String[] getUntypedValues(String name){
		return getUntypedValues(name, true);
	}
	
	/**
	 * Get all of the named values as a Map<String, String[]> map
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String[]> getUntypedAttributesMap(boolean cacheLocalUpdate) {
		HashMap<String, String[]> returnData = new HashMap<String, String[]>();

		for (String name : objectMap.keySet()) {
			StateObjElement<Attribute> val = (StateObjElement<Attribute>) objectMap.get(name);
			
			if (cacheLocalUpdate && val.getCacheElemStatus() == Status.deleted)
				continue;
			
			String[] valArray = new String[val.size()];
			for (int i=0; i<valArray.length; i++) {
				valArray[i] = val.get(i).getValue();
			}
			returnData.put(name, valArray);
		}
		return returnData;
	}
	
	public Map<String, String[]> getUntypedAttributesMap() {
		return getUntypedAttributesMap(true);
	}
	
	public void removeState(String stateID){
		this.removeElement(stateID);
		addStateNotification(stateID, NotificationHandlerIF.Action.deleted);
	}
	
	public void clean(){
		this.cleanElementMap();
		addStateNotification(NotificationHandlerIF.MODIFIED_ALL, NotificationHandlerIF.Action.deleted);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof ElementMap))
		    return false;
		return objectMap.equals(
				((ElementMap)o).objectMap);
	}
	
	private void addStateNotification(String id, NotificationHandlerIF.Action action){
		
		// it doesn't allow notification topics to also emit notifications
		if (NotificationHelper.isNotificationTopic(this.getName()))
			return;
		
		try {
			// notification comes from the super class where the notification gets registered with
			// the observer.
			
			if (notification == null)
				throw new Exception ("No notification object is added to this content");
			
			notification.addNotification(this.getName(), id, action, NotificationHandlerIF.Type.state);
		}catch (Exception e){
			logger.error("Notification failed for content: " + e.getMessage());
		}
		
	}
}
