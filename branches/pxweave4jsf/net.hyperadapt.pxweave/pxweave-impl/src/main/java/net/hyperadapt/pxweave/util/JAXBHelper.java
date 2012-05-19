package net.hyperadapt.pxweave.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.config.ast.WeaverConfiguration;
import net.hyperadapt.pxweave.context.ast.Context;

public class JAXBHelper {
	
	public static final String ASPECT_AST_PACKAGE = "net.hyperadapt.pxweave.aspects.ast";
	public static final String CONFIG_AST_PACKAGE = "net.hyperadapt.pxweave.config.ast";
	public static final String CONTEXT_AST_PACKAGE = "net.hyperadapt.pxweave.context.ast";
	
	@SuppressWarnings("unchecked")
	public static Object unmarshallObject(String contextName, URL resourceURL) throws XMLWeaverException{
		try{
			//TODO Use JAXBs unmarshaller schema validation
			JAXBContext context = JAXBContext.newInstance(contextName);
			Unmarshaller loader = context.createUnmarshaller();
			Object o = loader.unmarshal(resourceURL);
			if(o!=null){
				return o instanceof JAXBElement?((JAXBElement<Object>)o).getValue():o;
			}
		}
		catch (JAXBException e) {
			throw new XMLWeaverException("Could not load content from '"+resourceURL.toString()+"'. Check if the input is valid.",e);
		}
		return null;
	}
	
	public static Object unmarshallObject(String contextName, URI resourceURI) throws XMLWeaverException{
		try{
			return unmarshallObject(contextName, resourceURI.toURL());
		}
		catch(MalformedURLException e){
			throw new XMLWeaverException(e);
		} 
	}
	
	public static Aspect unmarshallAspect(URL aspectURL) throws XMLWeaverException{
		try{	
			Object o = unmarshallObject(ASPECT_AST_PACKAGE,aspectURL);
			if(o!=null)
				return (Aspect)o;
		}
		catch(ClassCastException e){
			throw new XMLWeaverException("Content is not of type '"+Aspect.class.getName()+".",e);
		}
		return null;
	}
	
	public static WeaverConfiguration unmarshallConfig(URL configURL) throws XMLWeaverException{
		try{	
			Object o = unmarshallObject(CONFIG_AST_PACKAGE,configURL);
			if(o!=null)
				return (WeaverConfiguration)o;
		}
		catch(ClassCastException e){
			throw new XMLWeaverException("Content is not of type '"+WeaverConfiguration.class.getName()+".",e);
		}
		return null;
	}
	
	public static Context unmarshallContext(URL contextURL) throws XMLWeaverException{
		try{	
			Object o = unmarshallObject(CONTEXT_AST_PACKAGE,contextURL);
			if(o!=null)
				return (Context)o;
		}
		catch(ClassCastException e){
			throw new XMLWeaverException("Content is not of type '"+Context.class.getName()+".",e);
		}
		return null;
	}
	
}
