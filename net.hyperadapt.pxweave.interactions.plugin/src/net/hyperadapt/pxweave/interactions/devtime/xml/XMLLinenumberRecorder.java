package net.hyperadapt.pxweave.interactions.devtime.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

/**
 * class to get the linenumber for results for apply a xpath on an document
 * Hilfsklasse um Zeilennummern für die Anwendung eines XPath auf ein XML Dokument zu erhalten
 * 
 * @author danielkadner
 *
 */
public class XMLLinenumberRecorder extends DefaultHandler {
	public static final String KEY_LINE_NO = "lineNumber";
	public static final String KEY_COLUMN_NO = "columnNumber";

	private Document doc;
	private Locator locator = null;
	private Element current;

	public XMLLinenumberRecorder(Document doc) {
		this.doc = doc;
	}

	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	private void setLocationData(Node n) {
		if (locator != null) {
			n.setUserData(KEY_LINE_NO, locator.getLineNumber(), null);
			n.setUserData(KEY_COLUMN_NO, locator.getColumnNumber(), null);
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes attrs) {
		Element e = null;
		if (localName != null && !"".equals(localName)) e = doc.createElementNS(uri, localName);
		else e = doc.createElement(qName.toLowerCase());

		setLocationData(e);

		if (current == null) doc.appendChild(e);
		else current.appendChild(e);
		current = e;
	}

	public void endElement(String uri, String localName, String qName) {
		Node parent;
		if (current == null) return;
		parent = current.getParentNode();
		if (parent.getParentNode() == null) {
			current.normalize();
			current = null;
		} else current = (Element) current.getParentNode();
	}

	public void characters(char buf[], int offset, int length) {
		if (current != null) {
			String text = new String(buf, offset, length);
			text = text.replace("\'", "\"");
//			System.out.println(text);
			Node n = doc.createTextNode(text);
			setLocationData(n);
			current.appendChild(n);
		}
	}
}