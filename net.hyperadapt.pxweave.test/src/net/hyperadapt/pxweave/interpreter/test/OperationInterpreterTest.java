/**
 * 
 */
package net.hyperadapt.pxweave.interpreter.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.hyperadapt.pxweave.Environment;
import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.evaluation.XPathEvaluator;
import net.hyperadapt.pxweave.interpreter.OperationInterpreter;
import net.hyperadapt.pxweave.validation.DOML3Parser;
import net.hyperadapt.pxweave.validation.IDOMParser;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author msteinfeldt 
 * 
 */
public class OperationInterpreterTest {
	private Document document;
	private final OperationInterpreter oInterpreter = new OperationInterpreter();
	private ArrayList<Element> testElements1;
	@SuppressWarnings("unused")
	private ArrayList<Element> testElements0;
	private ArrayList<Element> testElements3;

	@SuppressWarnings("unused")
	private ArrayList<Text> testText1;
	@SuppressWarnings("unused")
	private ArrayList<Text> testText0;
	private ArrayList<Text> testText3;
	
	private XPathEvaluator xPathEvaluator;
	private IDOMParser parser;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		parser = new DOML3Parser();
		document = parser.buildDOM((new File("testData/book.xml")).toURI());
	
		xPathEvaluator = new XPathEvaluator(new Environment.NSContext(){
			{
				this.getNamespaces().put("books", URI.create("http://books"));
				this.getDefinitions().put(URI.create("http://books"),(new File("testData/book.xsd")).toURI());
			}
			
		});
		testElements0 = new ArrayList<Element>();

		testElements1 = new ArrayList<Element>() {
			private static final long serialVersionUID = 1L;
			{
				add(document.createElement("test0"));
			}
		};

		testElements3 = new ArrayList<Element>() {
			private static final long serialVersionUID = 1L;
			{
				add(document.createElement("test0"));
				add(document.createElement("test1"));
				add(document.createElement("test2"));
			}
		};

		testText0 = new ArrayList<Text>();
		testText1 = new ArrayList<Text>() {
			private static final long serialVersionUID = 1L;
			{
				add(document.createTextNode("text0"));
			}
		};

