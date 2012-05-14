package ca.ubc.magic.broker.subscriber.service.ws.test;

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

public class WebServiceClientTest {

	static int port = 9090;
	
	public static void main(String[] args) throws Exception {
		
//		subscribeTest("test2", "5", null);
//		unsubscribeTest("test4", "2"); 
		
//		sendEventTest("test2", "4", "attr1", "value1", null, null);
//		receiveEventsTest("test2", "4");
		
//		queryEventsTest("test2", "4", null);
//		queryClientsTest("test2", "5");
		
//		putStateTest("test3", "2", "state2", "value2");
//		getStateTest("test3", "2", null);
//		deleteStateTest("test3", "2", "state2");
		
//		keepAliveTest("test2", "5", "60");
	}

	@SuppressWarnings({ "unused" })
	private static void subscribeTest(String topic, String clientid, String expires) throws Exception {
		try {
			String serviceName = "subscribe";
			EndpointReference targetEPR = new EndpointReference("http://localhost:"+port+"/services/"+serviceName);
			
			OMFactory fac= OMAbstractFactory.getOMFactory();
			OMNamespace omNs= fac.createOMNamespace(
			"http://ws.service.subscriber.broker.magic.ubc.ca", "subscribe");
			OMElement operation = fac.createOMElement("subscribe", omNs);
			
			OMElement param1 = fac.createOMElement("topic", omNs);
			param1.addChild(fac.createOMText(topic));
			operation.addChild(param1);
			
			OMElement param2 = fac.createOMElement("clientid", omNs);
			param2.addChild(fac.createOMText(clientid));
			operation.addChild(param2);
			
			if(expires!=null)
			{
				OMElement param3 = fac.createOMElement("expires", omNs);
				param3.addChild(fac.createOMText(expires));
				operation.addChild(param3);
			}
			
			Options options= new Options();
			options.setTo(targetEPR);
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setAction("http://localhost:"+port+"/services/subscribe/subscribe");
			
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			
			OMElement result = sender.sendReceive(operation);
			StringWriter writer = new StringWriter();
			result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
			writer.flush();
			System.out.println(writer.toString());
		} catch (AxisFault e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unused" })
	private static void unsubscribeTest(String topic, String clientid) throws Exception {
		try {
			String serviceName = "unsubscribe";
			EndpointReference targetEPR = new EndpointReference("http://localhost:"+port+"/services/"+serviceName);
			
			OMFactory fac= OMAbstractFactory.getOMFactory();
			OMNamespace omNs= fac.createOMNamespace(
			"http://ws.service.subscriber.broker.magic.ubc.ca", serviceName);
			OMElement add = fac.createOMElement("unsubscribe", omNs);
			
			OMElement number1 = fac.createOMElement("topic", omNs);
			number1.addChild(fac.createOMText(topic));
			add.addChild(number1);
			
			OMElement number2 = fac.createOMElement("clientid", omNs);
			number2.addChild(fac.createOMText(clientid));
			add.addChild(number2);
			
			Options options= new Options();
			options.setTo(targetEPR);
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setAction("http://localhost:"+port+"/services/"+serviceName+""+"/unsubscribe");
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			OMElement result = sender.sendReceive(add);
			StringWriter writer = new StringWriter();
			result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
			writer.flush();
			System.out.println(writer.toString());
		} catch (AxisFault e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unused" })
	private static void sendEventTest(String topic, String clientid, String attr, String value, String receiverid, String excludeid) {
		try {
			String serviceName = "events";
			EndpointReference targetEPR = new EndpointReference("http://localhost:"+port+"/services/"+serviceName);
			
			OMFactory fac= OMAbstractFactory.getOMFactory();
			OMNamespace omNs= fac.createOMNamespace(
			"http://ws.service.subscriber.broker.magic.ubc.ca", serviceName);
			
			OMElement param = fac.createOMElement("sendEvent", omNs);
			
			OMElement number1 = fac.createOMElement("topic", omNs);
			number1.addChild(fac.createOMText(topic));
			param.addChild(number1);
			
			OMElement number2 = fac.createOMElement("clientid", omNs);
			number2.addChild(fac.createOMText(clientid));
			param.addChild(number2);
			
			OMElement number3 = fac.createOMElement("attr", omNs);
			number3.addChild(fac.createOMText(attr));
			param.addChild(number3);
			
			OMElement number4 = fac.createOMElement("value", omNs);
			number4.addChild(fac.createOMText(value));
			param.addChild(number4);
			
			if(receiverid!=null)
			{
				OMElement number5 = fac.createOMElement("receiverid", omNs);
				number5.addChild(fac.createOMText(receiverid));
				param.addChild(number5);
			}

			if(excludeid!=null)
			{
				OMElement number6 = fac.createOMElement("excludeid", omNs);
				number6.addChild(fac.createOMText(excludeid));
				param.addChild(number6);
			}
			
			Options options= new Options();
			options.setTo(targetEPR);
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setAction("http://localhost:"+port+"/services/"+serviceName+"sendEvent");
			
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			sender.sendReceive(param);
		} catch (AxisFault e) {
//			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unused" })
	private static void receiveEventsTest(String topic, String clientid) {
		try {
			String serviceName = "events";
			EndpointReference targetEPR = new EndpointReference("http://localhost:"+port+"/services/"+serviceName);
			
			OMFactory fac= OMAbstractFactory.getOMFactory();
			OMNamespace omNs= fac.createOMNamespace(
			"http://ws.service.subscriber.broker.magic.ubc.ca", serviceName);
			
			OMElement param = fac.createOMElement("receiveEvents", omNs);
			
			OMElement number1 = fac.createOMElement("topic", omNs);
			number1.addChild(fac.createOMText(topic));
			param.addChild(number1);
			
			OMElement number2 = fac.createOMElement("clientid", omNs);
			number2.addChild(fac.createOMText(clientid));
			param.addChild(number2);
			
			Options options= new Options();
			options.setTo(targetEPR);
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setAction("http://localhost:"+port+"/services/"+serviceName+"receiveEvents");
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			sender.sendReceive(param);
		} catch (AxisFault e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unused" })
	private static void queryEventsTest(String topic, String clientid, String querySize) throws Exception {
		try {
			String serviceName = "topic";
			EndpointReference targetEPR = new EndpointReference("http://localhost:"+port+"/services/"+serviceName);
			
			OMFactory fac= OMAbstractFactory.getOMFactory();
			OMNamespace omNs= fac.createOMNamespace(
			"http://ws.service.subscriber.broker.magic.ubc.ca", serviceName);
			
			OMElement param = fac.createOMElement("queryEvents", omNs);
			
			OMElement number1 = fac.createOMElement("topic", omNs);
			number1.addChild(fac.createOMText(topic));
			param.addChild(number1);
			
			OMElement number2 = fac.createOMElement("clientid", omNs);
			number2.addChild(fac.createOMText(clientid));
			param.addChild(number2);
			
			if(querySize!=null)
			{
				OMElement number3 = fac.createOMElement("querySize", omNs);
				number3.addChild(fac.createOMText(querySize));
				param.addChild(number3);
			}
			Options options= new Options();
			options.setTo(targetEPR);
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setAction("http://localhost:"+port+"/services/"+serviceName+"queryEvents");
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			OMElement result = sender.sendReceive(param);
			
			StringWriter writer = new StringWriter();
			result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
			writer.flush();
			System.out.println(writer.toString());
			
		} catch (AxisFault e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unused" })
	private static void queryClientsTest(String topic, String clientid) throws Exception {
		try {
			String serviceName = "topic";
			EndpointReference targetEPR = new EndpointReference("http://localhost:"+port+"/services/"+serviceName);
			
			OMFactory fac= OMAbstractFactory.getOMFactory();
			OMNamespace omNs= fac.createOMNamespace(
			"http://ws.service.subscriber.broker.magic.ubc.ca", "topic");
			
			OMElement param = fac.createOMElement("queryClients", omNs);
			
			OMElement number1 = fac.createOMElement("topic", omNs);
			number1.addChild(fac.createOMText(topic));
			param.addChild(number1);
			
			OMElement number2 = fac.createOMElement("clientid", omNs);
			number2.addChild(fac.createOMText(clientid));
			param.addChild(number2);
			
			targetEPR = new EndpointReference("http://localhost:"+port+"/services/topic");
			
			Options options= new Options();
			options.setTo(targetEPR);
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setAction("http://localhost:"+port+"/services/topic/queryClients");
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			OMElement result = sender.sendReceive(param);
			
			StringWriter writer = new StringWriter();
			result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
			writer.flush();
			System.out.println(writer.toString());
			
		} catch (AxisFault e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unused" })
	private static void putStateTest(String topic, String clientid, String attr, String value) {
		try {
			String serviceName = "state";
			EndpointReference targetEPR = new EndpointReference("http://localhost:"+port+"/services/"+serviceName);
			
			OMFactory fac= OMAbstractFactory.getOMFactory();
			OMNamespace omNs= fac.createOMNamespace(
			"http://ws.service.subscriber.broker.magic.ubc.ca", serviceName);
			
			OMElement param = fac.createOMElement("putState", omNs);
			
			OMElement number1 = fac.createOMElement("topic", omNs);
			number1.addChild(fac.createOMText(topic));
			param.addChild(number1);
			
			OMElement number2 = fac.createOMElement("clientid", omNs);
			number2.addChild(fac.createOMText(clientid));
			param.addChild(number2);
			
			OMElement number3 = fac.createOMElement("attr", omNs);
			number3.addChild(fac.createOMText(attr));
			param.addChild(number3);
			
			OMElement number4 = fac.createOMElement("value", omNs);
			number4.addChild(fac.createOMText(value));
			param.addChild(number4);
			
			Options options= new Options();
			options.setTo(targetEPR);
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setAction("http://localhost:"+port+"/services/"+serviceName+"putState");
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			sender.sendReceive(param);
		} catch (AxisFault e) {
//			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unused" })
	private static void getStateTest(String topic, String clientid, String stateid) throws Exception {
		try {
			String serviceName = "state";
			EndpointReference targetEPR = new EndpointReference("http://localhost:"+port+"/services/"+serviceName);
			
			OMFactory fac= OMAbstractFactory.getOMFactory();
			OMNamespace omNs= fac.createOMNamespace(
			"http://ws.service.subscriber.broker.magic.ubc.ca", serviceName);
			
			OMElement param = fac.createOMElement("getState", omNs);
			
			OMElement number1 = fac.createOMElement("topic", omNs);
			number1.addChild(fac.createOMText(topic));
			param.addChild(number1);
			
			OMElement number2 = fac.createOMElement("clientid", omNs);
			number2.addChild(fac.createOMText(clientid));
			param.addChild(number2);
			
			OMElement number3 = fac.createOMElement("stateid", omNs);
			number2.addChild(fac.createOMText(stateid));
			param.addChild(number2);
			
			Options options= new Options();
			options.setTo(targetEPR);
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setAction("http://localhost:"+port+"/services/"+serviceName+"getState");
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			OMElement result = sender.sendReceive(param);
			
			StringWriter writer = new StringWriter();
			result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
			writer.flush();
			System.out.println(writer.toString());
			
		} catch (AxisFault e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unused" })
	private static void deleteStateTest(String topic, String clientid, String stateid) {
		try {
			String serviceName = "state";
			EndpointReference targetEPR = new EndpointReference("http://localhost:"+port+"/services/"+serviceName);
			
			OMFactory fac= OMAbstractFactory.getOMFactory();
			OMNamespace omNs= fac.createOMNamespace(
			"http://ws.service.subscriber.broker.magic.ubc.ca", serviceName);
			
			OMElement param = fac.createOMElement("deleteState", omNs);
			
			OMElement number1 = fac.createOMElement("topic", omNs);
			number1.addChild(fac.createOMText(topic));
			param.addChild(number1);
			
			OMElement number2 = fac.createOMElement("clientid", omNs);
			number2.addChild(fac.createOMText(clientid));
			param.addChild(number2);
			
			OMElement number3 = fac.createOMElement("stateid", omNs);
			number3.addChild(fac.createOMText(stateid));
			param.addChild(number3);
			
			Options options= new Options();
			options.setTo(targetEPR);
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setAction("http://localhost:"+port+"/services/"+serviceName+"deleteState");
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			sender.sendReceive(param);
		} catch (AxisFault e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "unused" })
	private static void keepAliveTest(String topic, String clientid, String expires) throws Exception {
		try {
			String serviceName = "keepAlive";
			EndpointReference targetEPR = new EndpointReference("http://localhost:"+port+"/services/"+serviceName);
			
			OMFactory fac= OMAbstractFactory.getOMFactory();
			OMNamespace omNs= fac.createOMNamespace(
			"http://ws.service.subscriber.broker.magic.ubc.ca", "keepAlive");
			OMElement operation = fac.createOMElement("keepAlive", omNs);
			
			OMElement param1 = fac.createOMElement("topic", omNs);
			param1.addChild(fac.createOMText(topic));
			operation.addChild(param1);
			
			OMElement param2 = fac.createOMElement("clientid", omNs);
			param2.addChild(fac.createOMText(clientid));
			operation.addChild(param2);
			
			if(expires!=null)
			{
				OMElement param3 = fac.createOMElement("expires", omNs);
				param3.addChild(fac.createOMText(expires));
				operation.addChild(param3);
			}
			
			Options options= new Options();
			options.setTo(targetEPR);
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			options.setAction("http://localhost:"+port+"/services/subscribe/keepAlive");
			
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			
			OMElement result = sender.sendReceive(operation);
			StringWriter writer = new StringWriter();
			result.serialize(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
			writer.flush();
			System.out.println(writer.toString());
		} catch (AxisFault e) {
			//e.printStackTrace();
		}
	}
}
