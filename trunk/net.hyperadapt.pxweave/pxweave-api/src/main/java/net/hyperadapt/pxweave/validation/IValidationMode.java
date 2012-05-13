package net.hyperadapt.pxweave.validation;

public interface IValidationMode {

	boolean isValidateResult();
	boolean isValidateOperations();
	boolean isValidateInput();
	IDOMFactory createDOMFactory();
}