		testText3 = new ArrayList<Text>() {
			private static final long serialVersionUID = 1L;
			{
				add(document.createTextNode("text0"));
				add(document.createTextNode("text1"));
				add(document.createTextNode("text2"));
			}
		};
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpretChangeOrder(org.w3c.dom.NodeList, java.lang.String)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInterpretChangeOrder() throws XMLWeaverException {
		final NodeList joinPoints = xPathEvaluator.evaluateXPath(
				"//books:book[3]/books:author", document);

		String permutation = "(2 3 1)";
		oInterpreter.interpretChangeOrder(joinPoints, permutation);
		final String[] nodes = new String[3];
		NodeList result = xPathEvaluator.evaluateXPath("//books:book[3]/books:author",
				document).item(0).getChildNodes();
		nodes[0] = result.item(0).getNodeName();
		nodes[1] = result.item(1).getNodeName();
		nodes[2] = result.item(2).getNodeName();
		final String[] expected = new String[3];
		expected[0] = "lastName";
		expected[1] = "#text";
		expected[2] = "name";
		assertArrayEquals("the children's order was changed correctly",
				expected, nodes);

		permutation = "(3 1 2)";
		oInterpreter.interpretChangeOrder(joinPoints, permutation);
		result = xPathEvaluator.evaluateXPath("//books:book[3]/books:author", document)
				.item(0).getChildNodes();
		nodes[0] = result.item(0).getNodeName();
		nodes[1] = result.item(1).getNodeName();
		nodes[2] = result.item(2).getNodeName();
		expected[2] = "lastName";
		expected[0] = "#text";
		expected[1] = "name";

		assertArrayEquals("the children's order was changed correctly",
				expected, nodes);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpretChangeOrder(org.w3c.dom.NodeList, java.lang.String)}
	 * . This test method must throw an {@link XMLWeaverException} because the
	 * given permutation is incorrect, since the new position for the node at
	 * position 2 exceeds the number of elements of the permutation
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testInterpretChangeOrderWrongPosition()
			throws XMLWeaverException {
		final NodeList joinPoints = xPathEvaluator.evaluateXPath(
				"//books:book[3]/books:author", document);
		final String permutation = "(2 4 1)";
		oInterpreter.interpretChangeOrder(joinPoints, permutation);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpretChangeOrder(org.w3c.dom.NodeList, java.lang.String)}
	 * . This test method must throw an {@link XMLWeaverException} because the
	 * given permutation is incorrect, since the number of provided positions is
	 * smaller than the total amount of elements that have to be ordered.
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testInterpretChangeOrderIncomplete() throws XMLWeaverException {
		final NodeList joinPoints = xPathEvaluator.evaluateXPath(
				"//books:book[3]/books:author", document);
		final String permutation = "(2 1)";
		oInterpreter.interpretChangeOrder(joinPoints, permutation);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpretChangeOrder(org.w3c.dom.NodeList, java.lang.String)}
	 * . This test method must throw an {@link XMLWeaverException} because the
	 * given permutation is incorrect, since there is a duplicate mapping to a
	 * position
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testInterpretChangeOrderDuplicate() throws XMLWeaverException {
		final NodeList joinPoints = xPathEvaluator.evaluateXPath(
				"//books:book[3]/books:author", document);
		final String permutation = "(2 2 1)";
		oInterpreter.interpretChangeOrder(joinPoints, permutation);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteChangeValue(org.w3c.dom.NodeList, java.util.ArrayList)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInterpreteChangeValue() throws XMLWeaverException {
		NodeList joinPoints = xPathEvaluator.evaluateXPath("//books:book/books:author",
				document);
		oInterpreter.interpreteChangeValue(joinPoints, testText3);
		assertTrue(
				"the selected elements have a textNode with the corresponding value",
				xPathEvaluator.evaluateXPath("//books:book/books:author/text()", document)
						.item(2).getNodeValue().contentEquals("text2"));
		assertTrue("old textContent was removed", xPathEvaluator.evaluateXPath(
				"//books:book/books:author/text()", document).item(2).getNodeValue()
				.contains("mixedContent") == false);
		joinPoints = xPathEvaluator.evaluateXPath("//books:book/attribute::inStock",
				document);
		oInterpreter.interpreteChangeValue(joinPoints, testText3);

		assertTrue("the attribute was changed", xPathEvaluator.evaluateXPath(
				"//books:book[3]/attribute::inStock", document).item(0)
				.getNodeValue().contentEquals("text2"));
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteChooseVariant(org.w3c.dom.NodeList)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInterpreteChooseVariant() throws XMLWeaverException {
		final NodeList joinPoints = xPathEvaluator.evaluateXPath(
				"//books:book[1]", document);
		oInterpreter.interpreteChooseVariant(joinPoints);
		assertTrue("only one variant should be left",
				xPathEvaluator.evaluateXPath("//books:book", document)
						.getLength() == 1);
		assertTrue("the variant left should be the right one",
				(xPathEvaluator.evaluateXPath("//books:book/books:author/books:name", document)
						.item(0).getTextContent().contentEquals("Sam")));
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteCollapseElement(org.w3c.dom.NodeList, java.util.ArrayList)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInterpreteCollapseElement() throws XMLWeaverException {
		final NodeList joinPoints = xPathEvaluator.evaluateXPath(
				"//books:book/books:title", document);
		oInterpreter.interpreteCollapseElement(joinPoints, testText3);
		assertTrue("the nodes have a textNode", xPathEvaluator.evaluateXPath(
				"//books:book/books:title/text()", document).getLength() == 3);
		assertTrue("the nodes have no other child left", xPathEvaluator
				.evaluateXPath("//books:book/books:title/*[2]", document).getLength() == 0);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteDelete(org.w3c.dom.NodeList)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInterpreteDelete() throws XMLWeaverException {
		NodeList nodeList = xPathEvaluator.evaluateXPath(
				"//books:book/attribute::inStock", document);
		oInterpreter.interpreteDelete(nodeList);
		assertTrue("attribute was removed", (xPathEvaluator.evaluateXPath(
				"//books:book/attribute::inStock", document)).getLength() == 0);
		nodeList = xPathEvaluator
				.evaluateXPath("//books:book/books:title/text()", document);
		oInterpreter.interpreteDelete(nodeList);
		assertTrue("textNodes are removed", (xPathEvaluator.evaluateXPath(
				"//books:book/books:title/text()", document)).getLength() == 0);
		nodeList = xPathEvaluator.evaluateXPath("//books:book[1]/books:title", document);
		oInterpreter.interpreteDelete(nodeList);
		assertTrue(
				"no nodes of type \"title\" should be left in the first book",
				(xPathEvaluator.evaluateXPath("//books:book[1]/books:title", document))
						.getLength() == 0);
		nodeList = xPathEvaluator.evaluateXPath("//books:book", document);
		oInterpreter.interpreteDelete(nodeList);
		assertTrue("no books left", (xPathEvaluator.evaluateXPath("//books:book",
				document)).getLength() == 0);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteEnrichContent(org.w3c.dom.NodeList, int, java.util.ArrayList)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInterpreteEnrichContent() throws XMLWeaverException {
		final NodeList joinPoints = xPathEvaluator.evaluateXPath("//books:book",
				document);
		final int position = -1;
		final ArrayList<Text> valueList = new ArrayList<Text>() {
			private static final long serialVersionUID = 1L;
			{
				add(document.createTextNode("data"));
				add(document.createTextNode("data"));
				add(document.createTextNode("data"));
			}
		};
		oInterpreter.interpreteEnrichContent(joinPoints, position, valueList);
		final NodeList result = xPathEvaluator
				.evaluateXPath("//books:book", document);
		assertTrue("every book should now have a textNode",
				result.getLength() == joinPoints.getLength());
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteEnrichContent(org.w3c.dom.NodeList, int, java.util.ArrayList)}
	 * . The {@link IndexOutOfBoundsException} is thrown because any joinPoint
	 * needs a textNode
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = IndexOutOfBoundsException.class)
	public void testInterpreteEnrichContentNotEnoughArgs()
			throws XMLWeaverException {
		final NodeList joinPoints = xPathEvaluator.evaluateXPath("//books:book",
				document);
		final int position = -1;
		final ArrayList<Text> valueList = new ArrayList<Text>() {
			private static final long serialVersionUID = 1L;
			{
				add(document.createTextNode("data"));
				add(document.createTextNode("data"));
			}
		};
		oInterpreter.interpreteEnrichContent(joinPoints, position, valueList);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteEnrichContent(org.w3c.dom.NodeList, int, java.util.ArrayList)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 *             because the enriched nodes have to be elementNodes
	 */
	@Test(expected = XMLWeaverException.class)
	public void testInterpreteEnrichContentWrongNodeType()
			throws XMLWeaverException {
		final NodeList joinPoints = xPathEvaluator.evaluateXPath(
				"//books:book[1]/books:title/text()", document);
		final int position = -1;
		final ArrayList<Text> valueList = new ArrayList<Text>() {
			private static final long serialVersionUID = 1L;
			{
				add(document.createTextNode("data"));
			}
		};
		oInterpreter.interpreteEnrichContent(joinPoints, position, valueList);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteExpandElement(org.w3c.dom.NodeList, java.util.ArrayList)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInterpreteExpandElement() throws XMLWeaverException {
		final NodeList joinPoints = xPathEvaluator.evaluateXPath(
				"//books:book/books:title", document);
		oInterpreter.interpreteExpandElement(joinPoints, testElements3);
		assertTrue("no textNodes are left", (xPathEvaluator.evaluateXPath(
				"//books:book/books:title/text()", document).getLength() == 0));
		assertTrue("the nodes have a new child", (xPathEvaluator.evaluateXPath(
				"//books:book/books:title/*[1]", document).getLength() == 3));
		assertTrue("the node's name must be test2",
				(xPathEvaluator
						.evaluateXPath("//books:book[3]/books:title/test2", document)
						.getLength() == 1));
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteFillComponentByID(java.lang.String, java.lang.String, org.w3c.dom.NodeList, java.util.List)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInterpreteFillComponentByID() throws XMLWeaverException {
		final Document listTarget = parser.buildDOM(URI.create("testData/bookList.xml"));
		final Document bookSource = parser.buildDOM(URI.create("testData/book.xml"));
		final NodeList joinPoints = xPathEvaluator.evaluateXPath("//book",
				listTarget);
		final NodeList sourceComps = xPathEvaluator.evaluateXPath("//books:book",
				bookSource);

		final List<Element> sourceComponents = new ArrayList<Element>();
		sourceComponents.add((Element) sourceComps.item(0));
		sourceComponents.add((Element) sourceComps.item(1));
		sourceComponents.add((Element) sourceComps.item(2));

		oInterpreter.interpreteFillComponentByID("price", "price", joinPoints,
				sourceComponents);
		NodeList result = xPathEvaluator.evaluateXPath("//book//books:name/text()",
				listTarget);
		assertTrue("JoinPoints are filled with the correct nodes", (result
				.item(0).getNodeValue().contentEquals("Sam") & result.item(1)
				.getNodeValue().contentEquals("M."))
				& result.item(2).getNodeValue().contentEquals("A"));
		oInterpreter.interpreteFillComponentByID("price", "price", joinPoints,
				sourceComponents);
		result = xPathEvaluator.evaluateXPath("//book//books:name/text()", listTarget);
		assertTrue("Already existing nodes in joinPoints are kept", (result
				.item(0).getNodeValue().contentEquals("Sam") & result.item(1)
				.getNodeValue().contentEquals("Sam"))
				& result.item(2).getNodeValue().contentEquals("M."));
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteFillComponentByID(java.lang.String, java.lang.String, org.w3c.dom.NodeList, java.util.List)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testInterpreteFillComponentByIDMissingSource()
			throws XMLWeaverException {
		final Document listTarget = parser.buildDOM(URI.create("testData/bookList.xml"));
		final Document source = parser.buildDOM(URI.create("testData/book.xml"));
		final NodeList joinPoints = xPathEvaluator.evaluateXPath("//book",
				listTarget);
		final NodeList sourceComps = xPathEvaluator.evaluateXPath("//books:book",
				source);

		final List<Element> sourceComponents = new ArrayList<Element>();
		sourceComponents.add((Element) sourceComps.item(0));
		sourceComponents.add((Element) sourceComps.item(1));

		oInterpreter.interpreteFillComponentByID("price", "price", joinPoints,
				sourceComponents);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteFillComponentByID(java.lang.String, java.lang.String, org.w3c.dom.NodeList, java.util.List)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testInterpreteFillComponentByIDDuplicateID()
			throws XMLWeaverException {
		final Document listTarget = parser.buildDOM(URI.create("testData/bookList.xml"));
		final Document source = parser.buildDOM(URI.create("testData/book.xml"));
		final NodeList joinPoints = xPathEvaluator.evaluateXPath("//book",
				listTarget);
		final NodeList sourceComps = xPathEvaluator.evaluateXPath("//books:book",
				source);

		final List<Element> sourceComponents = new ArrayList<Element>();
		sourceComponents.add((Element) sourceComps.item(0));
		sourceComponents.add((Element) sourceComps.item(1));
		sourceComponents.add((Element) sourceComps.item(1));

		oInterpreter.interpreteFillComponentByID("price", "price", joinPoints,
				sourceComponents);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteFillComponentByID(java.lang.String, java.lang.String, org.w3c.dom.NodeList, java.util.List)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testInterpreteFillComponentByIDSameNode()
			throws XMLWeaverException {
		final Document target = parser.buildDOM(URI.create("testData/bookList.xml"));
		final Document source = target;
		final NodeList joinPoints = xPathEvaluator.evaluateXPath("//book",
				target);
		final NodeList sourceComps = xPathEvaluator.evaluateXPath("//book",
				source);

		final List<Element> sourceComponents = new ArrayList<Element>();
		sourceComponents.add((Element) sourceComps.item(0));
		sourceComponents.add((Element) sourceComps.item(1));
		sourceComponents.add((Element) sourceComps.item(2));
		oInterpreter.interpreteFillComponentByID("price", "price", joinPoints,
				sourceComponents);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteInsertElement(org.w3c.dom.NodeList, java.util.ArrayList, int)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInterpreteInsertElement() throws XMLWeaverException {
		NodeList joinPoints = xPathEvaluator.evaluateXPath("//books:book/books:title[1]",
				document);
		final int lengthOld = joinPoints.item(0).getChildNodes().getLength();
		oInterpreter.interpreteInsertElement(joinPoints, testElements3, 0);
		assertTrue(joinPoints.item(1).getChildNodes().item(0).getNodeName()
				.contentEquals("test1"));
		assertTrue(joinPoints.item(2).getChildNodes().item(0).getNodeName()
				.contentEquals("test2"));
		assertTrue("exactly one Node has to be added", joinPoints.item(0)
				.getChildNodes().getLength() - 1 == lengthOld);
		joinPoints = xPathEvaluator.evaluateXPath("//books:book[1]", document);
		oInterpreter.interpreteInsertElement(joinPoints, testElements1, 0);
		assertTrue("inserted element must be at position 0", joinPoints.item(0)
				.getChildNodes().item(0).getNodeName().contentEquals("test0"));
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteInsertElement(org.w3c.dom.NodeList, java.util.ArrayList, int)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test(expected = XMLWeaverException.class)
	public void testInterpreteInsertElementNoElement()
			throws XMLWeaverException {
		final NodeList joinPoints = xPathEvaluator.evaluateXPath(
				"//books:book/books:title[1]/text()", document);
		oInterpreter.interpreteInsertElement(joinPoints, testElements1, -1);
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteMoveElement(org.w3c.dom.Node, org.w3c.dom.Node, int)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInterpreteMoveElement() throws XMLWeaverException {
		Node joinPoint = xPathEvaluator.evaluateXPath("//books:book[1]/books:title",
				document).item(0);
		final Node target = xPathEvaluator.evaluateXPath("//books:book[2]/books:title",
				document).item(0);
		final int lengthOld = target.getChildNodes().getLength();
		oInterpreter.interpreteMoveElement(joinPoint, target, 1);
		joinPoint = xPathEvaluator.evaluateXPath("//books:book[1]/books:title", document)
				.item(0);
		assertTrue("target should have one more child now", target
				.getChildNodes().getLength() == lengthOld + 1);
		assertNull("source should be null now", xPathEvaluator.evaluateXPath(
				"//books:book[1]/books:title", document).item(0));
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.interpreter.test.OperationInterpreter#interpreteReduceContent(org.w3c.dom.NodeList, java.lang.String)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testInterpreteReduceContent() throws XMLWeaverException {
		final NodeList joinPoints = xPathEvaluator.evaluateXPath(
				"//books:book/books:publisher", document);
		oInterpreter.interpreteReduceContent(joinPoints, "Media");
		assertTrue("", joinPoints.item(0).getFirstChild().getNodeValue()
				.contentEquals("VVM"));
		oInterpreter.interpreteReduceContent(joinPoints, "");
		assertTrue("no textNode should be left", joinPoints.item(0)
				.getChildNodes().getLength() == 0);
		oInterpreter.interpreteReduceContent(joinPoints, null);
		assertTrue("no textNode should be left", joinPoints.item(0)
				.getChildNodes().getLength() == 0);

	}

}
