package ca.ubc.magic.broker.subscriber.service.ws;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

public class SendService {

	public static String sendHttpPost (String url, String queryString)
	   throws Exception
	{
	    String result = null;
	    
	    try {
	        HttpClient client = new HttpClient();
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
		
		HttpClient httpClient = new HttpClient();
		PostMethod post = new PostMethod(url);
		
		ByteArrayInputStream bs = new ByteArrayInputStream(((String) bodyContent).getBytes()); 
		post.setRequestBody(bs);
		
		httpClient.executeMethod(post);
		results = post.getResponseBodyAsString();
		post.releaseConnection();
		
		return results;
		
	}
}
