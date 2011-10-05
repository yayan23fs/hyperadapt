package net.hyperadapt.pxweave.aspects.test;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigInteger;
import java.net.MalformedURLException;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.context.DefaultWeavingContext;
import net.hyperadapt.pxweave.context.IWeavingContext;
import net.hyperadapt.pxweave.context.ast.Context;
import net.hyperadapt.pxweave.util.JAXBHelper;

import org.junit.Test;

/**
 * @author msteinfeldt
 * 
 */
public class ContextEvaluatorTest {


	@Test
	public void testGetWeavingContext() throws MalformedURLException, XMLWeaverException {
		File contextFile = new File("testData/contextEvaluatorTest/context.xml");
		Context context = JAXBHelper.unmarshallContext(contextFile.toURI().toURL());
		assertNotNull(context);
		IWeavingContext weavingContext = new DefaultWeavingContext(context);
		
		Object o = weavingContext.getParameterValue("deviceType");
		assertTrue("Parameter was set with expected value", "cellPhone"
				.contentEquals((String) o));
		 o = weavingContext.getParameterValue("hres");
		assertEquals("Parameter was set with expected value", BigInteger
				.valueOf(800), o);
		assertTrue(!weavingContext.isMissingValue("deviceType","hres","vres"));

	}
}
