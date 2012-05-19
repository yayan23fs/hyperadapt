package net.hyperadapt.pxweave.validation;

/**
 * @author msteinfeldt
 * 
 */
public interface IDOMFactory {
	/**
	 * Creates a {@link IDOMParser}
	 * 
	 * @return a {@link IDOMParser}
	 */
	public IDOMParser createDOMParser();

	/**
	 * Creates a {@link DOMValidator}
	 * 
	 * @return a {@link DOMValidator}
	 */
	public IDOMValidator createDOMValidator();
}
