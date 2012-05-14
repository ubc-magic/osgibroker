package ca.ubc.magic.broker.api.ds;

import java.util.ArrayList;
import java.util.List;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.cache.CacheElementIF;

@SuppressWarnings("hiding")
public class StateObjElement<Attribute> implements CacheElementIF<Attribute> {

	protected List<Attribute> attributeList = null;
	protected Status cacheStatus = null;
	
	public StateObjElement(){
		
		attributeList = new ArrayList<Attribute>();
		cacheStatus = Status.intact;
	}
	
	public boolean add(Attribute obj){
		return attributeList.add(obj);
	}
	
	public int size(){
		return attributeList.size();
	}
	
	public Attribute get(int index){
		return attributeList.get(index);
	}
	
	public Attribute[] toArray(Attribute[] obj){
		return attributeList.toArray(obj);
	}
	
	public List<Attribute> getList(){
		return this.attributeList;
	}
	
	public void setList(List<Attribute> list){
		this.attributeList = list;
	}

	public Status getCacheElemStatus() {
		return cacheStatus;
	}

	public void setCacheElemStatus(Status _status) {
		this.cacheStatus = _status;
	}
	
	public ca.ubc.magic.broker.api.ds.Attribute get(String attributeID) throws BrokerException {
		
		ca.ubc.magic.broker.api.ds.Attribute attribute = null;
		for (int i = 0; i < attributeList.size(); i++){
			
			attribute = (ca.ubc.magic.broker.api.ds.Attribute) attributeList.get(i);
			
			if (attribute.getName().equals(attributeID))
				break;
		}
		if (attribute == null)
			throw new BrokerException("attribute was not found for attributeID: " + attributeID);
		return attribute;
	}
}
