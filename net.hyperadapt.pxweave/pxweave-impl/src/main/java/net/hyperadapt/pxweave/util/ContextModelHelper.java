package net.hyperadapt.pxweave.util;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.ast.AdviceGroup;
import net.hyperadapt.pxweave.aspects.ast.Dependency;
import net.hyperadapt.pxweave.config.ast.ContextModelEndpoint;
import net.hyperadapt.pxweave.context.IWeavingContext;
import net.hyperadapt.pxweave.context.ast.Parameter;
import net.hyperadapt.pxweave.context.ast.StringParameter;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ContextModelHelper {

	private static MultiThreadedHttpConnectionManager httpConnectionManager;
	private static HttpClient httpClient;
	private static RPCServiceClient serviceClient;

	static {
		httpConnectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setDefaultMaxConnectionsPerHost(20);
		// params.setConnectionTimeout(5000);
		// params.setLinger(5000);
		params.setMaxConnectionsPerHost(
				HostConfiguration.ANY_HOST_CONFIGURATION, 50);
		params.setMaxTotalConnections(50);
		// params.setSoTimeout(5000);
		httpConnectionManager.setParams(params);
		httpClient = new HttpClient(httpConnectionManager);
	}

	public static boolean evaluateAdviceCondition(
			ContextModelEndpoint endpoint, AdviceGroup adviceGroup,
			IWeavingContext context) throws XMLWeaverException {

		if (adviceGroup == null || endpoint == null) {
			return false;
		}

		Dependency dependency = adviceGroup.getDepends();

		if (dependency == null || dependency.getSparql() == null
				|| dependency.getSparql().getValue() == null) {
			return true;
		}

		String query = replaceContextParams(dependency.getSparql().getValue(),
				context, false);

		StringParameter param = (StringParameter) context
				.getParameter("sessionId");
		String response = getResponse(endpoint, query,
				param != null ? param.getValue() : "");
		Map<String, String> resultMap = splitAnswerXml(response);

		if (resultMap != null && !resultMap.isEmpty()) {
			saveResult(resultMap, context);
		} else {
			return false;
		}

		for (String result : resultMap.values()) {
			if (result.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public static String replaceContextParams(String query,
			IWeavingContext context, boolean ignoreEmpty) {

		String newQuery = query;

		Pattern pat = Pattern.compile("\\Q${\\E.*?\\}");
		Matcher mat = pat.matcher(newQuery);
		while (mat.find()) {
			String param = mat.group(0);
			Object value = context.getParameterValue(param.replaceAll(
					"[\\Q${}\\E]", ""));

			if (value == null || (!ignoreEmpty && value.toString().isEmpty())) {
				return null;
			}

			newQuery = newQuery.replace(param, value.toString());
		}

		return newQuery;
	}

	public static synchronized void saveResult(Map<String, String> resultMap,
			IWeavingContext context) {
		for (String key : resultMap.keySet()) {
			Parameter param = context.getParameter(key);

			if (param == null) {
				StringParameter strParam = new StringParameter();
				strParam.setName(key);
				strParam.setValue(resultMap.get(key));
				context.addParameter(strParam);
				continue;
			}
			if (param instanceof StringParameter) {
				((StringParameter) param).setValue(resultMap.get(key));
			}
		}
	
	}

	private static synchronized String getResponse(
			ContextModelEndpoint endpoint, String query, String sessionId)
			throws XMLWeaverException {
		try {

			serviceClient = new RPCServiceClient();
			Options options = serviceClient.getOptions();
			options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, true);
			// options.setProperty(HTTPConstants.SO_TIMEOUT, 5000);
			// options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 5000);
			// options.setTimeOutInMilliSeconds(5000);
			options.setProperty(HTTPConstants.AUTO_RELEASE_CONNECTION, true);
			options.setProperty(HTTPConstants.CACHED_HTTP_CLIENT, httpClient);

			// Options options = serviceClient.getOptions();
			options.setTo(new EndpointReference(endpoint.getUrl()));

			QName qName = new QName(endpoint.getTargetNamespace(),
					endpoint.getOperation());

			Object[] opSetQuery = new Object[] { query };

			// PXWeaveAxisCallback callback = new PXWeaveAxisCallback();
			// serviceClient.invokeNonBlocking(qName, opSetQuery, callback);
			// boolean recall = false;
			// while (!callback.isComplete()) {
			// if (recall) {
			// serviceClient
			// .invokeNonBlocking(qName, opSetQuery, callback);
			// recall = false;
			// }
			// Thread.sleep(1000);
			// recall = true;
			// }

			Object[] returnObject = serviceClient.invokeBlocking(qName,
					opSetQuery, new Class[] { String.class });

			// serviceClient.cleanup();
			serviceClient.cleanupTransport();
			if (returnObject != null && returnObject.length > 0) {
				// CallbackHelper.getInstance().updateCallbackHandler(endpoint,
				// query, sessionId);
				// serviceClient.cleanup();
				// serviceClient.cleanupTransport();
				return returnObject[0].toString();
			}

		} catch (Exception e) {
			throw new XMLWeaverException(
					"Exception by invoking the context webservice!");
		}
		return "";
	}

	protected static Map<String, String> splitAnswerXml(final String xmlText) {
		final Map<String, String> bindMap = new HashMap<String, String>();
		if (xmlText == null || xmlText.equals("")) {
			return bindMap;
		}
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory
					.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlText));

			final Document doc = db.parse(is);
			if (doc.getElementsByTagName("result").getLength() == 0) {

				final NodeList nodes = doc.getElementsByTagName("variable");
				for (int i = 0; i < nodes.getLength(); i++) {
					final Element element = (Element) nodes.item(i);
					String name = element.getAttribute("name");
					if (name != null) {
						bindMap.put(name, "");
					}
				}
				return bindMap;
			}
			final NodeList nodes = doc.getElementsByTagName("result");

			// iterate the employees
			for (int i = 0; i < nodes.getLength(); i++) {
				final Element element = (Element) nodes.item(i);
				final NodeList bindings = element
						.getElementsByTagName("binding");

				for (int j = 0; j < bindings.getLength(); j++) {
					final Element bind = (Element) bindings.item(j);
					final String bindName = bind.getAttribute("name");
					NodeList uris = bind.getElementsByTagName("uri");
					for (int k = 0; k < uris.getLength(); k++) {
						final Element uri = (Element) uris.item(k);
						final String bindUri = getCharacterDataFromElement(uri);
						bindMap.put(bindName, bindUri);
					}
					uris = bind.getElementsByTagName("literal");
					for (int k = 0; k < uris.getLength(); k++) {
						final Element uri = (Element) uris.item(k);
						final String bindUri = getCharacterDataFromElement(uri);
						bindMap.put(bindName, bindUri);
					}
				}
			}
		} catch (final Exception e) {
		}
		return bindMap;
	}

	private static String getCharacterDataFromElement(final Element e) {
		final Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			final CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "?";
	}
}
