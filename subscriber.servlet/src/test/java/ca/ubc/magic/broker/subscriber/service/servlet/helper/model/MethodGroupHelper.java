/**
 * UBC MAGIC (c) 2010
 */
package ca.ubc.magic.broker.subscriber.service.servlet.helper.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ca.ubc.magic.broker.api.BrokerException;


/**
 * Helper class to invoke http request methods and parse http request method
 * responses.
 * 
 * @author vtsao
 */
public class MethodGroupHelper
{
	private static final int MAX_HOST_CONNECTIONS = 30;
	private static final int MAX_TOTAL_CONNECTIONS = 100;

	private static HttpClient http = null;

	static void initHttpClient ()
	{
		HttpConnectionManagerParams connectionManagerParams = new HttpConnectionManagerParams ();
		connectionManagerParams.setDefaultMaxConnectionsPerHost (MAX_HOST_CONNECTIONS);
		connectionManagerParams.setMaxTotalConnections (MAX_TOTAL_CONNECTIONS);

		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager ();
		connectionManager.setParams (connectionManagerParams);

		http = new HttpClient (connectionManager);
	}

	/**
	 * Invokes the specified http request method.
	 * 
	 * @param method The http method to invoke.
	 * @throws OSGiBrokerException If the invocation fails on a low level.
	 */
	static void invokeMethod (HttpMethod method) throws BrokerException
	{
		if (http == null)
			initHttpClient ();

		try
		{
			int status = http.executeMethod (method);

			if (status != HttpStatus.SC_OK && status != HttpStatus.SC_NO_CONTENT)
				throw new BrokerException (status, method.getStatusText ());
		}
		catch (HttpException e)
		{
			throw new BrokerException ("Fatal protocol violation: HTTPException - " + e.getMessage ());
		}
		catch (IOException e)
		{
			throw new BrokerException ("Fatal transport error: IOException - " + e.getMessage ());
		}
	}

	/**
	 * Helper method to parse events on a topic serialized in XML returned from
	 * the OSGi Broker.
	 * 
	 * @param is The inputstream to parse the events from.
	 * @return The events in an array of TopicEvent objects.
	 * @throws SAXException A fatal parsing exception, the XML may be malformed,
	 *         it is probably an OSGi Broker bug if it's returning malformed
	 *         XML.
	 * @throws IOException If there was a problem reading from the inputstream,
	 *         the stream might have been closed prematurely.
	 * @throws ParserConfigurationException Low level fatal exception.
	 */
	public static TopicEvent[] parseEvents (InputStream is) throws SAXException, IOException, ParserConfigurationException
	{
		SAXTopicEventParser parseHandler = new SAXTopicEventParser ();

		SAXParser parser = SAXParserFactory.newInstance ().newSAXParser ();
		parser.parse (is, parseHandler);

		return parseHandler.getTopicEvents ();
	}

	private static class SAXTopicEventParser extends DefaultHandler
	{
		private ArrayList <TopicEvent> m_events;
		private boolean m_inEvent;
		private boolean m_inAttribute;
		private TopicEvent m_currentEvent;
		private String m_currentName;
		private StringBuffer m_currentValue;

		/**
		 * Constructor to create a new instance of a SAX parser to parse events
		 * serialized in XML returned from the OSGi Broker.
		 */
		SAXTopicEventParser ()
		{
			m_events = new ArrayList <TopicEvent> ();
			m_inEvent = false;
			m_inAttribute = false;
		}

		@Override
		public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if (qName.equals ("event"))
			{
				m_inEvent = true;
				// m_currentEvent = new TopicEvent (Integer.parseInt
				// (attributes.getValue ("id")), Integer.parseInt
				// (attributes.getValue ("timestamp")));
				
				// TODO: doesn't look like we get a unique id, but we do get a timestamp
				m_currentEvent = new TopicEvent (0, Long.parseLong(attributes.getValue ("timestamp")));
			}
			else if (m_inEvent)
			{
				m_inAttribute = true;
				m_currentName = qName;
				m_currentValue = new StringBuffer ();
			}
		}

		@Override
		public void characters (char ch[], int start, int length) throws SAXException
		{
			if (m_inAttribute)
				m_currentValue.append (ch, start, length);
		}

		@Override
		public void endElement (String uri, String localName, String qName) throws SAXException
		{
			if (m_inAttribute)
			{
				m_inAttribute = false;
				m_currentEvent.addAttribute (m_currentName, m_currentValue.toString ());
			}
			else if (qName.equals ("event"))
			{
				m_inEvent = false;
				m_events.add (m_currentEvent);
			}
		}

