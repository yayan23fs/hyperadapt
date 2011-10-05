package net.hyperadapt.pxweave;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

import org.apache.log4j.Logger;

import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.aspects.AspectOrderer;
import net.hyperadapt.pxweave.aspects.WildcardJoinpoint;
import net.hyperadapt.pxweave.config.ast.NamespaceDeclaration;
import net.hyperadapt.pxweave.config.ast.WeaverConfiguration;
import net.hyperadapt.pxweave.context.DefaultWeavingContext;
import net.hyperadapt.pxweave.context.IWeavingContext;
import net.hyperadapt.pxweave.context.ast.Context;
import net.hyperadapt.pxweave.interactions.patternmatrix.ConflictMatrix;
import net.hyperadapt.pxweave.interactions.runtime.DynamicAnalysis;
import net.hyperadapt.pxweave.logger.LoggerConfiguration;
import net.sf.saxon.om.NamespaceResolver;

import net.hyperadapt.pxweave.util.JAXBHelper;
import net.hyperadapt.pxweave.validation.ValidationMode;


/**
 * The environment provides all information the AspectWeaver needs to be executed such
 * as the aspects, configuration objects, the static context model, the namespace context
 * and validation mode.
 * <br>
 * It also acts as a mediator for state updates at runtime, e.g., if the weaver is executed
 * in a pipeline, the pipeline informs the environment about changes by setting a new execution
 * state object. 
 * 
 * @author skarol
 *
 */
public class Environment implements IEnvironment {
	
	public static final URI DEFAULT_CONFIG_URI = URI.create("weaverConfig.xml");
	public static final URI DEFAULT_CONTEXT_URI = URI.create("context.xml");
	public static final URI DEFAULT_CONFLICT_MATRIX_URI = URI.create("patternConflicts.xml");
	
	private List<Aspect> aspects;
	private WeaverConfiguration configuration;
	private IWeavingContext contextModel;
	private URI baseURI;
	private IPXWeaveNamespaceContext nsContext;
	private ValidationMode validationMode = ValidationMode.None;
	private ConflictMatrix conflictMatrix;
	
	private IExecutionState currentExecutionState = null;
	private List<IExecutionStateObserver> executionStateObservers = new LinkedList<IExecutionStateObserver>();
	private DynamicAnalysis conflictAnalyser = null;
	
	public static synchronized IEnvironment create(URI baseURI)throws XMLWeaverException{
		return create(baseURI,DEFAULT_CONFIG_URI,DEFAULT_CONTEXT_URI,DEFAULT_CONFLICT_MATRIX_URI);
	}
	
	public static synchronized IEnvironment create(URI baseURI, URI configURI, URI contextURI, URI patternConflictsURI) throws XMLWeaverException {
		Logger logger = LoggerConfiguration.instance().getLogger(Main.class);
		checkBaseURI(baseURI);
		URI configURL = configURI.isAbsolute()?configURI:baseURI.resolve(configURI);
		URI contextURL = contextURI.isAbsolute()?contextURI:baseURI.resolve(contextURI);
		URI patternConflictsURL = patternConflictsURI.isAbsolute()?patternConflictsURI:baseURI.resolve(patternConflictsURI);
		
		WeaverConfiguration configuration = null;
		Context context = null;
		ConflictMatrix conflictMatrix = null;
		try {
			logger.info("Loading configuration from '" + configURL.toString() + "'.");
			configuration = JAXBHelper.unmarshallConfig(configURL.toURL());
			logger.info("Loading default context model from '" + contextURL.toString() + "'.");
			context = JAXBHelper.unmarshallContext(contextURL.toURL());
			logger.info("Loading conflict matrix from '" +patternConflictsURL.toString()+"'.");
			conflictMatrix = ConflictMatrix.createMatrix(patternConflictsURL);
		} catch (MalformedURLException e) {
			logger.error("Config URL or Context URL is malformed.",e);
			throw new IllegalArgumentException(e);
		} catch (XMLWeaverException e) {
			logger.error("Error while loading weaver config or default contextmodel.",e);
			throw e;
		}
		
		IWeavingContext weavingContext = new DefaultWeavingContext(context);
		
		if(configuration!=null)
			return create(baseURI,configuration,weavingContext,conflictMatrix);
		return null;
	}
	
