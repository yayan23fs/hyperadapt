/**
 * 
 */
package net.hyperadapt.pxweave.validation.test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.validation.IDOMFactory;
import net.hyperadapt.pxweave.validation.IDOMParser;
import net.hyperadapt.pxweave.validation.IDOMValidator;
import net.hyperadapt.pxweave.validation.ValidationMode;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.validation.NodeEditVAL;

/**
 * @author martin
 * 
 */
public class ValidatorTest {

	private IDOMParser parserVal;
	private IDOMValidator validatorVal;
	private IDOMParser parser;
	private IDOMValidator validator;
	private final IDOMFactory dfVal = ValidationMode.createDOM3ValidationAPIFactory();
	private final IDOMFactory df = ValidationMode.createDOM3Factory();
	private Document docVal;
	private Document doc;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		parserVal = dfVal.createDOMParser();
		validatorVal = dfVal.createDOMValidator();
		parser = df.createDOMParser();
		validator = df.createDOMValidator();
		validator.setSchema((new File("testData/book.xsd")).toURI());
		docVal = parserVal.buildDOM((new File("testData/book.xml")).toURI(),
				(new File("testData/book.xsd")).toURI());
		doc = parser.buildDOM((new File("testData/book.xml")).toURI(),
				 (new File("testData/book.xsd")).toURI());
	}

	/**
	 * Test method for {@link IDOMValidator#validateDocument(Document)}.
	 * Document is valid, result of
	 * {@link IDOMValidator#validateDocument(Document)} must be
	 * {@link NodeEditVAL#VAL_TRUE}
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testValidateDocument() throws XMLWeaverException {
		final short validVal = validatorVal.validateDocument(docVal);
		final short valid = validator.validateDocument(doc);
		assertEquals("The document should be valid", validVal,
				NodeEditVAL.VAL_TRUE);
		assertEquals("The document should be valid", valid,
				NodeEditVAL.VAL_TRUE);
	}
 
	/**
	 * Test method for {@link IDOMValidator#validateElement(Element)}. The
	 * Element is valid, result of
	 * {@link IDOMValidator#validateElement(Element)} must be
	 * {@link NodeEditVAL#VAL_TRUE}
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testValidateElement() throws XMLWeaverException {
		final Element elementVal3 = (Element) docVal.getDocumentElement()
				.getElementsByTagName("title").item(0);
		final Element element = (Element) doc.getDocumentElement()
				.getElementsByTagName("title").item(0);

		short valid = validatorVal.validateElement(elementVal3);
		assertEquals("element should be valid", valid, NodeEditVAL.VAL_TRUE);
		valid = validator.validateElement(element);
		assertEquals("element should be valid", valid, NodeEditVAL.VAL_TRUE);
	}

	/**
	 * Test method for {@link IDOMValidator#validateElement(Element)}. The
	 * Element is invalid, an {@link XMLWeaverException} must be thrown at
	 * validating.
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testValidateElementForInvalid() throws XMLWeaverException {
		final Element element = (Element) doc.getDocumentElement()
				.getElementsByTagName("author").item(0);
		element.removeChild(element.getElementsByTagName("lastName").item(0));
		validator.validateElement(element);
	}

	/**
	 * Test method for {@link IDOMValidator#validateElement(Element)}. The
	 * Element is invalid, an {@link XMLWeaverException} must be thrown at
	 * validating.
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testValidateElementForInvalidVal() throws XMLWeaverException {
		final Element element = (Element) docVal.getDocumentElement()
				.getElementsByTagName("author").item(0);
		Node parent = element.getParentNode();
		parent.removeChild(element);
		validatorVal.validateElement((Element)parent);
	}

	/**
	 * Test method for {@link IDOMValidator#validateElement(Element)}. The
	 * Element is valid, but can't be validated, since its not globally known.
	 * All Elements have to be declared in the global name space, therefore an
	 * {@link XMLWeaverException} must be thrown at validating.
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testValidateElementForInvalidVal2() throws XMLWeaverException {
		final Element element = (Element) docVal.getDocumentElement()
				.getElementsByTagName("edition").item(0);
		validatorVal.validateElement(element);
	}

	/**
	 * Test method for {@link IDOMValidator#validateElement(Element)}. The
	 * Element is valid, but can't be validated, since its not globally known.
	 * All Elements have to be declared in the global name space, therefore an
	 * {@link XMLWeaverException} must be thrown at validating.
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testValidateElementForInvalid2() throws XMLWeaverException {
		final Element element = (Element) doc.getDocumentElement()
				.getElementsByTagName("edition").item(0);
		validator.validateElement(element);
	}

	/**
	 * Test method for {@link IDOMValidator#validateDocument(Document)}. The
	 * document is invalid, an {@link XMLWeaverException} must be thrown at
	 * validating.
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testValidateDocumentNegative() throws XMLWeaverException {
		final Element element = (Element) doc.getDocumentElement()
				.getElementsByTagName("author").item(0);
		element.removeChild(element.getElementsByTagName("name").item(0));
		validator.validateDocument(element.getOwnerDocument());
	}

	/**
	 * Test method for {@link IDOMValidator#validateDocument(Document)}. The
	 * document is invalid, an {@link XMLWeaverException} must be thrown at
	 * validating.
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testValidateDocumentNegativeVal() throws XMLWeaverException {
		final Element element = (Element) docVal.getDocumentElement()
				.getElementsByTagName("author").item(0);
		element.removeChild(element.getElementsByTagName("name").item(0));
		validatorVal.validateDocument(element.getOwnerDocument());

	}
}
