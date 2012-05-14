package ca.ubc.magic.broker.storage.helper;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Manages SQL expressions to manipulate the database. All the statements for the database are stored in the mysql.xml
 * file located at /src/main/resources/sql/mysql/mysql.xml. The statements contain all the required sql statements
 * to modify the content for all the tables available in the OSGiBroker database. 
 * 
 * To support other database servers, e.g., Postgres SQL, a similar statement file can be stored in the
 * /src/main/resources/sql/pgsql/pgsql.xml and the DBManager implementation for Postgres creates an object of 
 * statement manager with the path to this XML file sent to it.
 * 
 * @author nima
 *
 */

public class StatementManager {
	
	private Document xmlDocument = null;
	private XPath xpath = null;
	private XPathExpression expr = null;
	
	private NodeList topicStoreNodes = null;
	private NodeList clientStoreNodes = null;
	private NodeList subscriberStoreNodes = null;
	private NodeList eventStoreNodes = null;
	private NodeList stateStoreNodes = null;
	private NodeList contentStoreNodes = null;
	
	/**
	 * defines the path to the SQL statements and initializes the Statement manager
	 * 
	 * @param _statementsPath	Path to the statemenet manager XML file
	 * @throws Exception		thrown if there is any problem with parsing the file or reading the file, etc.
	 */
	public StatementManager(String _statementsPath) throws Exception{
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); 
	    DocumentBuilder builder = domFactory.newDocumentBuilder();
	    InputStream is =
            this.getClass().getClassLoader().getResourceAsStream( _statementsPath );
	    xmlDocument = builder.parse(is);
	    xpath = XPathFactory.newInstance().newXPath();
	    
	    expr = xpath.compile("//topic-store/*");
	    topicStoreNodes = (NodeList) expr.evaluate(xmlDocument, XPathConstants.NODESET);
	    
	    expr = xpath.compile("//client-store/*");
	    clientStoreNodes = (NodeList) expr.evaluate(xmlDocument, XPathConstants.NODESET);
	    
	    expr = xpath.compile("//subscriber-store/*");
	    subscriberStoreNodes = (NodeList) expr.evaluate(xmlDocument, XPathConstants.NODESET);
	    
	    expr = xpath.compile("//eventlog-store/*");
	    eventStoreNodes = (NodeList) expr.evaluate(xmlDocument, XPathConstants.NODESET);
	    
	    expr = xpath.compile("//state-store/*");
	    stateStoreNodes = (NodeList) expr.evaluate(xmlDocument, XPathConstants.NODESET);
	    
	    expr = xpath.compile("//content-store/*");
	    contentStoreNodes = (NodeList) expr.evaluate(xmlDocument, XPathConstants.NODESET);
	}
	
	/**
	 * retrieves the statement for TOPICs
	 * 
	 * @param stmt							The XML tag for the statement to be retrieved
	 * @return								The string for the sql statement
	 * @throws XPathExpressionException		Thrown if there is any problem with parsing or reading the file
	 */
	public synchronized String getTopicStoreStmt(String stmt) throws XPathExpressionException{
		
		for (int i = 0; i < topicStoreNodes.getLength(); i++) {
		     if(topicStoreNodes.item(i).getNodeName().equals(stmt))
		    	 return StringEscapeUtils.unescapeHtml(topicStoreNodes.item(i).getFirstChild().getNodeValue());
	    }
		return null;
	}
	
	/**
	 * retrieves the statement for Clients
	 * 
	 * @param stmt							The XML tag for the statement to be retrieved
	 * @return								The string for the sql statement
	 * @throws XPathExpressionException		Thrown if there is any problem with parsing or reading the file
	 */
	public synchronized String getClientStoreStmt(String stmt) throws XPathExpressionException {
		for (int i = 0; i < clientStoreNodes.getLength(); i++) {
		     if(clientStoreNodes.item(i).getNodeName().equals(stmt))
		    	 return StringEscapeUtils.unescapeHtml(clientStoreNodes.item(i).getFirstChild().getNodeValue());
	    }
		return null;		
	}
	
	/**
	 * retrieves the statement for Subscribers
	 * 
	 * @param stmt							The XML tag for the statement to be retrieved
	 * @return								The string for the sql statement
	 * @throws XPathExpressionException		Thrown if there is any problem with parsing or reading the file
	 */
	public synchronized String getSubscriberStoreStmt(String stmt) throws XPathExpressionException {
		for (int i = 0; i < subscriberStoreNodes.getLength(); i++) {
		     if(subscriberStoreNodes.item(i).getNodeName().equals(stmt))
		    	 return StringEscapeUtils.unescapeHtml(subscriberStoreNodes.item(i).getFirstChild().getNodeValue());
	    }
		return null;
	}
	
	/**
	 * retrieves the statement for Events
	 * 
	 * @param stmt							The XML tag for the statement to be retrieved
	 * @return								The string for the sql statement
	 * @throws XPathExpressionException		Thrown if there is any problem with parsing or reading the file
	 */
	public synchronized String getEventStoreStmt(String stmt) throws XPathExpressionException {
		for (int i = 0; i < eventStoreNodes.getLength(); i++) {
		     if(eventStoreNodes.item(i).getNodeName().equals(stmt))
		    	 return StringEscapeUtils.unescapeHtml(eventStoreNodes.item(i).getFirstChild().getNodeValue());
	    }
		return null;
	}
	
	/**
	 * retrieves the states of a topic
	 * 
	 * @param stmt							The XML tag for the statement to be retrieved
	 * @return								The string for the sql statement
	 * @throws XPathExpressionException		Thrown if there is any problem with parsing or reading the file
	 */
	public synchronized String getStateStoreStmt(String stmt) throws XPathExpressionException {
		for (int i = 0; i < stateStoreNodes.getLength(); i++) {
		     if(stateStoreNodes.item(i).getNodeName().equals(stmt))
		    	 return StringEscapeUtils.unescapeHtml(stateStoreNodes.item(i).getFirstChild().getNodeValue());
	    }
		return null;
	}
	
	/**
	 * retrieves the content sql statements for a topic
	 * 
	 * @param stmt							The XML tag for the content to be retrieved
	 * @return								The string for the sql statementst
	 * @throws XPathExpressionException		Thrown if there is any problem with parsing or reading the file
	 */
	public synchronized String getContentStoreStmt(String stmt) throws XPathExpressionException {
		for (int i = 0; i < contentStoreNodes.getLength(); i++) {
		     if(contentStoreNodes.item(i).getNodeName().equals(stmt))
		    	 return StringEscapeUtils.unescapeHtml(contentStoreNodes.item(i).getFirstChild().getNodeValue());
	    }
		return null;
	}
	
}
