/**
 * UBC MAGIC (c) 2010
 */
package ca.ubc.magic.broker.subscriber.service.servlet.helper.model;

/**
 * A data container class that represents content on an OSGi Broker topic.
 * 
 * @author vtsao
 */
public class TopicContent
{
	private String m_fileName;
	private String m_fileSize;
	private String m_fileContentType;
	private String m_clientId;
	private String m_topic;
	private String m_url;

	/**
	 * Constructor to create a new topic content.
	 * 
	 * @param fileName The name of the file associated with this content.
	 * @param fileSize The size of the file associated with this content.
	 * @param fileContentType The type of the file associated with this content.
	 * @param clientId The id of the client who uploaded this content.
	 * @param topic The topic this content resides on.
	 * @param url The url to access the content.
	 */
	TopicContent (String fileName, String fileSize, String fileContentType, String clientId, String topic, String url)
	{
		m_fileName = fileName;
		m_fileSize = fileSize;
		m_fileContentType = fileContentType;
		m_clientId = clientId;
		m_topic = topic;
		m_url = url;
	}

	/** @return The name of the file associated with this content. */
	public String getFileName ()
	{
		return m_fileName;
	}

	/** @return The size of the file associated with this content. */
	public String getFileSize ()
	{
		return m_fileSize;
	}

	/** @return The type of the file associated with this content. */
	public String getFileContentType ()
	{
		return m_fileContentType;
	}

	/** @return The id of the client who uploaded this content. */
	public String getClientId ()
	{
		return m_clientId;
	}

	/** @return The topic this content resides on. */
	public String getTopic ()
	{
		return m_topic;
	}

	/** @return The url to access the content. */
	public String getUrl ()
	{
		return m_url;
	}
}
