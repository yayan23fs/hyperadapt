package net.hyperadapt.pxweave.interactions.devtime.xml;

import java.io.File;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * class to get all pipestations (mostly from sitemap.xmap)
 * 
 * @author danielkadner
 *
 */
public class PipelineStations {

	private static String file;

	public static NodeList getStations() {
		if (file != null) {
			try {
				File f = new File(file);

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document d = builder.parse(f);

				NamespaceContext ctx = new NamespaceContext() {
					public String getNamespaceURI(String prefix) {
						String uri;
						if (prefix.equals("map"))
							uri = "http://apache.org/cocoon/sitemap/1.0";
						else
							uri = null;
						return uri;
					}

					// Dummy implementation - not used!
					public Iterator<?> getPrefixes(String val) {
						return null;
					}

					// Dummy implemenation - not used!
					public String getPrefix(String uri) {
						return null;
					}
				};

				XPath xpath = XPathFactory.newInstance().newXPath();
				xpath.setNamespaceContext(ctx);
				String xp = "/map:sitemap/map:components[1]/map:transformers[1]/map:transformer";
				NodeList nl = (NodeList) xpath.evaluate(xp, d, XPathConstants.NODESET);
				return nl;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void setFile(String s){
		PipelineStations.file = s;
	}

}
