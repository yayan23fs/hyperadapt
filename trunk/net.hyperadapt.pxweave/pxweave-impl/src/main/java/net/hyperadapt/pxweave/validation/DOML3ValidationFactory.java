
package net.hyperadapt.pxweave.validation;


/**
 * @author msteinfeldt
 * 
 */
public class DOML3ValidationFactory implements IDOMFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.hyperadapt.pxweave.validation.IDOMFactory#createDOMParser()
	 */
	public IDOMParser createDOMParser() {
		return new DOML3ValidationParser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.hyperadapt.pxweave.validation.IDOMFactory#createDOMValidator()
	 */
	public DOMValidator createDOMValidator() {
		return new DOML3ValidationValidator();
	}

}
