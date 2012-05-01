package net.hyperadapt.pxweave.integration.common;

public interface IntegrationConstraints {

	/** statistic evaluation regarding to the selenium test suite (penetration test) **/
	final static boolean WRITESTATISTIC = false;
	
	/** aspect configurations to define the programmatic joinpoint **/
	final static String POSTPROCESSING = "PostProcessing";
	final static String PREPROCESSING = "PreProcessing";
	final static String PXWEAVEADVICE = "PXWeaveAdvice";
	
	/** html encoding for the response **/
	final static String HTML_CONTENTTYPE = "text/html;charset=UTF-8";
	final static String HTML_ENCODING = "UTF-8";
	
	/** request header parameters **/
	final static String HEADERPARAM_ADAPTED = "adapted";
	final static String HEADERPARAM_STREAMHASH = "streamHash";
	final static String HEADERPARAM_XML = "XMLHttpRequest";
	final static String HEADERPARAM_AJAX = "x-requested-with";
	final static String HEADERPARAM_JSF_PARTIAL = "partial/ajax";
	final static String HEADERPARAM_JSF_REQUEST = "faces-request";
	
	/** session attributes **/
	final static String SESSION_PXWEAVE_CONTEXT = "pxweaveContext";
	final static String SESSION_SESSIONID = "sessionId";
	
	/** application parameter **/
	final static String APPLICATION_CONTEXTPATH = "jsfpxweave";
	final static String APPLICATION_CLASSPATH = "java.class.path";
	final static String APPLICATION_FOLDER_BIN = "bin";
	final static String APPLICATION_FOLDER_RESOURCES = "resources";
	final static String APPLICATION_FOLDER_WEBAPPS = "webapps";
	final static String APPLICATION_PARAM_CONTEXT = "$context$";
	final static String APPLICATION_PARAM_PROPFILE = "$propFile$";
	final static String APPLICATION_PROPERTY = "pxweave.properties";
	
	/** integration solutions **/
	final static String INTEGRATION_GENERIC = "genericIntegration";
	final static String INTEGRATION_JSFAOP = "jsfaopIntegration";
	
	/** adaption file properties **/
	final static String FILE_EXTENTION_JSF = ".jsf";
	final static String FILE_EXTENTION_XHTML = ".xhtml";
	final static String FILE_PREFIX_XML = "<?xml";
	final static String FILE_PREFIX_DOCTYPE = "<!DOCTYPE";
	
	/** PX-Weave default configurations **/
	final static String PXWEAVE_DEFAULT_FOLDERNAME = "weavingOperationsTest";
	final static String PXWEAVE_DEFAULT_PATTERNCONFLICTS = "patternConflicts.xml";
	final static String PXWEAVE_DEFAULT_CONTEXT = "context.xml";
	final static String PXWEAVE_DEFAULT_WEAVERCONFIG = "weaverConfiguration.xml";
}
