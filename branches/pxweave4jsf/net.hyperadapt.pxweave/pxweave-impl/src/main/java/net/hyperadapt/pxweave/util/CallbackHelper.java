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

/**
 * The CallbackHelper proceeds and saves the context data from an extern
 * ontologie. Futhermore it creates a axis based server to regist a callback
 * handler for the given query.
 * 
 * @author Martin Lehmann
 * 
 */
public class CallbackHelper {

	private static CallbackHelper helper;
	private static AxisServer server = null;

	private Map<String, List<String>> sparqlMap = new HashMap<String, List<String>>();
	private Map<String, Map<String, String>> sessionMap = new HashMap<String, Map<String, String>>();

	private CallbackHelper() {
	}

	/**
	 * Use of the singleton pattern to secure that there is only one
	 * CallbackHelper registed.
	 * 
	 * @return
	 */
	public static CallbackHelper getInstance() {
		if (helper == null) {
			helper = new CallbackHelper();
		}
		return helper;
	}

	/**
	 * Add a session to a session list and contect it to a sparql query.
	 * 
	 * @param sparql
	 *            - query from the ontologie
	 * @param sessionId
	 *            - session id from the current user
	 */
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

	/**
	 * Returns a Map of all session based information to refresh the context
	 * parameter of an user.
	 * 
	 * @param sessionId
	 *            - session id of the current user
	 * @return context data
	 */
	public Map<String, String> getEntryForSession(String sessionId) {
		Map<String, String> newEntries = sessionMap.get(sessionId);
		sessionMap.remove(sessionId);
		return newEntries;
	}

	/**
	 * Handles the query and put it in a session map based on the session id
	 * from a user.
	 * 
	 * @param sparqlQuery
	 *            - request sparql query from the ontologie
	 * @param result
	 *            - the evaluated response from the ontologie
	 */
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

	/**
	 * The Method regist a callback handler on a specific endpoint to handle
	 * changes in the ontologie.
	 * 
	 * @param endpoint
	 *            - endpoint to the webservice
	 * @throws AxisFault
	 *             - during a fault
	 */
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

	/**
	 * Interface to over classes, which control the callback mechanism and
	 * invoke the webservice.
	 * 
	 * @param endpoint - endpoint of the webservice
	 * @param query - query, which is send to the ontologie
	 * @param sessionId - session id of the current user
	 * @throws AxisFault - during a fault
	 */
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
