package net.hyperadapt.pxweave.integration.generic;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
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
import net.hyperadapt.pxweave.integration.util.PropertyHelper;
import net.hyperadapt.pxweave.integration.util.Statistic;

/**
 * The PreProcessingFilter is a generic variant to integrate PX-Weave in a
 * webframework lifecycle. The filter interrupts the request processing of an
 * application server and invokes PX-Weave before the webframework.
 * 
 * @author Martin Lehmann
 * 
 */
public class PreProcessingFilter implements Filter {

	private String pattern = null;
	Logger log = Logger.getLogger("PreProcessingFilter");

	/**
	 * Initialization of the filter with the developer configuration in the
	 * web.xml of the application server.
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		this.pattern = filterConfig.getInitParameter("pattern");
	}

	/**
	 * The method invokes PX-Weave with the help of the PXWeaveHelper. After the
	 * adaptation process the request cycle will be aborted and it came to a
	 * redirect to the adapted file.
	 * 
	 * @param request
	 *            - to get the current session
	 * @param response
	 *            - to abort the request and send a redirect
	 * @throws IOException
	 *             - in an access exception
	 * @throws ServletException
	 *             - exception within the sendRedirect mechanism
	 */
	private void callPXWeaveBefore(ServletRequest request,
			ServletResponse response) throws IOException, ServletException {

		/* Get the allocated file from the request url. */
		String requestURI = ((HttpServletRequest) request).getRequestURI();
		String contextPath = ((HttpServletRequest) request).getContextPath()
				+ "/";
		requestURI = requestURI.replace(contextPath, "");
		requestURI = requestURI.replace(
				IntegrationConstraints.FILE_EXTENTION_JSF, "");
		String site = requestURI;
		requestURI = requestURI
				.concat(IntegrationConstraints.FILE_EXTENTION_XHTML);

		String absolutContextPath = PropertyHelper
				.getContextPath(IntegrationConstraints.APPLICATION_CONTEXTPATH);

		/*
		 * Invokation of PX-Weave with the adapted file name, the current
		 * session information and the programmatic joinpoint.
		 */
		String responseURI = PXWeaveHelper.createCall(new File(
				absolutContextPath + requestURI), absolutContextPath, site,
				IntegrationConstraints.PREPROCESSING,
				((HttpServletRequest) request).getSession());

		/* Adaptation is completed, now send a redirect to the adapted file. */
		if (responseURI != null) {
			((HttpServletResponse) response).sendRedirect(contextPath
					+ responseURI.replace("xhtml", "jsf") + "?adapted=true");
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

		String url = ((HttpServletRequest) request).getRequestURI();
		HttpServletRequest req = ((HttpServletRequest) request);

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

		/*
		 * If the adaptation has not yet run and its not about an allocation of
		 * a resource as well as an ajax invoke (otherwise, continue the filter
		 * chain), then PX-Weave can adopt the stream.
		 */
		if (PXWeaveIntegrationHelper.getInstance().isGenericIntegration()
				&& !isRessource && adapted == null && !isAjax) {
			/*
			 * Its possible to evaluate the adaptation times regarding to the
			 * selenium test suite (penetration test).
			 */
			long currentTime;
			if (IntegrationConstraints.WRITESTATISTIC) {
				currentTime = Calendar.getInstance().getTimeInMillis();
				Statistic.countFull("Start:", req.getSession().getId(),
						currentTime);
			}
			/*
			 * Before the processing of the request from the webframework
			 * started, the adaptation of the template with PX-Weave will be
			 * begin.
			 */
			callPXWeaveBefore(request, response);

			if (IntegrationConstraints.WRITESTATISTIC) {
				Statistic.countPre(req.getSession().getId(), Calendar
						.getInstance().getTimeInMillis() - currentTime);
			}
		}
		chain.doFilter(request, response);
	}

	/**
	 * Method that is invoked when the filter was removed from the filter chain.
	 */
	public void destroy() {
		/* nothing to do */
	}
}
