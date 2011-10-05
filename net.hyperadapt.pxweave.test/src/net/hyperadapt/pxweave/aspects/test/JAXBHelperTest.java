package net.hyperadapt.pxweave.aspects.test;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.util.JAXBHelper;

import org.junit.Test;

public class JAXBHelperTest {
	
	
	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.aspects.AspectWeaver#unmarshalAspects(ArrayList aspectFiles)}
	 * .
	 * 
	 * @throws XMLWeaverException
	 * @throws MalformedURLException 
	 */
	@Test
	public void testUnmarshalAspects() throws XMLWeaverException, MalformedURLException {
		final ArrayList<File> aspectFiles = new ArrayList<File>();
		aspectFiles.add(new File("testData/jaxbHelperTest/Aspect1.xml"));
		aspectFiles.add(new File("testData/jaxbHelperTest/Aspect2.xml"));
		for(File file:aspectFiles){
			Aspect aspect = JAXBHelper.unmarshallAspect(file.toURI().toURL());
			assertNotNull(aspect);
		}
		
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.aspects.AspectWeaver#unmarshalAspects(ArrayList aspectFiles)}
	 * . This test method must throw an {@link XMLWeaverException} because an
	 * aspect file is not found.
	 * 
	 * @throws XMLWeaverException
	 * @throws MalformedURLException 
	 */
	@Test(expected = XMLWeaverException.class)
	public void testUnmarshalAspectsAspectNotFound() throws XMLWeaverException, MalformedURLException {	
		File aspectFile = new File("testData/jaxbHelperTest/Aspect3.xml");
		JAXBHelper.unmarshallAspect(aspectFile.toURI().toURL());
	}

	/**
	 * Test method for
	 * {@link net.hyperadapt.pxweave.aspects.AspectWeaver#unmarshalAspects(ArrayList aspectFiles)}
	 * . This test method must throw an {@link XMLWeaverException} because an
	 * aspect file is not valid.
	 * 
	 * @throws XMLWeaverException
	 * @throws MalformedURLException 
	 */
	@Test(expected = XMLWeaverException.class)
	public void testUnmarshalAspectsAspectInvalid() throws XMLWeaverException, MalformedURLException {
		File aspectFile = new File("testData/jaxbHelperTest/AspectInvalid.xml");
		Aspect aspect = JAXBHelper.unmarshallAspect(aspectFile.toURI().toURL());
		assertNotNull(aspect);
	}
}
