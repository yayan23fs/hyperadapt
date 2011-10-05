/**
 * 
 */
package net.hyperadapt.pxweave.interpreter.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.AspectOrderer;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.util.JAXBHelper;

import org.junit.Before;
import org.junit.Test;

/**
 * @author martin
 * 
 */
public class AspectOrdererTest {

	private List<Aspect> aspects = new ArrayList<Aspect>();
	//ArrayList<File> aspectFiles = new ArrayList<File>();

	public static Integer getAspectPosition(final List<Aspect> aspects,
			final String aspectName) {
		for (int i = 0; i < aspects.size(); i++) {
			if (aspects.get(i).getName().contentEquals(aspectName)) {
				return i;
			}
		}
		return null;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		aspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect1.xml").toURI().toURL()));
		aspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect2.xml").toURI().toURL()));
		aspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect3.xml").toURI().toURL()));
		aspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect4.xml").toURI().toURL()));
		aspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect5.xml").toURI().toURL()));
	}

	/**
	 * Test method for
	 * {@link net.net.hyperadapt.pxweave.aspects.AspectOrderer#aspectOrdererTest()}.
	 * 
	 * @throws XMLWeaverException
	 */
	@Test
	public void testOrderAspects() throws XMLWeaverException {
		final ArrayList<Aspect> oneAspect = new ArrayList<Aspect>();
		oneAspect.add(aspects.get(0));
		assertTrue("testing boundaries:1 aspect", AspectOrderer.orderAspects(oneAspect).size() == 1);
		List<Aspect> result = AspectOrderer.orderAspects(aspects);
		assertTrue("Aspect3 should be last", result.get(result.size() - 1)
				.getName().contentEquals("aspect3"));
		assertTrue("Aspect4 should be first", result.get(0).getName()
				.contentEquals("aspect4"));
		assertTrue("Aspect5 should be before Aspect1", AspectOrdererTest
				.getAspectPosition(result, "aspect5") < AspectOrdererTest
				.getAspectPosition(result, "aspect1"));
		assertTrue("Aspect2 should be after Aspect5", AspectOrdererTest
				.getAspectPosition(result, "aspect5") < AspectOrdererTest
				.getAspectPosition(result, "aspect2"));
	}

	/**
	 * Test method for
	 * {@link net.net.hyperadapt.pxweave.aspects.AspectOrderer#aspectOrdererTest()}. Must throw
	 * an {@link XMLWeaverException} since aspect6 is declared to be weaved
	 * after "aspectX", which is missing
	 * @throws MalformedURLException 
	 */
	@Test(expected = XMLWeaverException.class)
	public void testOrderAspectsMissingAspect() throws XMLWeaverException, MalformedURLException {
		List<Aspect> localAspects = new LinkedList<Aspect>(aspects); 
		localAspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect6.xml").toURI().toURL()));
		AspectOrderer.orderAspects(localAspects);
	}

	/**
	 * Test method for
	 * {@link net.net.hyperadapt.pxweave.aspects.AspectOrderer#orderAspects()}. An
	 * {@link XMLWeaverException} must be thrown, since both aspects are
	 * declared as last Aspects
	 * @throws MalformedURLException 
	 */
	@Test(expected = XMLWeaverException.class)
	public void testOrderAspectsMultipleLast() throws XMLWeaverException, MalformedURLException {
		List<Aspect> localAspects = new LinkedList<Aspect>(); 
		localAspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect3.xml").toURI().toURL()));
		localAspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect7.xml").toURI().toURL()));
		AspectOrderer.orderAspects(localAspects);
	}

	/**
	 * Test method for
	 * {@link net.net.hyperadapt.pxweave.aspects.AspectOrderer#orderAspects()}. An
	 * {@link XMLWeaverException} must be thrown, since both aspects are
	 * declared as first Aspects
	 * @throws MalformedURLException 
	 */
	@Test(expected = XMLWeaverException.class)
	public void testOrderAspectsMultipleFirst() throws XMLWeaverException, MalformedURLException {
		List<Aspect> localAspects = new LinkedList<Aspect>(); 
		localAspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect4.xml").toURI().toURL()));
		localAspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect10.xml").toURI().toURL()));
		AspectOrderer.orderAspects(localAspects);
	}

	/**
	 * Test method for
	 * {@link net.net.hyperadapt.pxweave.aspects.AspectOrderer#orderAspects()}. An
	 * {@link XMLWeaverException} must be thrown, since aspect8 is declared to
	 * be before aspect1 and as the last aspect.
	 * @throws MalformedURLException 
	 */
	@Test(expected = XMLWeaverException.class)
	public void testOrderAspectsLastAndBefore() throws XMLWeaverException, MalformedURLException {
		List<Aspect> localAspects = new LinkedList<Aspect>(); 
		localAspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect1.xml").toURI().toURL()));
		localAspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect8.xml").toURI().toURL()));
		AspectOrderer.orderAspects(localAspects);
	}

	/**
	 * Test method for
	 * {@link net.net.hyperadapt.pxweave.aspects.AspectOrderer#orderAspects()}. An
	 * {@link XMLWeaverException} must be thrown, since aspect9 is declared to
	 * be after aspect1 and to be the first aspect.
	 * @throws MalformedURLException 
	 */
	@Test(expected = XMLWeaverException.class)
	public void testOrderAspectsFirstAndAfter() throws XMLWeaverException, MalformedURLException {
		List<Aspect> localAspects = new LinkedList<Aspect>(); 
		localAspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect1.xml").toURI().toURL()));
		localAspects.add(JAXBHelper.unmarshallAspect(new File("testData/aspectOrdererTest/Aspect9.xml").toURI().toURL()));
		AspectOrderer.orderAspects(localAspects);

	}
}
