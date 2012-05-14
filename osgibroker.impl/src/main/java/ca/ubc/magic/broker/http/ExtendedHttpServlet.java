package ca.ubc.magic.broker.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.SubscriberIF;

/**
 * The class provides an extension to HttpServlet to enable definition of servlet alisa and resolution address 
 * and resolution directory for the servlet to work properly. This is to enable using Knopflerfish implementation of
 * servlet registration to work with PAX Web HTTP server. PAX provides a better implementation which migh be 
 * shifted over if a necessity is realized at a later point during the project 
 * 
 * @author nima
 *
 */

public class ExtendedHttpServlet extends HttpServlet {
	
	protected static final String  EVENTS_QUERY = "events";
	protected static final String  CLIENTS_QUERY = "clients";
	protected static final String  QUERY_SIZE = "querySize";
	
	public static final String  QUERY_START        = "start";
	public static final String  QUERY_BEFORE_TIME  = "beforeT";
	public static final String  QUERY_AFTER_TIME   = "afterT";
	public static final String  QUERY_BEFORE_EVENT = "beforeE";
	public static final String  QUERY_AFTER_EVENT  = "afterE";
	public static final String  QUERY_END          = "end";
	
	public static final String  QUERY_BASE  = "queryBase";
	
	protected static final String  FORMAT       = "format";
	
	public static final String  TIME_OUT      = "timeOut";
	
	protected static final String  METHOD = "_method";
	protected static final String  METHOD_DELETE = "DELETE";
	protected static final String  METHOD_POST = "POST";
	protected static final String  METHOD_PUT = "PUT";

	public static final String RESOURCE_DIR   = "/www";
	public static final String RESOURCE_ALIAS = "/";
	protected String  servletAlias = "";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExtendedHttpServlet() {
		super();
	}
	
	public String getServletAlias(){
		return this.servletAlias;
	}
	
	
	public void setServletAlias(String _servletAlias){
		this.servletAlias = _servletAlias;
	}
}
