/**
 * UBC MAGIC (c) 2010
 */
package ca.ubc.magic.broker.subscriber.service.servlet.helper.model;

import java.util.HashMap;
import java.util.Map;

/**
 * A data container class that represents a state on an OSGi Broker topic.
 * 
 * @author vtsao
 */
public class TopicState
{
	private String m_topic;
	private Map <String, TopicStateAttribute> m_stateAttributes;

	/**
	 * Constructor to create a topic state.
	 * 
	 * @param topic The topic this state belongs to.
	 */
	TopicState (String topic)
	{
		m_topic = topic;
		m_stateAttributes = new HashMap <String, TopicStateAttribute> ();
	}

	/** @return The topic this state belongs to. */
	public String getTopic ()
	{
		return m_topic;
	}

	/**
	 * Adds a state attribute to this state.
	 * 
	 * @param name The name of the attribute.
	 * @param attribute The attribute to add.
	 */
	void addAttribute (String name, TopicStateAttribute attribute)
	{
		m_stateAttributes.put (name, attribute);
	}

	/**
	 * Retrieves the attribute with the specified name.
	 * 
	 * @param name The name of the attribute to retrieve.
	 * @return The retrieved attribute.
	 */
	public TopicStateAttribute getAttribute (String name)
	{
		return m_stateAttributes.get (name);
	}

	/** @return An array of all the names of the attributes in this state. */
	public String[] getNameArray ()
	{
		return m_stateAttributes.keySet ().toArray (new String[m_stateAttributes.size ()]);
	}
}
