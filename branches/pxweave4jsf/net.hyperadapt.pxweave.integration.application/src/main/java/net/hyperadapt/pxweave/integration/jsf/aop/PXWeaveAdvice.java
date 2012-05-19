package net.hyperadapt.pxweave.integration.jsf.aop;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.hyperadapt.pxweave.integration.common.IntegrationConstraints;
import net.hyperadapt.pxweave.integration.util.PXWeaveHelper;
import net.hyperadapt.pxweave.integration.util.PXWeaveIntegrationHelper;
import net.hyperadapt.pxweave.integration.util.Statistic;

import org.apache.abdera.i18n.text.io.DynamicPushbackInputStream;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.xml.sax.InputSource;

/**
 * The PXWeaveAdvice supported an alternative to interrupt the lifecycle of the
 * used webframework. Afterwards its possible to invoke PX-Weave following by
 * the adaptation process on the intercepted stream.
 * 
 * @author Martin Lehmann
 * 
 */
@Aspect
public class PXWeaveAdvice {

	private static Map<String, InputStream> requestMap = new HashMap<String, InputStream>();

	/**
	 * Aspects are not instantiated with "new" expressions, a program can only
	 * get a reference to an aspect instance using this static method.
	 * 
	 * @return PXWeaveAdvice, an instance of this class
	 */
	public static PXWeaveAdvice aspectOf() {
		return new PXWeaveAdvice();
	}

	/**
	 * Pointcut definition in order to decide, which joinpoints within the
	 * programmatic sequence are relevant for the advice.
	 */
	@Pointcut("execution(public * org.apache.xerces.jaxp.SAXParserImpl.parse(..))")
	public void setupPXWeave() {
	}

	/**
	 * By using the joinpoint object, the stream can be extract. Afterwards this
	 * stream can be used to initiate the adaption process of PX-Weave. In
	 * conclusion, the stream from the current pointcut method will be replaced
	 * by the adapted stream and the programmatic sequence is continued.
	 * 
	 * @param pjp
	 *            - current joinpoint within the programmatic sequence
	 * @throws Throwable
	 *             - exception during processing
	 */
	@Before("setupPXWeave()")
	public void setup(JoinPoint pjp) throws Throwable {
		if (PXWeaveIntegrationHelper.getInstance().isJSFAOPIntegration()) {

			/*
			 * The correct joinpoint as well as the availability of the jsf
			 * context are checked.
			 */
			Object[] args = pjp.getArgs();
			if (args.length != 2 || !(args[0] instanceof InputSource)
					|| FacesContext.getCurrentInstance() == null) {
				return;
			}

			/* Extraction of the important adaptation objects. */
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			InputSource source = (InputSource) args[0];
			InputStream inputStream = source.getByteStream();

			boolean isAjax = IntegrationConstraints.HEADERPARAM_XML
					.equals(request
							.getHeader(IntegrationConstraints.HEADERPARAM_AJAX))
					|| IntegrationConstraints.HEADERPARAM_JSF_PARTIAL
							.equals(request
									.getHeader(IntegrationConstraints.HEADERPARAM_JSF_REQUEST));
			if (isAjax) {
				return;
			}

			/* Conversion of the input stream to an byte stream. */
			ByteArrayOutputStream byteStream = convertInputStream(inputStream);
			inputStream = new ByteArrayInputStream(byteStream.toByteArray());
			Integer streamHash = byteStream.toString().hashCode();

			/*
			 * If the parameter is set, the adaptation has been executed. For
			 * this reason, the same request, which is made over an over again,
			 * will be cached.
			 */
			Object adapted = request
					.getAttribute(IntegrationConstraints.HEADERPARAM_STREAMHASH);
			if (adapted != null && requestMap.get(adapted.toString()) != null
					&& streamHash.equals(adapted)) {
				source.setByteStream(new BufferedInputStream(requestMap
						.get(adapted.toString())));
				return;
			}

			/* The stream in the source is replaced. */
			source.setByteStream(new BufferedInputStream(inputStream));

			/*
			 * If the adaptation has not yet run and its not about an allocation
			 * of a resource as well as an ajax invoke, then PX-Weave can adopt
			 * the stream.
			 */
			if (adapted == null && request.getParameterMap().isEmpty()) {
				HttpSession session = (HttpSession) FacesContext
						.getCurrentInstance().getExternalContext()
						.getSession(false);

				if (session == null) {
					return;
				}

				long currentTime;
				if (IntegrationConstraints.WRITESTATISTIC) {
					currentTime = Calendar.getInstance().getTimeInMillis();
				}

				/*
				 * Invokation of PX-Weave with the response stream from the
				 * webframework, the current session information and the
				 * programmatic joinpoint.
				 */
				OutputStream outputStream = PXWeaveHelper.createCall(
						inputStream, IntegrationConstraints.PXWEAVEADVICE,
						session);

				if (outputStream != null) {

					/*
					 * Remove xml header information, which lead to errors
					 * during the adaptation process.
					 */
					String output = ((ByteArrayOutputStream) outputStream)
							.toString(IntegrationConstraints.HTML_ENCODING);
					byte[] newByteStream = output
							.replace(
									"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
									"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">")
							.getBytes();

					/*
					 * Write the adapted stream to the input stream of the
					 * current pointcut.
					 */
					InputStream bais = new ByteArrayInputStream(newByteStream);
					source.setByteStream(new BufferedInputStream(bais));

					/* Put the stream into a static map for caching purpose. */
					request.setAttribute(
							IntegrationConstraints.HEADERPARAM_STREAMHASH,
							streamHash);
					requestMap.put(streamHash.toString(),
							new ByteArrayInputStream(newByteStream));
				}

				/*
				 * Its possible to evaluate the adaptation times regarding to
				 * the selenium test suite (penetration test).
				 */
				if (IntegrationConstraints.WRITESTATISTIC) {
					Statistic.countPre(session.getId(), Calendar.getInstance()
							.getTimeInMillis() - currentTime);
					if (Statistic.readyForWriting) {
						Statistic.writeStatistic();
					}
				}
			}
		}
	}

	/**
	 * Because of the restriction to read an input stream only one times, there
	 * is the possibility to "unread" the stream after reading all bytes from it
	 * - the DynamicPushbackInputStream allows this procedure.
	 * 
	 * @param inputStream
	 *            - stream from the current pointcut
	 * @return ByteArrayOutputStream - to be flexible during the processing
	 * @throws IOException
	 *             - in an access exception
	 */
	private ByteArrayOutputStream convertInputStream(InputStream inputStream)
			throws IOException {

		byte[] allBytes = new byte[1];
		PushbackInputStream reader = new DynamicPushbackInputStream(inputStream);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while (reader.read(allBytes) != -1) {
			bos.write(allBytes);
		}
		bos.flush();
		reader.unread(bos.toByteArray());

		return bos;
	}
}