		/** @return The parsed events as an array of TopicEvent objects. */
		TopicEvent[] getTopicEvents ()
		{
			return m_events.toArray (new TopicEvent[m_events.size ()]);
		}
	}

	/**
	 * Helper method to parse subscribers on a topic serialized in XML returned
	 * from the OSGi Broker.
	 * 
	 * @param is The inputstream to parse the events from.
	 * @return The subscribers in an array of TopicSubscriber objects.
	 * @throws SAXException A fatal parsing exception, the XML may be malformed,
	 *         it is probably an OSGi Broker bug if it's returning malformed
	 *         XML.
	 * @throws IOException If there was a problem reading from the inputstream,
	 *         the stream might have been closed prematurely.
	 * @throws ParserConfigurationException Low level fatal exception.
	 */
	static TopicClient[] parseClients (InputStream is) throws SAXException, IOException, ParserConfigurationException
	{
		SAXTopicClientParser parseHandler = new SAXTopicClientParser ();

		SAXParser parser = SAXParserFactory.newInstance ().newSAXParser ();
		parser.parse (is, parseHandler);

		return parseHandler.getTopicClients ();
	}

	private static class SAXTopicClientParser extends DefaultHandler
	{
		private ArrayList <TopicClient> m_clients;
		private boolean m_inSubscriber;
		private boolean m_inId;
		private boolean m_inType;
		private StringBuffer m_currentClient;
		private StringBuffer m_currentClientType;

		/**
		 * Constructor to create a new instance of a SAX parser to parse
		 * subscribers serialized in XML returned from the OSGi Broker.
		 */
		SAXTopicClientParser ()
		{
			m_clients = new ArrayList <TopicClient> ();
			m_inSubscriber = false;
			m_inId = false;
			m_inType = false;
		}

		@Override
		public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if (qName.equals ("client"))
				m_inSubscriber = true;
			else if (m_inSubscriber)
			{
				if (qName.equals ("clientID"))
				{
					m_inId = true;
					m_currentClient = new StringBuffer ();
				}
				else if (qName.equals ("clientType"))
				{
					m_inType = true;
					m_currentClientType = new StringBuffer ();
				}
			}
		}

		@Override
		public void characters (char ch[], int start, int length) throws SAXException
		{
			if (m_inId)
				m_currentClient.append (ch, start, length);
			else if (m_inType)
				m_currentClientType.append (ch, start, length);
		}

		@Override
		public void endElement (String uri, String localName, String qName) throws SAXException
		{
			if (qName.equals ("clientID"))
				m_inId = false;
			else if (qName.equals ("clientType"))
				m_inType = false;
			else if (qName.equals ("client"))
			{
				m_inSubscriber = false;
				m_clients.add (new TopicClient (m_currentClient.toString (), m_currentClientType.toString ()));
			}
		}

		/**
		 * @return The parsed subscribers as an array of TopicSubscriber
		 *         objects.
		 */
		TopicClient[] getTopicClients ()
		{
			return m_clients.toArray (new TopicClient[m_clients.size ()]);
		}
	}

	/**
	 * Helper method to parse state from a topic serialized in XML returned from
	 * the OSGi Broker.
	 * 
	 * @param is The inputstream to parse the state from.
	 * @return The state in a TopicState object.
	 * @throws SAXException A fatal parsing exception, the XML may be malformed,
	 *         it is probably an OSGi Broker bug if it's returning malformed
	 *         XML.
	 * @throws IOException If there was a problem reading from the inputstream,
	 *         the stream might have been closed prematurely.
	 * @throws ParserConfigurationException Low level fatal exception.
	 */
	static TopicState parseState (InputStream is) throws SAXException, IOException, ParserConfigurationException
	{
		SAXTopicStateParser parseHandler = new SAXTopicStateParser ();

		SAXParser parser = SAXParserFactory.newInstance ().newSAXParser ();
		parser.parse (is, parseHandler);

		return parseHandler.getTopicState ();
	}

	private static class SAXTopicStateParser extends DefaultHandler
	{
		private TopicState m_state;
		private boolean m_inState;
		private boolean m_inStateAttribute;
		private String m_currentStateName;
		private StringBuffer m_currentStateValue;
		private String m_currentType;

		/**
		 * Constructor to create a new instance of a SAX parser to parse
		 * subscribers serialized in XML returned from the OSGi Broker.
		 */
		SAXTopicStateParser ()
		{
			m_inState = false;
			m_inStateAttribute = false;
		}

		@Override
		public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if (qName.equals ("state"))
			{
				m_inState = true;
				m_state = new TopicState (attributes.getValue ("topic"));
			}
			else if (m_inState)
			{
				m_currentStateValue = new StringBuffer ();
				m_inStateAttribute = true;
				m_currentStateName = qName;
				m_currentType = attributes.getValue ("type");
			}
		}

		@Override
		public void characters (char ch[], int start, int length) throws SAXException
		{
			if (m_inStateAttribute)
				m_currentStateValue.append (ch, start, length);
		}

		@Override
		public void endElement (String uri, String localName, String qName) throws SAXException
		{
			if (m_inStateAttribute)
			{
				m_inStateAttribute = false;
				m_state.addAttribute (m_currentStateName, new TopicStateAttribute (m_currentStateName, m_currentStateValue.toString (), m_currentType));
			}
			else if (qName.equals ("state"))
				m_inState = false;
		}

		/**
		 * @return The parsed subscribers as an array of TopicSubscriber
		 *         objects.
		 */
		TopicState getTopicState ()
		{
			return m_state;
		}
	}

	/**
	 * Helper method to parse contents on a topic serialized in XML returned
	 * from the OSGi Broker.
	 * 
	 * @param is The inputstream to parse the contents from.
	 * @return The contents in an array of TopicContent objects.
	 * @throws SAXException A fatal parsing exception, the XML may be malformed,
	 *         it is probably an OSGi Broker bug if it's returning malformed
	 *         XML.
	 * @throws IOException If there was a problem reading from the inputstream,
	 *         the stream might have been closed prematurely.
	 * @throws ParserConfigurationException Low level fatal exception.
	 */
	static TopicContent[] parseContents (InputStream is) throws SAXException, IOException, ParserConfigurationException
	{
		SAXTopicContentParser parseHandler = new SAXTopicContentParser ();

		SAXParser parser = SAXParserFactory.newInstance ().newSAXParser ();
		parser.parse (is, parseHandler);

		return parseHandler.getTopicContents ();
	}

	private static class SAXTopicContentParser extends DefaultHandler
	{
		private ArrayList <TopicContent> m_contents;
		private boolean m_inContent;
		private boolean m_inFileName;
		private boolean m_inFileSize;
		private boolean m_inFileContentType;
		private boolean m_inClientId;
		private boolean m_inTopic;
		private boolean m_inUrl;
		private String m_currentFileName;
		private String m_currentFileSize;
		private String m_currentFileContentType;
		private String m_currentClientId;
		private String m_currentTopic;
		private String m_currentUrl;

		/**
		 * Constructor to create a new instance of a SAX parser to parse
		 * subscribers serialized in XML returned from the OSGi Broker.
		 */
		SAXTopicContentParser ()
		{
			m_contents = new ArrayList <TopicContent> ();
			m_inContent = false;
			m_inFileName = false;
			m_inFileSize = false;
			m_inFileContentType = false;
			m_inClientId = false;
			m_inTopic = false;
			m_inUrl = false;
		}

		@Override
		public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if (qName.equals ("content"))
				m_inContent = true;
			else if (m_inContent)
			{
				if (qName.equals ("fileName"))
					m_inFileName = true;
				else if (qName.equals ("fileSize"))
					m_inFileSize = true;
				else if (qName.equals ("fileContentType"))
					m_inFileContentType = true;
				else if (qName.equals ("clientID"))
					m_inClientId = true;
				else if (qName.equals ("topic"))
					m_inTopic = true;
				else if (qName.equals ("url"))
					m_inUrl = true;
			}
		}

		@Override
		public void characters (char ch[], int start, int length) throws SAXException
		{
			String value = new String (ch, start, length);

			if (m_inFileName)
				m_currentFileName = value;
			else if (m_inFileSize)
				m_currentFileSize = value;
			else if (m_inFileContentType)
				m_currentFileContentType = value;
			else if (m_inClientId)
				m_currentClientId = value;
			else if (m_inTopic)
				m_currentTopic = value;
			else if (m_inUrl)
				m_currentUrl = value;
		}

		@Override
		public void endElement (String uri, String localName, String qName) throws SAXException
		{
			if (qName.equals ("fileName"))
				m_inFileName = false;
			else if (qName.equals ("fileSize"))
				m_inFileSize = false;
			else if (qName.equals ("fileContentType"))
				m_inFileContentType = false;
			else if (qName.equals ("clientID"))
				m_inClientId = false;
			else if (qName.equals ("topic"))
				m_inTopic = false;
			else if (qName.equals ("url"))
				m_inUrl = false;
			else if (qName.equals ("content"))
			{
				m_inContent = false;
				m_contents.add (new TopicContent (m_currentFileName, m_currentFileSize, m_currentFileContentType, m_currentClientId, m_currentTopic, m_currentUrl));
			}
		}

		/**
		 * @return The parsed subscribers as an array of TopicSubscriber
		 *         objects.
		 */
		TopicContent[] getTopicContents ()
		{
			return m_contents.toArray (new TopicContent[m_contents.size ()]);
		}
	}
}
