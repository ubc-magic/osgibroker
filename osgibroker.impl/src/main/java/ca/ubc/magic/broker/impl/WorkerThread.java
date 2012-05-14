package ca.ubc.magic.broker.impl;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import ca.ubc.magic.broker.api.RemoteClientIF;
import ca.ubc.magic.broker.api.RemoteSubscriberIF;
import ca.ubc.magic.broker.api.ds.Client;

/**
 * The executable class to take care of remote execution of the incoming clients
 * through managing their response to the system
 * 
 * @author nima
 *
 */
class WorkerThread implements Runnable {
	
	private Object __client;
	private Serializable   __arg1;
	private String __arg2;
	
	private static final String DELIVER_METHOD = "deliver";
	private static final Logger logger = Logger.getLogger( WorkerThread.class );
	
	WorkerThread (Object client, Serializable msg){
		__client = client;
		__arg1    = msg;
	}
	
	WorkerThread (Object client, Serializable msg, String topic){
		__client = client;
		__arg1   = msg;
		__arg2   = topic;
	}
	
	public void run() {
		
		try{
			if (__client instanceof RemoteClientIF)
				((RemoteClientIF) __client).deliver(__arg1);
			else if (__client instanceof RemoteSubscriberIF)
				((RemoteSubscriberIF) __client).deliver(__arg1, __arg2);
			
		}catch(Throwable e){
			logger.error(e.getMessage());
			System.out.println(e.getMessage());
		}
	}
}