	public static synchronized IEnvironment create(URI baseURI, WeaverConfiguration configuration, IWeavingContext context, ConflictMatrix conflictMatrix) throws XMLWeaverException {
		Logger logger = LoggerConfiguration.instance().getLogger(Main.class);
		checkBaseURI(baseURI);
		List<Aspect> aspects = new LinkedList<Aspect>();
		for(String aspectPath:configuration.getAspectFiles().getAspectFile()){
			URI aspectURI = URI.create(aspectPath);
			URI resolvedAspectURI = null;
			if(aspectURI.isAbsolute()){
				resolvedAspectURI = aspectURI;
			}
			else{
				resolvedAspectURI = baseURI.resolve(aspectURI);	
			}
			logger.debug("Loading aspect unit from '" + resolvedAspectURI.toString() + "'");
			try {
				Aspect aspect = JAXBHelper.unmarshallAspect(resolvedAspectURI.toURL());
				aspects.add(aspect);
			} catch (MalformedURLException e) {
				logger.error("Aspect URL is malformed.",e);
				throw new XMLWeaverException("Aspect URL is malformed.",e);
			} catch (XMLWeaverException e) {
				logger.error("Aspect is malformed.",e);
				throw e;
			}
		}
		if(aspects.size()==0)
			logger.info("No aspects loaded.");
		return create(baseURI,configuration,context,conflictMatrix,aspects);
	}
	
	private static synchronized IEnvironment create(URI baseURI, WeaverConfiguration configuration, IWeavingContext context,ConflictMatrix conflictMatrix,List<Aspect> aspects) throws XMLWeaverException{
		Logger logger = LoggerConfiguration.instance().getLogger(Main.class);
		checkBaseURI(baseURI);
		NSContext nsContext = new NSContext();
		if(configuration.getNamespaces()!=null){
			logger.debug("Preparing namespace context from configuration.");
			for(NamespaceDeclaration namespace:configuration.getNamespaces().getNamespace()){
				logger.debug("Adding '" + namespace.getPrefix() + "' - '" + namespace.getName() + "'");
				try{
					URI nsURI = URI.create(namespace.getName());
					nsContext.getNamespaces().put(namespace.getPrefix(),nsURI);
					if(namespace.getDefinition()!=null){
						URI definitionURI = URI.create(namespace.getDefinition());
						if(definitionURI.isAbsolute()){
							nsContext.getDefinitions().put(nsURI,definitionURI);
						}
						else{
							nsContext.getDefinitions().put(nsURI,baseURI.resolve(definitionURI));
						}
					}
				}
				catch(IllegalArgumentException e){
					logger.error("NamespaceURI '" + namespace.getName() + "' is not a valid URI.",e);
					throw e;
				}
			}			
		}
		else{
			logger.info("No namespaces declared in configuration.");
		}

		return create(baseURI,configuration,context,conflictMatrix,aspects,nsContext);
	}
	
	private static synchronized IEnvironment create(URI baseURI, WeaverConfiguration configuration, IWeavingContext context,ConflictMatrix conflictMatrix,List<Aspect> aspects, IPXWeaveNamespaceContext nsContext)throws XMLWeaverException{
		Logger logger = LoggerConfiguration.instance().getLogger(Main.class);
		checkBaseURI(baseURI);
		boolean validateBefore = configuration.getValidationMode().isValidateBeforeWeaving(); 
		boolean validateAfter = configuration.getValidationMode().isValidateAfterWeaving(); 
		String validationModeName = configuration.getValidationMode().getValue().value();
		ValidationMode validationMode;
	
		if (("DOML3").equals(validationModeName)) {
			validationMode = validateBefore?ValidationMode.DomLevel3:ValidationMode.DomLevel3_noInput;
		} 
		else if ("DOML3Validation".equals(validationModeName)) {
			validationMode = validateBefore?ValidationMode.DomLevel3ValidationAPI:ValidationMode.DomLevel3ValidationAPI_noInput;
		} 
		else if ("none".equals(validationModeName)) {
			if(validateBefore && validateAfter){
				validationMode = ValidationMode.NoOperationValidation;
			}
			else if(validateBefore){
				validationMode = ValidationMode.OnlyInput;
			}
			else if(validateAfter){
				validationMode = ValidationMode.OnlyOutput;
			}
			else 
				validationMode = ValidationMode.None;
		}
		else {
			logger.error("Validation mode '" + validationModeName + "' is not known.");
			throw new XMLWeaverException("Unknown validation mode '" + validationModeName + "'.");
		}
		return create(baseURI,configuration,context,conflictMatrix,aspects,nsContext,validationMode);

	}
	
