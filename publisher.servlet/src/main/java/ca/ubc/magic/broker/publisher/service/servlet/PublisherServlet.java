package ca.ubc.magic.broker.publisher.service.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.api.ds.Event;
import ca.ubc.magic.broker.api.PublisherIF;
import ca.ubc.magic.broker.api.SubscriberIF;
import ca.ubc.magic.broker.http.ExtendedHttpServlet;

/**
 * The PublisherServlet class is a class that uses PublisherImpl in order to
 * publish events to the subscribers. The messages are received as HTTP variables
 * and are then propagated to the receivers of the message as Event objects
 * 
 * @author nima kaviani
 *
 */

public class PublisherServlet extends ExtendedHttpServlet {
		
	private static final long serialVersionUID = 1L;
	
	public final String  RES_DIR       = "/www";
	public final String  SERVLET_ALIAS = "/servlet/publisher";
	public final String  RES_ALIAS     = "/publisher/resources";
	
	private Event event;		
	
	private PublisherIF   publisherService = null;
	
	public PublisherServlet(PublisherIF publisher){
		super();
		this.publisherService = publisher;	
		this.setResDir(RES_DIR);
		this.setResAlias(RES_ALIAS);
		this.setServletAlias(SERVLET_ALIAS);
	}	
	
	/**
	 * receives the request from the servlet and parses the variables into 
	 * Event messages that are then delivered to the publisher by calling the
	 * deliver method
	 */
	public synchronized void doGet(HttpServletRequest request, HttpServletResponse response) throws 
							IOException, ServletException{
		
		if (request.getParameter(SubscriberIF.TOPIC) == null)
			return;
		
		event = new Event();
		
		Enumeration enumeration = request.getParameterNames();
		while (enumeration.hasMoreElements()){
			String key = StringEscapeUtils.escapeHtml((String) enumeration.nextElement());
			event.addAttribute(key, StringEscapeUtils.escapeHtml((String) request.getParameter(key)));	
		}
		
		try {
			deliver(event, (String)request.getParameter(SubscriberIF.TOPIC));
		} catch (BrokerException e) {
			response.getWriter().write(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		PrintWriter out = response.getWriter();
		out.print("<html><body>");		
		out.print(event.toString());
		out.print("</body></html>");
	}
	
	public synchronized void doPost(HttpServletRequest request, HttpServletResponse response) throws 
									IOException, ServletException {		
		doGet(request, response);
		
	}
	
	/**
	 * Forwards the message with the corresponding topic to the publisherService
	 * to be broadcasted to all the  
	 * 
	 * @param message
	 * @param topic
	 * @throws Exception 
	 */
	private void deliver(Serializable message, String topic) throws Exception{		
		publisherService.deliver(message, topic);
	}	
}
