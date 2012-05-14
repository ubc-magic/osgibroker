package ca.ubc.magic.broker.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;

public class TestHttpContext implements HttpContext {

	public String getMimeType(String reqEntry) {
	    return null; // server decides type
	  }
	
	  public boolean handleSecurity( HttpServletRequest request,
	                                 HttpServletResponse response )
	    throws IOException
	  {
	    return true;
	  }
	  
	  public URL getResource(String name) {
		try {
			return new URL("file:" + "_content/" + name);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	  }
}