	private static IEnvironment create(URI baseURI,
			WeaverConfiguration configuration, IWeavingContext context, ConflictMatrix conflictMatrix,
			List<Aspect> aspects, IPXWeaveNamespaceContext nsContext,
			 ValidationMode validationMode) throws XMLWeaverException {
		List<Aspect> orderedAspects = AspectOrderer.orderAspects(aspects);
		Environment env = new Environment();
		env.configuration = configuration;
		env.baseURI = baseURI;
		env.contextModel = context;
		env.aspects = orderedAspects;
		env.nsContext = nsContext;
		env.conflictMatrix = conflictMatrix;
		env.conflictAnalyser = new DynamicAnalysis(conflictMatrix);
		if(validationMode!=null)
			env.validationMode = validationMode;
		env.currentExecutionState = new ExecutionState(new WildcardJoinpoint(),env.contextModel,null);
		return env;
	}
	
	public static IEnvironment create(List<Aspect> aspects, IWeavingContext context, IPXWeaveNamespaceContext nsContext, ValidationMode validationMode)throws XMLWeaverException{
		return create(null,null,context,null,aspects,nsContext,validationMode);
	}

	private static void checkBaseURI(URI baseURI){
		Logger logger = LoggerConfiguration.instance().getLogger(Main.class);
		if(!baseURI.isAbsolute()){
			logger.error("Base uri must be absolute, but is '" +baseURI.toString()+ "'.");
			throw new IllegalArgumentException();
		}
	}
	

	
	private Environment(){}
	
	@Override
	public List<Aspect> getAspects() {
		return aspects;
	}

	@Override 
	public WeaverConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public IWeavingContext getContextModel() {
		return contextModel;
	}

	@Override
	public IPXWeaveNamespaceContext getNamespaceContext() {
		return nsContext;
	}


	@Override
	public URI getBaseURI() {
		return baseURI;
	}
	
	
	public ValidationMode getValidationMode(){
		return validationMode;
	}
	
	@Override
	public boolean isValid() {
		return aspects!=null&&nsContext!=null;
	}
	

	@Override
	public IExecutionState getExecutionState() {
		return currentExecutionState;
	}

	@Override
	public boolean registerExecutionStateObserver(IExecutionStateObserver observer) {
		if(!executionStateObservers.contains(observer)){
			return executionStateObservers.add(observer);
		}
		return false;
	}
	

	@Override
	public boolean deRegisterExecutionStateObserver(IExecutionStateObserver observer) {
		return executionStateObservers.remove(observer);
	}

	@Override
	public synchronized void updateExecutionState(IExecutionState state) throws XMLWeaverException {
		IExecutionState oldState = currentExecutionState;
		if(state!=null){
			currentExecutionState = state;
			for(IExecutionStateObserver observer:executionStateObservers){
				observer.notifyStateUpdated(oldState);
			}
		}		
	}
	
	@Override
	public synchronized void beforeExecutionState(IExecutionState state)
			throws XMLWeaverException {
		if(state!=null){
			for(IExecutionStateObserver observer:executionStateObservers){
				observer.notifyBeforeUpdate(state);
			}
		}		
	}
	
	public static class NSContext implements NamespaceContext, NamespaceResolver, IPXWeaveNamespaceContext {
		
		private Map<String,URI> namespaces;
		private Map<URI,URI> definitionURIs;
	
