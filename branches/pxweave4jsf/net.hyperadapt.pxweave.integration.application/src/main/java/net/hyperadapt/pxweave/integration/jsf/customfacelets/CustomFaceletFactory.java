//package net.hyperadapt.pxweave.integration.jsf.customfacelets;
//
//import java.io.BufferedInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.lang.reflect.Constructor;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.net.URL;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.logging.Level;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import javax.el.ExpressionFactory;
//import javax.faces.view.facelets.FaceletException;
//import javax.faces.view.facelets.FaceletHandler;
//import javax.faces.view.facelets.ResourceResolver;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;
//
//import org.xml.sax.SAXException;
//import org.xml.sax.XMLReader;
//
//import com.sun.faces.facelets.Facelet;
//import com.sun.faces.facelets.FaceletCache;
//import com.sun.faces.facelets.compiler.CompilationMessageHolder;
//import com.sun.faces.facelets.compiler.EncodingHandler;
//import com.sun.faces.facelets.compiler.SAXCompiler;
//import com.sun.faces.facelets.impl.DefaultResourceResolver;
//import com.sun.faces.facelets.tag.composite.CompositeLibrary;
//import com.sun.faces.facelets.tag.jsf.core.CoreLibrary;
//import com.sun.faces.facelets.tag.jsf.html.HtmlLibrary;
//import com.sun.faces.facelets.tag.jstl.core.JstlCoreLibrary;
//import com.sun.faces.facelets.tag.jstl.fn.JstlFunction;
//import com.sun.faces.facelets.tag.ui.UILibrary;
//import com.sun.faces.facelets.util.DevTools;
//import com.sun.faces.facelets.util.FunctionLibrary;
//
//public class CustomFaceletFactory extends
//		com.sun.faces.facelets.impl.DefaultFaceletFactory {
//
//	private final URL baseUrl;
//	private final static Pattern XmlDeclaration = Pattern
//			.compile("^<\\?xml.+?version=['\"](.+?)['\"](.+?encoding=['\"]((.+?))['\"])?.*?\\?>");
//	private SAXCompiler compiler;
//	private Object mgr;
//	private CompilationHandler handler;
//
//	@SuppressWarnings("unused")
//	private Map<String, URL> relativeLocations;
//
//	public CustomFaceletFactory() throws IOException {
//		this(CustomFaceletFactory.getCompiler(), new DefaultResourceResolver(),
//				2);
//	}
//
//	public static SAXCompiler getCompiler() {
//		SAXCompiler c = new SAXCompiler();
//		c.setTrimmingComments(false);
//		c.addTagLibrary(new CoreLibrary());
//		c.addTagLibrary(new HtmlLibrary());
//		c.addTagLibrary(new UILibrary());
//		c.addTagLibrary(new JstlCoreLibrary());
//		c.addTagLibrary(new FunctionLibrary(JstlFunction.class,
//				"http://java.sun.com/jsp/jstl/functions"));
//		c.addTagLibrary(new FunctionLibrary(DevTools.class,
//				"http://java.sun.com/mojarra/private/functions"));
//		c.addTagLibrary(new CompositeLibrary());
//
//		return c;
//	}
//
//	public CustomFaceletFactory(final SAXCompiler aCompiler,
//			final ResourceResolver resolver, final long refreshPeriod) {
//		super(aCompiler, resolver, refreshPeriod);
//		baseUrl = resolver.resolveUrl("/");
//		relativeLocations = new ConcurrentHashMap<String, URL>();
//		compiler = aCompiler;
//	}
//
//	public CustomFaceletFactory(final SAXCompiler aCompiler,
//			final ResourceResolver resolver, final long refreshPeriod,
//			final FaceletCache<?> cache) {
//		super(aCompiler, resolver, refreshPeriod, cache);
//		baseUrl = resolver.resolveUrl("/");
//		relativeLocations = new ConcurrentHashMap<String, URL>();
//		compiler = aCompiler;
//	}
//
//	public Facelet getFacelet(final String uri) throws IOException {
//		URL url = this.resolveURL(this.baseUrl, uri);
//		Facelet facelet = createFacelet(url);
//
//		return facelet;
//	}
//
//	private Facelet createFacelet(URL url) throws IOException {
//		if (log.isLoggable(Level.FINE)) {
//			log.fine("Creating Facelet for: " + url);
//		}
//		String escapedBaseURL = Pattern.quote(this.baseUrl.getFile());
//		String alias = '/' + url.getFile().replaceFirst(escapedBaseURL, "");
//		try {
//
//			mgr = getNewCompilationManager(alias);
//			handler = new CompilationHandler(mgr, alias);
//
//			FaceletHandler h = doCompile(url, alias);
//
//			return getNewDefaultFacelet(url, alias, h);
//		} catch (FileNotFoundException fnfe) {
//			throw new FileNotFoundException("Facelet " + alias
//					+ " not found at: " + url.toExternalForm());
//		}
//	}
//
//	protected FaceletHandler doCompile(URL src, String alias)
//			throws IOException {
//
//		InputStream is = null;
//		String encoding = "UTF-8";
//		try {
//			is = new BufferedInputStream(src.openStream(), 1024);
//
//			// PXWeaveHelper pxweave = new PXWeaveHelper(is);
//			// is = pxweave.getAdaptedInputStream();
//
//			encoding = writeXmlDecl(is);
//			SAXParser parser = this.createSAXParser();
//			parser.parse(is, handler);
//
//		} catch (SAXException e) {
//			throw new FaceletException("Error Parsing " + alias + ": "
//					+ e.getMessage(), e.getCause());
//		} catch (ParserConfigurationException e) {
//			throw new FaceletException("Error Configuring Parser " + alias
//					+ ": " + e.getMessage(), e.getCause());
//		} catch (FaceletException e) {
//			throw e;
//		} finally {
//			if (is != null) {
//				is.close();
//			}
//		}
//		Class<?> manager;
//		FaceletHandler result = null;
//		Method method;
//		Object[] nullObjectArray = null;
//		try {
//			manager = Class
//					.forName("com.sun.faces.facelets.compiler.CompilationManager");
//
//			method = manager.getMethod("createFaceletHandler");
//			method.setAccessible(true);
//			FaceletHandler faceletHandler = (FaceletHandler) method.invoke(mgr,
//					nullObjectArray);
//
//			method = manager.getMethod("getCompilationMessageHolder");
//			method.setAccessible(true);
//			CompilationMessageHolder compilationMessageHolder = (CompilationMessageHolder) method
//					.invoke(mgr, nullObjectArray);
//
//			result = new EncodingHandler(faceletHandler, encoding,
//					compilationMessageHolder);
//
//			method = manager.getMethod("setCompilationMessageHolder",
//					CompilationMessageHolder.class);
//			method.setAccessible(true);
//			method.invoke(mgr, compilationMessageHolder);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		}
//		return result;
//	}
//
//	private SAXParser createSAXParser() throws SAXException,
//			ParserConfigurationException {
//		SAXParserFactory factory = SAXParserFactory.newInstance();
//		factory.setNamespaceAware(true);
//		factory.setFeature("http://xml.org/sax/features/namespace-prefixes",
//				true);
//		factory.setFeature("http://xml.org/sax/features/validation",
//				compiler.isValidating());
//		factory.setValidating(compiler.isValidating());
//		SAXParser parser = factory.newSAXParser();
//		XMLReader reader = parser.getXMLReader();
//		reader.setProperty("http://xml.org/sax/properties/lexical-handler",
//				handler);
//		reader.setErrorHandler(handler);
//		reader.setEntityResolver(handler);
//		return parser;
//	}
//
//	private String writeXmlDecl(InputStream is) throws IOException {
//		is.mark(128);
//		String encoding = "UTF-8";
//		try {
//			byte[] b = new byte[128];
//			if (is.read(b) > 0) {
//				String r = new String(b);
//				Matcher m = XmlDeclaration.matcher(r);
//				if (m.find()) {
//
//					Class<?> manager = Class
//							.forName("com.sun.faces.facelets.compiler.CompilationManager");
//					manager.getMethod("writeInstruction()").invoke(mgr,
//							m.group(0) + "\n");
//					if (m.group(3) != null) {
//						encoding = m.group(3);
//					}
//				}
//			}
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (SecurityException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} finally {
//			is.reset();
//		}
//		return encoding;
//	}
//
//	public Facelet getNewDefaultFacelet(URL url, String alias, FaceletHandler h) {
//
//		Facelet defaultFacelet = null;
//		Class<?> defaultFaceletClass;
//		Class<?>[] intArgsClass = new Class[] {
//				com.sun.faces.facelets.impl.DefaultFaceletFactory.class,
//				ExpressionFactory.class, URL.class, String.class,
//				FaceletHandler.class };
//		Object[] intArgs = new Object[] { this,
//				this.compiler.createExpressionFactory(), url, alias, h };
//		Constructor<?> intArgsConstructor;
//
//		try {
//			defaultFaceletClass = Class
//					.forName("com.sun.faces.facelets.impl.DefaultFacelet");
//			intArgsConstructor = defaultFaceletClass
//					.getConstructor(intArgsClass);
//			intArgsConstructor.setAccessible(true);
//			defaultFacelet = (Facelet) intArgsConstructor.newInstance(intArgs);
//		} catch (ClassNotFoundException e) {
//			System.out.println(e);
//		} catch (NoSuchMethodException e) {
//			System.out.println(e);
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
//		return defaultFacelet;
//	}
//
//	public Object getNewCompilationManager(String alias) {
//
//		Object compilationManager = null;
//		Class<?> compilationManagerClass;
//		Class<?>[] intArgsClass = new Class[] { String.class,
//				com.sun.faces.facelets.compiler.Compiler.class };
//		Object[] intArgs = new Object[] { alias, this.compiler };
//		Constructor<?> intArgsConstructor;
//
//		try {
//			compilationManagerClass = Class
//					.forName("com.sun.faces.facelets.compiler.CompilationManager");
//			intArgsConstructor = compilationManagerClass
//					.getConstructor(intArgsClass);
//			intArgsConstructor.setAccessible(true);
//			compilationManager = intArgsConstructor.newInstance(intArgs);
//		} catch (ClassNotFoundException e) {
//			System.out.println(e);
//		} catch (NoSuchMethodException e) {
//			System.out.println(e);
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		}
//		return compilationManager;
//	}
//
//	public Object getCompilationManager() {
//		return mgr;
//	}
//
//}
