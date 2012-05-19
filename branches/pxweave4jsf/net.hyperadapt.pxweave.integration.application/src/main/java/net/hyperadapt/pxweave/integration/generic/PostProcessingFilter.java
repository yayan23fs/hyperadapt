package net.hyperadapt.pxweave.integration.generic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hyperadapt.pxweave.integration.common.IntegrationConstraints;
import net.hyperadapt.pxweave.integration.util.PXWeaveHelper;
import net.hyperadapt.pxweave.integration.util.PXWeaveIntegrationHelper;
import net.hyperadapt.pxweave.integration.util.Statistic;

/**
 * The PostProcessingFilter is a generic variant to integrate PX-Weave in a
 * webframework lifecycle. The filter interrupts the request processing of an
 * application server and invokes PX-Weave after the webframework.
 * 
 * @author Martin Lehmann
 * 
 */
public class PostProcessingFilter implements Filter {

	private String pattern = null;
	Logger log = Logger.getLogger("PostProcessingFilter");
	private static Map<String, String> map = new HashMap<String, String>();

	/**
	 * Initialization of the filter with the developer configuration in the
	 * web.xml of the application server.
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		this.pattern = filterConfig.getInitParameter("pattern");
	}

	/**
	 * The method invokes PX-Weave with the help of the PXWeaveHelper. After the
	 * genereated OutputStream had been applyed, the wrapper class, which
	 * included the response, is used to write this adapted stream to the
	 * response.
	 * 
	 * @param request
	 *            - to get the current session
	 * @param responseWrapper
	 *            - keeps open the response
	 * @param out
	 *            - writer of the response stream
	 * @param response
	 *            - current response
	 * @throws IOException
	 *             - in an access exception
	 */
	private void callPXWeaveAfter(ServletRequest request,
			PXWeaveResponseWrapper responseWrapper, PrintWriter out,
			ServletResponse response) throws IOException {
		
//		if (map.get(((HttpServletRequest)request).getSession().getId()) != null) {
//			out.write(map.get(((HttpServletRequest)request).getSession().getId()));
//			//map.remove(((HttpServletRequest)request).getSession().getId());
//			return;
//		}
		String strResponse = responseWrapper.toString();

		InputStream inputStream = new ByteArrayInputStream(
				strResponse.getBytes());

		/*
		 * Invokation of PX-Weave with the generated response stream from the
		 * webframework, the current session information and the programmatic
		 * joinpoint.
		 */
		OutputStream outputStream = PXWeaveHelper.createCall(inputStream,
				IntegrationConstraints.POSTPROCESSING,
				((HttpServletRequest) request).getSession());
		if (outputStream != null) {
			/* Write the adapted stream to the response. */
			CharArrayWriter caw = new CharArrayWriter();
			caw.write(((ByteArrayOutputStream) outputStream)
					.toString(IntegrationConstraints.HTML_ENCODING));
			response.setContentLength(caw.toString().length());
			responseWrapper
					.setContentType(IntegrationConstraints.HTML_CONTENTTYPE);
			out.write(caw.toString());

			//map.put(((HttpServletRequest)request).getSession().getId(), caw.toString());
		}
	}

	/**
	 * The method is invoked from the filter chain. The programmatic function is
	 * to decide whether a request achieve the pattern constraints (defined from
	 * the developer within the web.xml of the application server). Futhermore
	 * the resource and ajax request don't cause a PX-Weave invokation.
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = ((HttpServletRequest) request);
		String url = ((HttpServletRequest) request).getRequestURI();

		/*
		 * The pattern constraints in the web.xml checks whether the current
		 * request allocated a resource or a template file.
		 */
		boolean isRessource = false;
		if (pattern != null && !url.matches(pattern)) {
			isRessource = true;
		}

		/* If the parameter is set, the adaptation has been executed. */
		Object adapted = request
				.getParameter(IntegrationConstraints.HEADERPARAM_ADAPTED);
		
		boolean isAjax = IntegrationConstraints.HEADERPARAM_XML.equals(req
				.getHeader(IntegrationConstraints.HEADERPARAM_AJAX))
				|| IntegrationConstraints.HEADERPARAM_JSF_PARTIAL
						.equals(req
								.getHeader(IntegrationConstraints.HEADERPARAM_JSF_REQUEST));
		

		// boolean run = false;
		// Long time = map.get(req.getSession().getId());
		// if (time != null) {
		// Long newTime = Calendar.getInstance().getTimeInMillis() - time;
		// if (newTime < 2000) {
		// run = true;
		// }
		// }
		
		/*
		 * If the adaptation has not yet run and its not about an allocation of
		 * a resource as well as an ajax invoke (otherwise, continue the filter
		 * chain), then PX-Weave can adopt the stream.
		 */
		if (PXWeaveIntegrationHelper.getInstance().isGenericIntegration()
				&& !isRessource && adapted == null && !isAjax) {
			
			/*
			 * Put the response in a wrapper to prevent the closing action on
			 * the response stream from the used webframework.
			 */
			PrintWriter out = response.getWriter();
			PXWeaveResponseWrapper responseWrapper = new PXWeaveResponseWrapper(
					(HttpServletResponse) response);

			/*
			 * The request cycle continues (other filters were called and the
			 * webframework processes the request).
			 */
			chain.doFilter(request, responseWrapper);

			long currentTime;
			if (IntegrationConstraints.WRITESTATISTIC) {
				currentTime = Calendar.getInstance().getTimeInMillis();
			}
			/*
			 * After the processing of the request from the webframework, the
			 * adaptation with PX-Weave be accomplished.
			 */
			callPXWeaveAfter(request, responseWrapper, out, response);

			/*
			 * Its possible to evaluate the adaptation times regarding to the
			 * selenium test suite (penetration test).
			 */
			if (IntegrationConstraints.WRITESTATISTIC) {
				Statistic.countPost(req.getSession().getId(), Calendar
						.getInstance().getTimeInMillis() - currentTime);
				Statistic.countFull("End: ", req.getSession().getId(), Calendar
						.getInstance().getTimeInMillis());
				if (Statistic.readyForWriting) {
					Statistic.writeStatistic();
				}
			}

		} else {
			chain.doFilter(request, response);
		}
	}

	/**
	 * Method that is invoked when the filter was removed from the filter chain.
	 */
	public void destroy() {
		/* nothing to do */
	}
}