		public NSContext(){
			namespaces = new HashMap<String,URI>();
			definitionURIs = new HashMap<URI,URI>();
		}
		
		public NSContext(Map<String,URI> namespaces){
			this.namespaces = namespaces;
			definitionURIs = new HashMap<URI,URI>();
		}
		
		public NSContext(Map<String,URI> namespaces,Map<URI,URI> definitionURIs){
			this.namespaces = namespaces;
			this.definitionURIs = definitionURIs;
		}
		
		/* (non-Javadoc)
		 * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
		 */
		/* (non-Javadoc)
		 * @see net.hyperadapt.pxweave.IPXWeaveNamespaceContext#getNamespaceURI(java.lang.String)
		 */
		@Override
		public String getNamespaceURI(String prefix) {
			return namespaces.get(prefix).toString();
		}
		
		/* (non-Javadoc)
		 * @see net.hyperadapt.pxweave.IPXWeaveNamespaceContext#getNamespaces()
		 */
		public Map<String,URI> getNamespaces(){
			return namespaces;
		}
		
		/* (non-Javadoc)
		 * @see net.hyperadapt.pxweave.IPXWeaveNamespaceContext#getDefinitions()
		 */
		public Map<URI,URI> getDefinitions(){
			return definitionURIs;
		}
		
		/* (non-Javadoc)
		 * @see net.hyperadapt.pxweave.IPXWeaveNamespaceContext#getDefinitionURI(java.net.URI)
		 */
		public URI getDefinitionURI(URI namespace){
			URI definitionURI = definitionURIs.get(namespace);
			if(definitionURI==null){
				definitionURI = URI.create(namespace.toString()+".xsd");
			}
			return definitionURI;
		}

		/* (non-Javadoc)
		 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
		 */
		/* (non-Javadoc)
		 * @see net.hyperadapt.pxweave.IPXWeaveNamespaceContext#getPrefix(java.lang.String)
		 */
		@Override
		public String getPrefix(String namespaceURI) {
			Set<String> keys = namespaces.keySet();
			for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
				String prefix = iterator.next();
				String uri =  namespaces.get(prefix).toString();
				if (uri.equals(namespaceURI)) {
					return prefix;
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
		 */
		/* (non-Javadoc)
		 * @see net.hyperadapt.pxweave.IPXWeaveNamespaceContext#getPrefixes(java.lang.String)
		 */
		@Override
		public Iterator<String> getPrefixes(String namespaceURI) {
			List<String> prefixes = new LinkedList<String>();
			Set<String> keys = namespaces.keySet();
			for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
				String prefix = iterator.next();
				String uri = namespaces.get(prefix).toString();
				if (uri.equals(namespaceURI)) {
					prefixes.add(prefix);
				}
			}
			return prefixes.iterator();
		}

		/* (non-Javadoc)
		 * @see net.sf.saxon.om.NamespaceResolver#getURIForPrefix(java.lang.String, boolean)
		 */
		/* (non-Javadoc)
		 * @see net.hyperadapt.pxweave.IPXWeaveNamespaceContext#getURIForPrefix(java.lang.String, boolean)
		 */
		@Override
		public String getURIForPrefix(String prefix, boolean useDefault) {
			return this.getNamespaceURI(prefix);
		}

		/* (non-Javadoc)
		 * @see net.sf.saxon.om.NamespaceResolver#iteratePrefixes()
		 */
		/* (non-Javadoc)
		 * @see net.hyperadapt.pxweave.IPXWeaveNamespaceContext#iteratePrefixes()
		 */
		@Override
		public Iterator<String> iteratePrefixes() {
			Set<String> keys = namespaces.keySet();
			return keys.iterator();
		}
	
	}

	@Override
	public ConflictMatrix getConflictMatrix() {
		return conflictMatrix;
	}

	@Override
	public DynamicAnalysis getConflictAnalyser() {
		return conflictAnalyser;
	}

	@Override
	public boolean reportConflicts() {
		return getConfiguration().isReportInteractions()==null?false:getConfiguration().isReportInteractions();
	}
	
}
