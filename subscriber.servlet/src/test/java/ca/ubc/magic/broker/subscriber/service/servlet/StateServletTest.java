package ca.ubc.magic.broker.subscriber.service.servlet;

import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class StateServletTest extends TestCase {
	
	private static ServletTester tester;
    private static String baseUrl;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		tester = new ServletTester();
        tester.setContextPath("/");
        tester.addServlet(StateServlet.class, "/state");
        
        baseUrl = tester.createSocketConnector(true);
        tester.start();
		
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		tester.stop();
	}
	
	public StateServletTest(String name){
		super(name);
	}
	
	public HttpTester addStateServletTest() throws Exception {
		
		HttpTester req = new HttpTester();
    	HttpTester res = new HttpTester();
    	
    	req.setMethod("POST");
    	req.setHeader("HOST", "tester");
    	req.setURI("/state?topic=test&_method=PUT&name=haji&family=baji&class=tv");
    	
    	res.parse(tester.getResponses(req.generate()));
    	
    	return res;
	}
	
	public HttpTester deleteStateServletTest() throws Exception {
		
		HttpTester req = new HttpTester();
    	HttpTester res = new HttpTester();
    	
    	req.setMethod("POST");
    	req.setHeader("HOST", "tester");
    	req.setURI("/state?topic=test&_method=DELETE&stateID=class");
    	
    	res.parse(tester.getResponses(req.generate()));
    	
    	return res;
	}
	
	public HttpTester getStateServletTest() throws Exception {
		
		HttpTester req = new HttpTester();
    	HttpTester res = new HttpTester();
    	
    	req.setMethod("GET");
    	req.setHeader("HOST", "tester");
    	req.setURI("/state?topic=test");
    	
    	res.parse(tester.getResponses(req.generate()));
    	System.err.println(res.getContent());
    	
    	return res;
		
	}
	
	public static Test suite(){
		TestSuite suite = new TestSuite();
		suite.addTest(new StateServletTest("addStateServletTest"));
		suite.addTest(new StateServletTest("deleteStateServletTest"));
		return suite;
	}
}
