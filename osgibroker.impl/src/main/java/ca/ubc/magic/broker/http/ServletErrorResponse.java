package ca.ubc.magic.broker.http;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.BrokerException;

/**
 * The class is used to provide REST based error messages over HTTP to the clients based on the failures
 * appear in dealing with the DB.
 * 
 * @author nima
 *
 */

public class ServletErrorResponse {
	
	private static final Logger logger = Logger.getLogger( ServletErrorResponse.class );
	
	/**
	 * sends the message in exception e to the requesting client in the response message  
	 * 
	 * @param response	the response to be sent to the client
	 * @param e			the error message to be reported to the client
	 */
	public static void doSendError (HttpServletResponse response, Exception e) {
		
		try{
			response.setHeader("Cache-Control", "no-cache");
			if (e instanceof BrokerException)
				response.sendError(((BrokerException)e).getStatus(), e.getMessage() );
			
			else
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			
		}catch (Exception ex){
			logger.debug("Error in sending error messages to the subscriber server");
			logger.debug(ex.getMessage());
			ex.printStackTrace();
		}
	}

}
