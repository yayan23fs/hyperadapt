package net.hyperadapt.pxweave.integration.selenium;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * The class creates a integration test suite with the selenium framework to
 * test the adaptation of pxweave and the integration within the web
 * application.
 * 
 * @author Martin Lehmann
 * 
 */
public class IntegrationTest {

	private static final String waitForPageLoad = "100000";
	private static final String URL = "http://localhost:8080/jsfpxweave/hello.html";

	@Test
	public void testIntegration() {
		Selenium selenium = new DefaultSelenium("localhost", 4444, "*firefox",
				URL);
		selenium.start();
		selenium.open(URL);
		selenium.waitForPageToLoad(waitForPageLoad);

		// Parse the document
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document doc = null;
		try {
			factory.setFeature(
					"http://apache.org/xml/features/nonvalidating/load-external-dtd",
					false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(URL);
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(doc, "Loading error of the web application!");

		// Generate xpath expression
		XPathFactory pathFactory = XPathFactory.newInstance();
		XPath xpath = pathFactory.newXPath();

		// Handle namespaces within the document
		try {
			xpath.setNamespaceContext(new NamespaceContext() {
				@SuppressWarnings("rawtypes")
				public Iterator getPrefixes(String arg0) {
					throw new UnsupportedOperationException();
				}

				public String getPrefix(String arg0) {
					throw new UnsupportedOperationException();
				}

				public String getNamespaceURI(String arg0) {
					if (arg0 == null)
						throw new NullPointerException("Null prefix");
					else if ("xhtml".equals(arg0))
						return "http://www.w3.org/1999/xhtml";
					else if ("xml".equals(arg0))
						return XMLConstants.XML_NS_URI;
					return XMLConstants.NULL_NS_URI;
				}
			});

			// Test adaption of px-weave (AspectChangeValue, AspectChangeOrder)
			// as well as the correct web application
			// integration
			String adaptationWorks = (String) xpath.evaluate(
					"//xhtml:span[@id='navigationButtons']/xhtml:h1", doc,
					XPathConstants.STRING);
			Assert.assertTrue(adaptationWorks.contains("Adaptation works"),
					"Adaptation of the aspect 'AspectChangeValue' doesn't work!");

			Node navigationButtonsNode = (Node) xpath.evaluate(
					"//xhtml:span[@id='navigationButtons']", doc,
					XPathConstants.NODE);
			Node iterNodes = navigationButtonsNode.getFirstChild()
					.getNextSibling();
			Assert.assertTrue(iterNodes.getTextContent().contains("Greetings"),
					"Adaptation of the aspect 'AspectChangeOrder' doesn't work!");
			iterNodes = iterNodes.getNextSibling().getNextSibling();
			Assert.assertTrue(iterNodes.getTextContent().contains("Hello"),
					"Adaptation of the aspect 'AspectChangeOrder' doesn't work!");

		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}
}
