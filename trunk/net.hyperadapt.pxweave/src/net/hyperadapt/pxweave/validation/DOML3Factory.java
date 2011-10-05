/**
 * 
 */
package net.hyperadapt.pxweave.validation;


/**
 * This factory produces a {@link DOML3Parser} and a {@link DOMValidator}
 * 
 */
public class DOML3Factory implements IDOMFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.hyperadapt.pxweave.validation.IDOMFactory#createDOMParser()
	 */
	@Override
	public IDOMParser createDOMParser() {
		return new DOML3Parser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.hyperadapt.pxweave.validation.IDOMFactory#createDOMValidator()
	 */
	@Override
	public DOMValidator createDOMValidator() {
		return new DOML3Validator();
	}

}
