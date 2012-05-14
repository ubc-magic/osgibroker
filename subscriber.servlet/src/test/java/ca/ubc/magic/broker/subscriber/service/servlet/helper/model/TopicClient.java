/**
 * UBC MAGIC (c) 2010
 */
package ca.ubc.magic.broker.subscriber.service.servlet.helper.model;

/**
 * A data container class that represents an OSGi Broker client.
 * 
 * @author vtsao
 */
public class TopicClient
{
	private String m_clientId;
	private String m_eventURL;
	private String m_eventFormat;
	private String m_type;

	/**
	 * Constructor to create a new topic client.
	 * 
	 * @param clientId The id of the client.
	 */
	TopicClient (String clientId, String type)
	{
		this (clientId, null, null, type);
	}

	/**
	 * Constructor to create a new topic client.
	 * 
	 * @param clientId The id of the client.
	 * @param eventURL The url direct polling is using.
	 * @param format The format direct polled events are serialized in when they
	 *        are sent.
	 */
	TopicClient (String clientId, String eventURL, String format, String type)
	{
		m_clientId = clientId;
		m_eventURL = eventURL;
		m_eventFormat = format;
		m_type = type;
	}

	/** @return The client id of this subscriber. */
	public String getClientId ()
	{
		return m_clientId;
	}

	/**
	 * @return The url events are being copied to if this subscriber is using
	 *         direct polling, otherwise it returns null.
	 */
	public String getEventURL ()
	{
		return m_eventURL;
	}

	/**
	 * @return The format direct polled events are being serialized in before
	 *         they are sent if this subscriber is using direct polling,
	 *         otherwise it returns null.
	 */
	public String getEventFormat ()
	{
		return m_eventFormat;
	}

	/** @return The type of this client. */
	public String getType ()
	{
		return m_type;
	}
}
