package ca.ubc.magic.broker.publisher.service.sms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import jline.ConsoleReader;
import jline.Terminal;
import jline.CursorBuffer;
import jline.WindowsTerminal;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.smslib.GatewayException;
import org.smslib.SMSLibException;
import org.smslib.Service;
import org.smslib.TimeoutException;
import org.smslib.helper.CommPortIdentifier;
import org.smslib.modem.SerialModemGateway;

/**
 * The PortScan class receives the id, the BaudRate, the Manufacturer and the Model for the  modem and
 * inspects all serial ports on the local machine in order to detect whether the port is open or not.
 * In case the port is found for the sms modem, the modem is configured to work with the broker and 
 * gets hooked to the publisher of the broker. In case it can not detect any port, it just throws an
 * exception and informs the user.
 * 
 * @author nima
 *
 */
@SuppressWarnings("unused")
public class PortScan implements Callable<String> {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	private String comport;
	private String id;
	private int baudRate;
	private String manufacturer;
	private String model;
	
	private Terminal terminal;
	private ConsoleReader reader;
	
	/**
	 * @param id			the id for the modem gateway
	 * @param baudRate		the baudrate for the gateway
	 * @param manufacturer	the manufacturer
	 * @param model			the model for the gateway
	 */
	public PortScan(String id, int baudRate, String manufacturer, String model){
		this.id = id;
		this.baudRate = baudRate;
		this.manufacturer = manufacturer;
		this.model = model;
		
		try {
		        terminal = Terminal.setupTerminal();
		        terminal.enableEcho();
		        reader = new ConsoleReader();
		        terminal.beforeReadLine(reader, "", (char)0);
		} catch (Exception e) {
		        e.printStackTrace();
		        terminal = null;
		}
	}

	/**
	 * The PortScan is implemented as a Callable class in order to facilitate threading and separation of scanning
	 * from other necessary tasks.
	 * 
	 */
	public String call() {
		int portNum = 0;
		int portsVisited = 0;
		Enumeration<CommPortIdentifier> portId = CommPortIdentifier.getPortIdentifiers();
		
		List<String> portList = new ArrayList<String>();
		
		CommPortIdentifier pi = null;
		while(portId.hasMoreElements()){
			
			pi = portId.nextElement();
			
			if (!portList.contains(pi.getName())){
				portList.add(pi.getName());
				portNum++;
			}
				
		}
		
		portId   = CommPortIdentifier.getPortIdentifiers();
		portList = new ArrayList<String>();
		while (portId.hasMoreElements()) {
			
			pi = (CommPortIdentifier) portId.nextElement();

			if (!portList.contains(pi.getName())){
				portsVisited++;
				portList.add(pi.getName());
				setProgress(portsVisited, portNum, pi.getName());
			}else
				continue;
			
			if (SMSManagedService.getInstance().getModemInfo().getSkipPortList().contains(pi.getName())){
				logger.info(pi.getName() + " is in the SkipList ... skipping " + pi.getName());
				continue;
			}
			
			if (portsVisited < portNum)
				setProgress(portsVisited, portNum, pi.getName());
			logger.info(pi.getName() + " is being scanned...");

			Service tempServ = new Service();
			SerialModemGateway gateway = new SerialModemGateway(id, pi
					.getName(), baudRate, manufacturer, model);

			logger.info(pi.getName() + " " + gateway.toString());

			try {
				tempServ.addGateway(gateway);
				tempServ.startService();
				
				comport = pi.getName();
				logger.debug(pi.getName() + " successfully added!");	
				
				tempServ.stopService();
				tempServ.removeGateway(gateway);

			} catch (TimeoutException e1) {
				logger.error(e1.getMessage());
			} catch (GatewayException e1) {
				logger.error(e1.getMessage());
			} catch (SMSLibException e1) {
				logger.error(e1.getMessage());
			} catch (IOException e1) {
				logger.error(e1.getMessage());
			} catch (InterruptedException e1) {
				logger.error(e1.getMessage());
			}
			
		}
		
		return comport;
	}
	
	/**
	 * A commandline progress line implemented using JLine 0.9.9.94
	 * 
	 * @param completed		The number or percentage passed the task
	 * @param total			The total number or percentage for a task 
	 * @param comport		The comport being scanned at the moment
	 */
	private void setProgress(int completed, int total, String comport) {
           if (terminal == null)
                   return;
           int w = reader.getTermwidth();
           int progress = (completed * 20) / total;
           String totalStr = String.valueOf(total);
           String percent = String.format("%0"+totalStr.length()+"d/%s [", completed, totalStr);
           String result = percent + StringUtils.repeat("=", progress)+ StringUtils.repeat(" ", 20 - progress) + "]" + " Scanning " + comport + "...";
           
           try {
                   reader.getCursorBuffer().clearBuffer();
                   reader.getCursorBuffer().write(result);
                   reader.setCursorPosition(w);
            	   reader.redrawLine();
            	   if (completed == total)
            		   reader.printNewline();
           }
           catch (IOException e) {
                   logger.error(e);
           }
	}
	
	public static void main(String args[]){
		PortScan portScan = new PortScan("default.samba75.modem", 230400, "Falcom", "Samba75");
		FutureTask<String> future = new FutureTask<String>(portScan);
		ExecutorService es = Executors.newSingleThreadExecutor();
		es.submit(future);
		
		try {
			System.out.println(future.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
	}
}
