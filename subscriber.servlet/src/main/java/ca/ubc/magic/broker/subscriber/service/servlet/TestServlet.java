package ca.ubc.magic.broker.subscriber.service.servlet;

import ca.ubc.magic.broker.http.ExtendedHttpServlet;

public class TestServlet extends ExtendedHttpServlet {
	
private static final long serialVersionUID = 1L;
	
	private final String  SERVLET_ALIAS = "/osgibroker/test";
	
	public void init(){
		
		this.setServletAlias(SERVLET_ALIAS);
	}
}
