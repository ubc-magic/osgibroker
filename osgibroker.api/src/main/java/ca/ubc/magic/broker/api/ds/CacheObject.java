package ca.ubc.magic.broker.api.ds;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ca.ubc.magic.broker.api.cache.CacheElementIF.Status;
import ca.ubc.magic.broker.api.cache.CacheElementIF;
import ca.ubc.magic.broker.api.notification.NotificationHandlerIF;
import ca.ubc.magic.broker.api.notification.NotificationListenerIF;

public abstract class CacheObject implements NotificationListenerIF {
	
private static final long serialVersionUID = 6931772567710279885L;
	
	protected Map<String, CacheElementIF> objectMap = null; 
	protected String cacheObjectTopicName = null;
	protected NotificationHandlerIF notification = null;
	
	protected CacheObject(String _cacheObjectTopicName) {
		super();
		this.cacheObjectTopicName = _cacheObjectTopicName;
		objectMap = new ConcurrentHashMap<String, CacheElementIF>();
	}
	
	public int size() {
		return objectMap.size();
	}
	
	public String getName(){
		return cacheObjectTopicName;
	}
	
	public CacheElementIF getElement(String name, boolean cacheLocalUpdate) {
		CacheElementIF attributeState = objectMap.get(name);
		if ( attributeState == null || (cacheLocalUpdate && attributeState.getCacheElemStatus() == Status.deleted ))
			return null;
		return attributeState;
	}
	
	public CacheElementIF getElement(String name){
		return getElement(name, true);
	}
	
	
	public boolean containsElement(String name) {
		return objectMap.containsKey(name);
	}
	
	@SuppressWarnings("unchecked")
	public Attribute[] getElementAttributes(String name, boolean cacheLocalUpdate) {
		
		CacheElementIF element = objectMap.get(name);
		
		return ((element == null) || (cacheLocalUpdate && element.getCacheElemStatus() == Status.deleted)) ? null :
			(Attribute[]) element.getList().toArray(new Attribute[0]);
	}
	
	public Attribute[] getElementAttributes(String name){
		return getElementAttributes(name, true);
	}
	
	public String[] getElementAttributeNames() {
		return this.getElementAttributeNames(true);
	}
	
	public String[] getElementAttributeNames(boolean cacheLocalUpdate){
		Set<String> names = new HashSet<String>();
		for (String name : (String[]) objectMap.keySet().toArray(new String[0])){
			if (!cacheLocalUpdate || objectMap.get(name).getCacheElemStatus() != Status.deleted)
				names.add(name);
		}
		return names.toArray(new String[0]);
	}
	
	public boolean isEmpty() {
		return objectMap.isEmpty();
	}
	
	public void addNotificationListener(NotificationHandlerIF _notification) {
		this.notification = _notification;
	}
	
	public void removeNotificationListener(){
		this.notification = null;
	}
	
	// ----------------------------------------------------
	// Protected methods
	// ----------------------------------------------------
	
	protected void clear() {		
		for (String key : objectMap.keySet().toArray(new String[0])){
			CacheElementIF attrState = (CacheElementIF) objectMap.get(key);
			attrState.setCacheElemStatus(Status.deleted);
			objectMap.put(key, attrState);
		}
	}
	
	protected void cleanElementMap(){
		Set<String> keySet = objectMap.keySet();
		
		for (String keyName : keySet.toArray(new String[keySet.size()])){
			CacheElementIF attributeState = (CacheElementIF) objectMap.get(keyName) ;
			if (attributeState.getCacheElemStatus() == Status.deleted){
				objectMap.remove(keyName);
			}
			else{
				attributeState.setCacheElemStatus(Status.intact);
				objectMap.put(keyName, attributeState);
			}
		}
	}
	
	protected void setName(String _cacheObjectName){
		this.cacheObjectTopicName = _cacheObjectName;
	}
	
	protected CacheElementIF setElement(String name, CacheElementIF newValues) {
		return setElement(name, newValues, true);
	}
	
	protected CacheElementIF setElement(String name, CacheElementIF newValues, boolean cacheLocalUpdate){
		
		CacheElementIF oldValues = objectMap.get(name);
		
		if (!cacheLocalUpdate)
			newValues.setCacheElemStatus(Status.intact);
		else if (oldValues != null)
			newValues.setCacheElemStatus(Status.updated);
		else
			newValues.setCacheElemStatus(Status.added);
		
		objectMap.put(name, newValues);
		
		return oldValues;
	}
	
	/**
	 * Remove all values of the named attribute
	 * 
	 * @param name
	 */
	@SuppressWarnings("unchecked")
	protected CacheElementIF removeElement(String name) {
		CacheElementIF attributeStateList = objectMap.get(name);
		
		if (attributeStateList.getCacheElemStatus() == Status.deleted)
			return null;
		
		attributeStateList.setCacheElemStatus(Status.deleted);
		return attributeStateList;
	}
	
	
//	This method gets overridden by each class the extends topic object. This way they
//	can decide about what to put inside a content holder
	protected void setElementAttribute(String name, Attribute attribute) {}
	
	protected void setElementAttribute(Attribute attribute){
		setElementAttribute(attribute.getName(), attribute);
	}
	
	/**
	 * Get attribute values in a string array
	 * 
	 * @param attributes
	 * @return
	 */
	protected String[] attributeValuesToStringArray(CacheElementIF<Attribute> attributes) {
			return attributeValuesToStringArray(attributes, true);
	}
	
	protected String[] attributeValuesToStringArray(CacheElementIF<Attribute> attributes, boolean cacheLocalUpdate){
		
		if (attributes == null || (cacheLocalUpdate && attributes.getCacheElemStatus() == Status.deleted))
			return null;
		String[] values = new String[attributes.size()];
		for (int i=0; i<values.length; i++) {
			values[i] = attributes.get(i).getValue();
		}
		return values;	
		
	}
}
