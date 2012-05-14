package ca.ubc.magic.broker.publisher.service.sms;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import org.apache.log4j.Logger;
import org.smslib.GatewayException;
import org.smslib.IInboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.SMSLibException;
import org.smslib.Service;
import org.smslib.TimeoutException;
import org.smslib.Message.MessageTypes;
import org.smslib.modem.SerialModemGateway;

import ca.ubc.magic.broker.api.BrokerException;
import ca.ubc.magic.broker.publisher.service.sms.broker.BrokerConnector;

/**
 * The configurator class receives the proper port from the {@link ca.ubc.magic.broker.publisher.service.sms.PortScan}, if
 * available, and thenconfigures the osgibroker.sms module to work with the core of the broker. If the port is not available
 * no configuration happens and the SMSConfigurator prompts the user with a message indicating that port scanning has failed.
 * 
 * 
 * @author nima
 *
 */
public class SMSConfigurator implements Runnable {
	
	private static final Logger logger = Logger.getLogger(SMSConfigurator.class);
			
	private SerialModemGateway modemGateway = null;
	private org.smslib.Service smslibServ = null;
	
	private BrokerConnector brokerConnector;
	private SMSModem modem;
	
	private FutureTask<String> future;
	private ExecutorService es;
	private PortScan portScan;

	public SMSConfigurator(){
		
		brokerConnector = new BrokerConnector();
		modem           = new SMSModem();
		
	}
	
	/**
	 * @return	the reference to the broker connector in order to feed in broker information and deliver messages
	 */
	public BrokerConnector getBrokerConnector(){
		return brokerConnector;
	}
	
	/**
	 * @param _modem	sets the modem for the configurator received from the {@link ca.ubc.magic.broker.publisher.service.sms.SMSManagedService}
	 */
	public void setSMSModem(SMSModem _modem){
		this.modem = _modem;
	}
	
	/**
	 * @return		the modem stored in the SMSConfigurator
	 */
	public SMSModem getSMSModem(){
		return this.modem;
	}

	/**
	 * The heart of the configurator, calling the PortScanner and then configuring the modem, if possible.
	 */
	public void run() {
		
		if (!SMSManagedService.getInstance().getModemInfo().isEmpty())
			this.modem = SMSManagedService.getInstance().getModemInfo();
		else
			return;
		
		System.out.println("Scanning...");

		portScan = new PortScan("default.samba75.modem1", 230400, "Falcom", "Samba75");
		modem.setComPort(portScan.call());
		
		System.out.println("Port Scanning Completed.");
		
		if (modem.getComPort() != null)
			try{
				configureSMSModule();
			}catch(Exception e){
				e.printStackTrace();
			}
		else{
			System.out.println("SMS Modem Configuration Failed. No port detected!");
		}
	}
	
	/**
	 * stops the configurator and its respective SMS services and gateways.
	 */
	public void stop(){
		
		logger.debug("SMS Publisher Polling is deactivated...");
		try {
			
			if (future != null)
				future.cancel(true);
			if (es != null)
				es.shutdown();
			portScan = null;
			
			if(modem.getComPort() != null){
				smslibServ.stopService();
				smslibServ.removeGateway(modemGateway);
			}
			
		}catch (NullPointerException e){
			logger.error(e);
		} catch (GatewayException e) {
			logger.error(e);
		}
		
		modemGateway = null;
		smslibServ = null;
	}
	
	/**
	 * binds the modem to the required port.
	 */
	protected void configureSMSModule() {
		
		System.out.println("SMS Publisher Polling is activated...");
		System.out.println("[Modem INFO] id:" + modem.getID() + "  comport: " + modem.getComPort() + 
				           "  baudrate: " + modem.getBaudRate() + "  manufacturer: " + modem.getManufacturer());

		try {

			if (modemGateway != null) {
				try {
					smslibServ.stopService();
				} catch (NullPointerException npe) {
					logger.info("gateway failure to stop");
				}
				smslibServ.removeGateway(modemGateway);
			} else
				smslibServ = new Service();

			modemGateway = new SerialModemGateway(modem.getID(), modem.getComPort(), modem.getBaudRate(),
					modem.getManufacturer(), modem.getModel());
			
			modemGateway.setInbound(true);

			smslibServ.addGateway(modemGateway);
			
			smslibServ.setInboundMessageNotification(new InboundNotification());
			
			smslibServ.startService();

		} catch(ExceptionInInitializerError eir){
			logger.error(eir);
		}catch (NullPointerException npe) {
			logger.error(npe.getMessage());
		} catch (GatewayException e) {
			logger.error(e.getMessage());
		} catch (TimeoutException e) {
			logger.error(e.getMessage());
		} catch (SMSLibException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
	}
	
	/**
	 * an implementation of the IInboundMessageNotification as a call back function to receive messages from the sms modem.
	 * @author nima
	 *
	 */
	public class InboundNotification implements IInboundMessageNotification {
		public void process(String gatewayId, MessageTypes msgType,
				InboundMessage msg) {
			if (msgType == MessageTypes.INBOUND) {
				logger.debug(">>> New Inbound message detected from Gateway: "
						+ gatewayId);

				// convert message into an Event and send to Broker

				Map<String, String> msgMap = new HashMap<String, String>();

				String from = (msg.getOriginator() == null ? "" : msg.getOriginator());
				String messageContent = (msg.getText() == null ? "" : msg.getText());
				String date = (msg.getDate() == null ? "" : msg.getDate().toString());

				
				// only sms for now, will add mms support later?
				// SMS Lib does not support MMS now
				msgMap.put("type", "sms");
				msgMap.put("from", from);
				msgMap.put("date", date);
				msgMap.put("message", messageContent);
				
				logger.debug("SMS Polling event: " + msgMap.toString());
				
				try {
					brokerConnector.deliver(msgMap);
				} catch (BrokerException e) {
					logger.error(e);
				}

			} else if (msgType == MessageTypes.STATUSREPORT)
				logger.debug(">>> New Inbound Status Report message detected from Gateway: "
								+ gatewayId);

				logger.debug(msg);

			try {
				smslibServ.deleteMessage(msg);
			} catch (TimeoutException e) {
				logger.error(e.getMessage());
			} catch (GatewayException e) {
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}

		}
	}

}
