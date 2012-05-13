package net.hyperadapt.pxweave.validation;

/**
 * Mode to validate the different transformation steps.
 * 
 * @author Martin Lehmann
 *
 */
public interface IValidationMode {

	boolean isValidateResult();
	boolean isValidateOperations();
	boolean isValidateInput();
	IDOMFactory createDOMFactory();
}
