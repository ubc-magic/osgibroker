package ca.ubc.magic.broker.subscriber.service.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class FakeServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
			
			response.getWriter().write("\n\n ******************** \n");
			response.getWriter().write("This is the start of message from the servlet\n");
			
			while(reader.ready())
				response.getWriter().write(reader.readLine());
			
			response.getWriter().write("\n This is the end of message from the servlet");
			response.getWriter().write("\n ******************** \n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
