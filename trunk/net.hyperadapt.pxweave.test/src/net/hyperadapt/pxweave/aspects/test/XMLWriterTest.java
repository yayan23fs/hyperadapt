/**
 * 
 */
package net.hyperadapt.pxweave.aspects.test;

import static org.junit.Assert.*;

import java.io.File;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.XMLWriter;
import net.hyperadapt.pxweave.interpreter.IInterpreterArgument;
import net.hyperadapt.pxweave.interpreter.InterpreterArgument;
import net.hyperadapt.pxweave.util.DOMOperations;
import net.hyperadapt.pxweave.validation.DOML3Parser;
import net.hyperadapt.pxweave.validation.IDOMParser;

import org.junit.Test;
import org.w3c.dom.Document;

/**
 * @author martin
 * 
 */
public class XMLWriterTest {

	/**
	 * Test method for {@link net.hyperadapt.pxweave.XMLWriter#getInstance()}.
	 */
	@Test
	public void testGetInstance() {
		final XMLWriter writer = XMLWriter.getInstance();
		final XMLWriter writer2 = XMLWriter.getInstance();
		assertEquals(
				"getInstance should return a reference to the same XMLWriter",
				writer, writer2);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.XMLWriter#writeToFile(org.w3c.dom.Document, java.lang.String, java.lang.String, java.lang.Boolean)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testWriteToFile() throws XMLWeaverException {
		IDOMParser parser = new DOML3Parser();
		//final Document document = parser.buildDOM((new File("testData/book.xml")).toURI());
		final XMLWriter xmlwWriter = XMLWriter.getInstance();
		
		IInterpreterArgument argument = new InterpreterArgument(new File("testData/book.xml"), new File("testData/result.xml") , "1");
		argument.loadDocument(null, false);
		xmlwWriter.write(argument, false);
		final Document doc = parser.buildDOM((new File("testData/result.xml")).toURI());
		assertTrue(DOMOperations.convertNodeToString(doc).contentEquals(
				DOMOperations.convertNodeToString(argument.getDocument())));
	}

}
