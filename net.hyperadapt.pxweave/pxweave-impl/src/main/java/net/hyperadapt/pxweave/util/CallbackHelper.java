package net.hyperadapt.pxweave.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.hyperadapt.pxweave.config.ast.ContextModelEndpoint;
import net.hyperadapt.pxweave.contextmodel.ContextCallbackHandler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.AxisServer;
import org.apache.axis2.rpc.client.RPCServiceClient;

public class CallbackHelper {

	private static CallbackHelper helper;
	private static AxisServer server = null;

	private Map<String, List<String>> sparqlMap = new HashMap<String, List<String>>();
	private Map<String, Map<String, String>> sessionMap = new HashMap<String, Map<String, String>>();

	private CallbackHelper() {
	}

	public static CallbackHelper getInstance() {
		if (helper == null) {
			helper = new CallbackHelper();
		}
		return helper;
	}

	private void addSparql(String sparql, String sessionId) {
		List<String> sessionList = sparqlMap.get(sparql);
		if (sessionList == null) {
			sessionList = new ArrayList<String>();
		}
		if (!sessionList.contains(sessionId)) {
			sessionList.add(sessionId);
			sparqlMap.put(sparql, sessionList);
		}
	}

	public Map<String, String> getEntryForSession(String sessionId) {
		Map<String, String> newEntries = sessionMap.get(sessionId);
		sessionMap.remove(sessionId);
		return newEntries;
	}

	public synchronized void handleQuery(final String sparqlQuery,
			final String result) {
		List<String> sessionList = sparqlMap.get(sparqlQuery);
		if (sessionList == null) {
			return;
		}
		Map<String, String> resultValues = ContextModelHelper
				.splitAnswerXml(result);
		for (String session : sessionList) {
			Map<String, String> oldMap = sessionMap.get(session);
			if (oldMap == null) {
				sessionMap.put(session, resultValues);
			} else {
				oldMap.putAll(resultValues);
				sessionMap.put(session, oldMap);
			}
		}
	}

	private void registerCallbackHandler(ContextModelEndpoint endpoint)
			throws AxisFault {
		if (server == null) {
			server = new AxisServer();
			try {
				server.deployService(ContextCallbackHandler.class.getName());
			} catch (final AxisFault e1) {
				e1.printStackTrace();
			}
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						server.stop();
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			});
			try {
				server.setConfigurationContext(ConfigurationContextFactory
						.createDefaultConfigurationContext());
			} catch (final Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public synchronized void updateCallbackHandler(
			ContextModelEndpoint endpoint, String query, String sessionId)
			throws AxisFault {
		registerCallbackHandler(endpoint);

		addSparql(query, sessionId);

		RPCServiceClient serviceClient = new RPCServiceClient();
		Options options = serviceClient.getOptions();
		options.setTo(new EndpointReference(endpoint.getUrl()));

		QName qName = new QName("http://webservice.croco.inf.tudresden.de",
				"requestNotification");

		Object[] opSetQuery = new Object[3];
		opSetQuery[0] = "http://localhost:6060/axis2/services/CallbackHandler";
		opSetQuery[1] = "http://test.webservice.croco.kp2010.de";
		opSetQuery[2] = query;

		serviceClient.invokeBlocking(qName, opSetQuery,
				new Class[] { String.class });
	}
}
