/**
 * UBC MAGIC (c) 2010
 */
package ca.ubc.magic.broker.subscriber.service.servlet.helper.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A data container class that represents an OSGi Broker event.
 * 
 * @author vtsao
 */
public class TopicEvent
{
	private int m_id;
	private long m_timestamp;
	private HashMap <String, String> m_data;

	/**
	 * Constructor to create a new topic event.
	 * 
	 * @param id The id of the event.
	 * @param timestamp The timestamp of the event.
	 */
	TopicEvent (int id, long timestamp)
	{
		m_id = id;
		m_timestamp = timestamp;
		m_data = new HashMap <String, String> ();
	}

	/**
	 * Adds an attribute to this event.
	 * 
	 * @param name The name of the attribute to add.
	 * @param value The value of the attribute to add.
	 */
	void addAttribute (String name, String value)
	{
		m_data.put (name, value);
	}

	/**
	 * Removes an attribute from this event.
	 * 
	 * @param name The name of the attribute to remove.
	 */
	void removeAttribute (String name)
	{
		m_data.remove (name);
	}

	/**
	 * Retrieves an attribute's value.
	 * 
	 * @param name The name of the attribute to retrieve its value.
	 * @return The attribute's value.
	 */
	public String getAttribute (String name)
	{
		return m_data.get (name);
	}

	/**
	 * Checks to see if the specified attribute exists.
	 * 
	 * @param name The name of the attribute to check.
	 * @return True if the attribute exists, false otherwise.
	 */
	public boolean hasAttribute (String name)
	{
		return m_data.containsKey (name);
	}

	/** @return The id of this topic event. */
	public int getId ()
	{
		return m_id;
	}

	/** @return The timestamp of this topic event. */
	public long getTimestamp ()
	{
		return m_timestamp;
	}

	@Override
	public String toString ()
	{
		return m_data.toString ();
	}

	/** @return An array of all the names of the attributes in this event. */
	public String[] getNameArray ()
	{
		return m_data.keySet ().toArray (new String[m_data.size ()]);
	}

	/** @return The topic this event was sent on. */
	public String getTopic ()
	{
		return m_data.get ("topic");
	}
	
	public Map<String, String> getEvnetMap(){
		return m_data;
	}
}
