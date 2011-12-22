/**
 * 
 */
package net.hyperadapt.pxweave.validation.test;

import java.io.File;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.validation.ValidationMode;
import net.hyperadapt.pxweave.validation.IDOMFactory;
import net.hyperadapt.pxweave.validation.IDOMParser;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * @author martin
 * 
 */
public class ParserTest {
	private final IDOMFactory dfVal = ValidationMode.createDOM3ValidationAPIFactory();
	private final IDOMFactory df = ValidationMode.createDOM3ValidationAPIFactory();
	private IDOMParser parser;
	private IDOMParser parserVal;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		parserVal = dfVal.createDOMParser();
		parser = df.createDOMParser();
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.validation.domL3.IDOMParser#buildDOM(java.lang.String, java.lang.String)}
	 * . The parsed document is invalid, an {@link XMLWeaverException} must be
	 * thrown at parsing.
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testInvalidDocumentValParsing() throws XMLWeaverException {
		@SuppressWarnings("unused")
		final Document documentVal3 = parserVal.buildDOM(
				(new File("testData/bookInvalid.xml")).toURI(),(new File("testData/book.xsd")).toURI());
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.validation.domL3.IDOMParser#buildDOM(java.lang.String, java.lang.String)}
	 * . The parsed document is invalid, an {@link XMLWeaverException} must be
	 * thrown at parsing.
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testInvalidDocumentParsing() throws XMLWeaverException {
		@SuppressWarnings("unused")
		final Document document = parser.buildDOM(
				(new File("testData/bookInvalid.xml")).toURI(), (new File("testData/book.xsd")).toURI());
	}

}
