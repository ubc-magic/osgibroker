package ca.ubc.magic.broker.subscriber.service.servlet;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

public class SendService {
	
	// The timeout time for the httpclinet to receive a response from the 
	// client posting the message to.
	private static final int TIME_OUT = 5000;

	public static String sendHttpPost (String url, String queryString)
	   throws Exception
	{
	    String result = null;
	    
	    try {
	        HttpClient client = new HttpClient();
	        
	        // sets the timeout for the client request to be 5 seconds
			// and modifies it for the client parameters
			HttpClientParams params = new HttpClientParams();
			params.setConnectionManagerTimeout(SendService.TIME_OUT);
			
	        PostMethod post = new PostMethod(url);
	        post.setQueryString(queryString);
	        client.executeMethod(post);
	        
	        result = post.getResponseBodyAsString();
	        post.releaseConnection();
	    }
	    catch (Exception e) {
	        throw new Exception("post failed", e);
	    }
	    
	    return result;
	}
	
	public static String sendHttpGet(String url, String queryString) throws Exception {
		
		String result = null;
		
		try{
			
			HttpClient httpClient = new HttpClient();
			
			// sets the timeout for the client request to be 5 seconds
			// and modifies it for the client parameters
			HttpClientParams params = new HttpClientParams();
			params.setConnectionManagerTimeout(SendService.TIME_OUT);
			
			GetMethod get = new GetMethod(url);
			get.setQueryString(queryString);
			httpClient.executeMethod(get);
			
			Header[] headers = get.getResponseHeaders();
			get.releaseConnection();
			
			System.err.println(headers.toString());
			
		}catch (Exception e) {
	        throw new Exception("get failed", e);
	    }
		return result;
	}
	
	public static String sendHttpPostBody(String url, Serializable bodyContent) throws Exception {
		
		String results = null;
		
		// creates the connection
		HttpClient httpClient = new HttpClient();
		
		// sets the timeout for the client request to be 5 seconds
		// and modifies it for the client parameters
		HttpClientParams params = new HttpClientParams();
		params.setConnectionManagerTimeout(SendService.TIME_OUT);
		httpClient.setParams(params);
		
		// creates the post method
		PostMethod post = new PostMethod(url);
		
		ByteArrayInputStream bs = new ByteArrayInputStream(((String) bodyContent).getBytes()); 
		post.setRequestBody(bs);
		
		// executes the post method
		httpClient.executeMethod(post);
		results = post.getResponseBodyAsString();
		post.releaseConnection();
		
		return results;
		
	}
	
	public static void main(String[] args){
		
		int i = 1000;
		
		while (!Thread.currentThread().isInterrupted()){
			
			try {
				
				SendService.sendHttpPost("http://localhost:8080/osgibroker/event", "topic=test&name=nima&counter="+String.valueOf(i));
				
				i += 12;
				
				Thread.currentThread().sleep(10000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
