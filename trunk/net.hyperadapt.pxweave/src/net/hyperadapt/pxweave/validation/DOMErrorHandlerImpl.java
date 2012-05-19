package net.hyperadapt.pxweave.validation;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

/**
 * This class implements the {@link DOMErrorHandler} to react on DOMErrors.
 */
public class DOMErrorHandlerImpl implements DOMErrorHandler {

	/**
	 * implements {@link DOMErrorHandler#handleError(DOMError)}
	 * 
	 * @param error
	 *            The reported {@link DOMError}
	 */
	public boolean handleError(final DOMError error) {
		if (error.getSeverity() == DOMError.SEVERITY_WARNING) {
			return true;
		} else {
			throw new IllegalArgumentException(error.getMessage());
		}
	}

}
