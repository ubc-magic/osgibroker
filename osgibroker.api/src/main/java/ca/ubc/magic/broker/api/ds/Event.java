package ca.ubc.magic.broker.api.ds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An event in the system. See
 * 
 * http://java.sun.com/j2ee/sdk_1.3/techdocs/api/javax/jms/Message.html
 * 
 * for a similar thing (JMS Message)
 * 
 * @author Mike
 * 
 */
public class Event implements Serializable {

	static final int XML = 0;
	static final int JSON = 1;
	
	public static final String CLIENT_EVENT_TIMESTAMP = "timestamp"; 

	
	transient private int serialFormat = XML;
	
	private long timestamp = -1;

	private HashMap eventData = new HashMap();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public class EventSAXHandler extends DefaultHandler {

		String currentElement;

		String currentValue;

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			currentElement = localName;
		}

		public void characters(char ch[], int start, int length)
				throws SAXException {
			currentValue = new String(ch, start, length);
		}

		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			eventData.put(currentElement, currentValue);
		}
	}

	public Event() {
		super();
		timestamp = System.currentTimeMillis();
		// TODO Auto-generated constructor stub
	}
	
	public Event(Map<String, String> params){
		this();
		for (String name : params.keySet())
			addAttribute(name, params.get(name));
	}

	public void addAttribute(String name, String value) {
		eventData.put(name, value);
	}

	public void removeAttribute(String name) {
		eventData.remove(name);
	}

	public String getAttribute(String name) {
		return (String) eventData.get(name);
	}
	
	public boolean hasAttribute(String name) {
		return eventData.containsKey(name);
		
	}

	public String[] getAttributeNames() {
		String[] names = new String[eventData.size()];
		return (String[]) eventData.keySet().toArray(names);
	}
	
	public void setTimeStamp(Long _timestamp){
		this.timestamp = _timestamp;
	}
	
	public long getTimeStamp(){
		return this.timestamp;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		if (this.serialFormat == XML)
			writeObjectXML(out);
		else
			writeObjectJSON(out);
	}

	private void writeObjectXML(OutputStream outStream)
			throws IOException {
		PrintWriter out = new PrintWriter(outStream);
		out.println("<event timestamp=\""+ Long.toString(timestamp) + "\">");
		String[] attributes = getAttributeNames();
		for (int i = 0; i < attributes.length; i++) {
			out.print("<" + attributes[i] + ">");
			out.print(getAttribute(attributes[i]));
			out.println("</" + attributes[i] + ">");
		}
		out.println("</event>");
		out.flush();
	}
	
	public String toString() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			writeObjectXML(stream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stream.toString();
	}

	private void writeObjectJSON(ObjectOutputStream out) throws IOException {
		// PrintWriter out = new PrintWriter(outStream);
		// out.println("<event>");
		// String[] attributes = getAttributeNames();
		// for (int i=0; i<attributes.length; i++) {
		// out.print("<"+attributes[i]+">");
		// out.print(getAttribute(attributes[i]));
		// out.print("</"+attributes[i]+">");
		// }
		// out.println("</event>");
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		// TODO, read from JSON too!
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			saxParser.parse(in, new EventSAXHandler());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
