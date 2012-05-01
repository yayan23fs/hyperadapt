//package net.hyperadapt.pxweave.integration.jsf.customfacelets;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.net.URL;
//
//import javax.faces.view.Location;
//import javax.faces.view.facelets.Tag;
//import javax.faces.view.facelets.TagAttribute;
//import javax.faces.view.facelets.TagAttributes;
//
//import org.xml.sax.Attributes;
//import org.xml.sax.InputSource;
//import org.xml.sax.Locator;
//import org.xml.sax.SAXException; 
//import org.xml.sax.SAXParseException;
//import org.xml.sax.ext.LexicalHandler;
//import org.xml.sax.helpers.DefaultHandler;
//
//import com.sun.faces.facelets.tag.TagAttributeImpl;
//import com.sun.faces.facelets.tag.TagAttributesImpl;
//
//public class CompilationHandler extends DefaultHandler implements
//		LexicalHandler {
//
//	protected final String alias;
//
//	protected boolean inDocument = false;
//
//	protected Locator locator;
//
//	protected final Object unit;
//
//	private void compilationManagerReflection(String strMethod, Class<?> className, Object parameter) {
//		try {
//			Class<?> compilationManager = Class.forName("com.sun.faces.facelets.compiler.CompilationManager");
//			Method method = null;
//			if (className != null) { 
//				method = compilationManager.getMethod(strMethod, className);
//				method.setAccessible(true);
//				method.invoke(unit, parameter);
//			} else {
//				method = compilationManager.getMethod(strMethod);
//				method.setAccessible(true);
//				method.invoke(unit);
//			}			
//			
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private void compilationManagerReflection(String strMethod, Class<?> classNameA, Class<?> classNameB, Object parameterA, Object parameterB) {
//		try {
//			Class<?> compilationManager = Class.forName("com.sun.faces.facelets.compiler.CompilationManager");
//			Method method = compilationManager.getMethod(strMethod, classNameA, classNameB);
//			method.setAccessible(true);
//			method.invoke(unit, parameterA, parameterB);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public CompilationHandler(Object unit, String alias) {
//		this.unit = unit;
//		this.alias = alias;
//	}
//
//	public void characters(char[] ch, int start, int length)
//			throws SAXException {
//		if (this.inDocument) {
//			compilationManagerReflection("writeText", String.class, new String(ch, start, length));
//		}
//	}
//
//	public void comment(char[] ch, int start, int length) throws SAXException {
//		if (this.inDocument) {
//			compilationManagerReflection("writeComment", String[].class, new String(ch, start, length));
//		}
//	}
//
//	protected TagAttributes createAttributes(Attributes attrs) {
//		int len = attrs.getLength();
//		TagAttribute[] ta = new TagAttribute[len];
//		for (int i = 0; i < len; i++) {
//			ta[i] = new TagAttributeImpl(this.createLocation(),
//					attrs.getURI(i), attrs.getLocalName(i), attrs.getQName(i),
//					attrs.getValue(i));
//		}
//		return new TagAttributesImpl(ta);
//	}
//
//	protected Location createLocation() {
//		return new Location(this.alias, this.locator.getLineNumber(),
//				this.locator.getColumnNumber());
//	}
//
//	public void endCDATA() throws SAXException {
//		if (this.inDocument) {
//			compilationManagerReflection("writeComment", String.class, "]]>");
//		}
//	}
//
//	public void endDocument() throws SAXException {
//		super.endDocument();
//	}
//
//	public void endDTD() throws SAXException {
//		this.inDocument = true;
//	}
//
//	public void endElement(String uri, String localName, String qName)
//			throws SAXException {
//		compilationManagerReflection("popTag", null, null);
//	}
//
//	public void endEntity(String name) throws SAXException {
//	}
//
//	public void endPrefixMapping(String prefix) throws SAXException {
//		compilationManagerReflection("popNamespace", String.class, prefix);
//	}
//
//	public void fatalError(SAXParseException e) throws SAXException {
//		if (this.locator != null) {
//			throw new SAXException("Error Traced[line: "
//					+ this.locator.getLineNumber() + "] " + e.getMessage());
//		} else {
//			throw e;
//		}
//	}
//
//	public void ignorableWhitespace(char[] ch, int start, int length)
//			throws SAXException {
//		if (this.inDocument) {
//			compilationManagerReflection("writeWhitespace", String[].class, new String(ch, start, length));
//		}
//	}
//
//	public InputSource resolveEntity(String publicId, String systemId)
//			throws SAXException {
//		String dtd = "com/sun/faces/xhtml/default.dtd";
//		/*
//		 * if ("-//W3C//DTD XHTML 1.0 Transitional//EN".equals(publicId)) { dtd
//		 * = "xhtml1-transitional.dtd"; } else if (systemId != null &&
//		 * systemId.startsWith("file:/")) { return new InputSource(systemId); }
//		 */
//		URL url = this.getClass().getClassLoader().getResource(dtd);
//		return new InputSource(url.toString());
//	}
//
//	public void setDocumentLocator(Locator locator) {
//		this.locator = locator;
//	}
//
//	public void startCDATA() throws SAXException {
//		if (this.inDocument) {
//			compilationManagerReflection("writeInstruction", String.class, "<![CDATA[");
//		}
//	}
//
//	public void startDocument() throws SAXException {
//		this.inDocument = true;
//	}
//
//	public void startDTD(String name, String publicId, String systemId)
//			throws SAXException {
//		if (this.inDocument) {
//			// If we're in an ajax request, this is unnecessary and bugged
//			// RELEASE_PENDING - this is a hack, and should probably not be
//			// here -
//			// but the alternative is to somehow figure out how *not* to
//			// escape the "<!"
//			// within the cdata of the ajax response. Putting the PENDING in
//			// here to
//			// remind me to have rlubke take a look. But I'm stumped.
//			StringBuffer sb = new StringBuffer(64);
//			sb.append("<!DOCTYPE ").append(name);
//			if (publicId != null) {
//				sb.append(" PUBLIC \"").append(publicId).append("\"");
//				if (systemId != null) {
//					sb.append(" \"").append(systemId).append("\"");
//				}
//			} else if (systemId != null) {
//				sb.append(" SYSTEM \"").append(systemId).append("\"");
//			}
//			sb.append(">\n");
//			compilationManagerReflection("writeInstruction", String.class, sb.toString());
//		}
//		this.inDocument = false;
//	}
//
//	public void startElement(String uri, String localName, String qName,
//			Attributes attributes) throws SAXException {
//		compilationManagerReflection("pushTag", Tag.class, new Tag(this.createLocation(), uri, localName, qName,
//				this.createAttributes(attributes)));
//	}
//
//	public void startEntity(String name) throws SAXException {
//	}
//
//	public void startPrefixMapping(String prefix, String uri)
//			throws SAXException {
//		compilationManagerReflection("pushNamespace", String.class, String.class, prefix, uri);
//	}
//
//	public void processingInstruction(String target, String data)
//			throws SAXException {
//		if (this.inDocument) {
//			StringBuffer sb = new StringBuffer(64);
//			sb.append("<?").append(target).append(' ').append(data)
//					.append("?>\n");
//			compilationManagerReflection("writeInstruction", String.class, sb.toString());
//		}
//	}
//}

