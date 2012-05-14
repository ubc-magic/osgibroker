/**
 * UBC MAGIC (c) 2010
 */
package ca.ubc.magic.broker.subscriber.service.servlet.helper.model;

/**
 * A data container class that represents a state attribute in an OSGi Broker
 * topic state.
 * 
 * @author vtsao
 */
public class TopicStateAttribute
{
	private String m_name;
	private String m_value;
	private String m_type;

	/**
	 * Constructor to create a new topic state attribute.
	 * 
	 * @param topic The topic whose state this attribute belongs to.
	 * @param name The name of the state attribute.
	 * @param value The value of the state attribute.
	 * @param type The type of the state attribute.
	 */
	TopicStateAttribute (String name, String value, String type)
	{
		m_name = name;
		m_value = value;
		m_type = type;
	}

	/** @return The name of the state attribute. */
	public String getName ()
	{
		return m_name;
	}

	/** @return The value of the state attribute. */
	public String getValue ()
	{
		return m_value;
	}

	/** @return The type of the state attribute. */
	public String getType ()
	{
		return m_type;
	}
}
