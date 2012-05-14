package ca.ubc.magic.broker.http;

/*
 * Copyright (c) 2003, KNOPFLERFISH project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the KNOPFLERFISH project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.HttpContext;

import ca.ubc.magic.broker.api.BrokerException;

/**
 * <p>
 * Wrapper class which listens for all framework services of
 * class HttpServlet or HttpContext. Each such service is picked
 * up and installed into actual running HttpServices
 * </p>
 * <p>
 * <p>
 * The alias used for the servlet/resource is taken from the 
 * PROP_ALIAS service property.
 * </p>
 * <p>
 * The resource dir used for contexts is taken from the 
 * PROP_DIR service property.
 * </p>
 */
public class HttpWrapper {
	
	private static final Logger logger = Logger.getLogger(HttpWrapper.class);

  /**
   * Service property name for HttpServlet and HttpContexts alias
   *
   * <p>
   * Value is <tt>httpwrapper.resource.alias</tt>
   * </p>
   */
  public static String PROP_ALIAS = "httpwrapper.resource.alias";

  /**
   * Service property name for HttpContext resource dir
   *
   * <p>
   * Value is <tt>httpwrapper.resource.dir</tt>
   * </p>
   */
  public static String PROP_DIR   = "httpwrapper.resource.dir";
  
  private static String PORT  = "org.osgi.service.http.port";
  private static String SECURE_PORT = "org.osgi.service.http.port.secure";
  
  public static int HTTP_PORT;
  public static int HTTP_SECURE_PORT;

  BundleContext  bc;

  ExtendedHttpServlet servlet;
  HttpContext context;
  List<ExtendedHttpServlet> servletList;

  public HttpWrapper(BundleContext _bc, HttpContext _context, List<ExtendedHttpServlet> _servletList) {
    this.bc = _bc;
    this.context = _context;
    this.servletList = _servletList;
  }
  
  private ServiceListener getHTTPService(){
		ServiceListener sl = new ServiceListener() {
		    	public void serviceChanged(ServiceEvent ev) {
		    	  ServiceReference sr = ev.getServiceReference();
		    	  switch(ev.getType()) {
		    	  case ServiceEvent.REGISTERED:
		    	    {
		      	    try {
		      	    	
		              register(sr);
		              HTTP_PORT = Integer.parseInt((String) sr.getProperty(PORT));
		        	  HTTP_SECURE_PORT = Integer.parseInt((String) sr.getProperty(SECURE_PORT));
		        	  
		        	  logger.debug("Http Port set to: " + HTTP_PORT);
		        	  logger.debug("Http Secure Port set to: " + HTTP_SECURE_PORT);
		        	  
		            } catch (Exception e) {
		              e.printStackTrace();
		            }
		          }
		        }
		      }
		};
	    return sl;
  }
  
  public void open() {
    
	ServiceListener sl = getHTTPService();
	
    String filter = "(objectclass=" + HttpService.class.getName() + ")";
    
    try {
      bc.addServiceListener(sl, filter);
      ServiceReference[] srl = bc.getServiceReferences(null, filter);
      
      for(int i = 0; srl != null && i < srl.length; i++) {
        register(srl[i]);
        
        HTTP_PORT = Integer.parseInt((String) srl[0].getProperty(PORT));
  	    HTTP_SECURE_PORT = Integer.parseInt((String) srl[0].getProperty(SECURE_PORT));
  	  
  	    logger.debug("Http Port set to: " + HTTP_PORT);
  	    logger.debug("Http Secure Port set to: " + HTTP_SECURE_PORT);
        
      }
    } catch (InvalidSyntaxException e) { 
      e.printStackTrace(); 
    }
  }
  
  public void close(){
	  ServiceListener sl = getHTTPService();
		
	    String filter = "(objectclass=" + HttpService.class.getName() + ")";
	    
	    try {
	      bc.addServiceListener(sl, filter);
	      ServiceReference[] srl = bc.getServiceReferences(null, filter);
	      for(int i = 0; srl != null && i < srl.length; i++) {
	        unregister(srl[i]);
	      }
	    } catch (InvalidSyntaxException e) { 
	      e.printStackTrace(); 
	    }
  }
  
  public void register(ServiceReference sr) {
    HttpService http = (HttpService) bc.getService(sr);
    
    if (http == null) {
    	System.out.println("http resource is null");
      return;
    }
    
    try {
      Hashtable props = new Hashtable();
      logger.debug("Servlet is being initialized");
      
      logger.debug ("Servlet Res Alias: " + ExtendedHttpServlet.RESOURCE_ALIAS);
      logger.debug ("Servlet Res Dir: " + ExtendedHttpServlet.RESOURCE_DIR);
      http.registerResources(ExtendedHttpServlet.RESOURCE_ALIAS,ExtendedHttpServlet.RESOURCE_DIR, context);
      
      for (ExtendedHttpServlet servlet : servletList){
    	  servlet.init();
	      logger.debug ("Servlet Alias: " + servlet.getServletAlias());
	      http.registerServlet(servlet.getServletAlias(), servlet, props, null);
      }
      
    } catch (Exception e) {
    	e.printStackTrace();
    	System.out.println("Failed to register resource");
    }
  }
  
  public void unregister(ServiceReference sr){
	  HttpService http = (HttpService) bc.getService(sr);
	  if (http == null) {
	    	System.out.println("http resource is null");
	      return;
	  }
	  
	  try{
		  http.unregister(ExtendedHttpServlet.RESOURCE_ALIAS);
		  for (ExtendedHttpServlet servlet : servletList){
			  try{
				  http.unregister(servlet.getServletAlias());
				  servlet.destroy();
			  }catch (Exception e){
				  logger.warn("Failed to unregister servlet:" + servlet.getServletAlias());
			  }
		  }
	  }catch (Exception e){
		  System.out.println("Failed to unregister resource");
	  }
  }

}

