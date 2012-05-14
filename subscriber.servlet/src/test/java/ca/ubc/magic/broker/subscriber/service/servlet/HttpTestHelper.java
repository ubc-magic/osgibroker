package ca.ubc.magic.broker.subscriber.service.servlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpStatus;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;
import org.xml.sax.SAXException;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.subscriber.service.servlet.helper.model.MethodGroupHelper;
import ca.ubc.magic.broker.subscriber.service.servlet.helper.model.TopicEvent;

public class HttpTestHelper {
	
	static HttpTester req = new HttpTester();
	static HttpTester res = new HttpTester();
	
	public static HttpTester subscribe(ServletTester tester, String baseURL, String paramList) throws IOException, Exception{
		
		req.setMethod("GET");
    	req.setHeader("Host", "tester");
    	req.setURI(baseURL+"/subscribe?" + paramList);
    	
    	res.parse(tester.getResponses(req.generate()));
    	if (res.getStatus() != HttpStatus.SC_OK)
    		throw new BrokerException(res.getStatus(), "Error Subscribing: " + res.getReason());
    	
    	return res;
	}
	
	public static HttpTester unsubscribe(ServletTester tester, String baseURL, String paramList) throws IOException, Exception{
		
		req.setMethod("GET");
    	req.setHeader("Host", "tester");
    	req.setURI(baseURL+"/unsubscribe?" + paramList);
    	
    	res.parse(tester.getResponses(req.generate()));
    	if (res.getStatus() != HttpStatus.SC_OK)
    		throw new BrokerException(res.getStatus(), "Error Unsubscribing: " + res.getReason());
    	
    	return res;
	}
	
	public static HttpTester postEvent(ServletTester tester, String baseURL, String paramList) throws IOException, Exception{
		
		req.setMethod("POST");
    	req.setHeader("Host", "tester");
    	req.setURI(baseURL+"/event?" + paramList);
    	
    	res.parse(tester.getResponses(req.generate()));
    	if (res.getStatus() != HttpStatus.SC_OK)
    		throw new BrokerException(res.getStatus(), "Error Sending Event: " + res.getReason());
    	
    	return res;
	}
	
	public static HttpTester queryEvent(ServletTester tester, String baseURL, String paramList) throws IOException, Exception {
		req.setMethod("GET");
		req.setHeader("Host", "tester");
		req.setURI(baseURL+"/topic/events?"+paramList);
		
		res.parse(tester.getResponses(req.generate()));
		if (res.getStatus() != HttpStatus.SC_OK)
    		throw new BrokerException(res.getStatus(), "Error Sending Event: " + res.getReason());
    	
    	return res;
	}
	
	public static TopicEvent[] parseEvent(String content) throws SAXException, IOException, ParserConfigurationException{
		
		return MethodGroupHelper.parseEvents(new ByteArrayInputStream(content.getBytes()));
	}
}
