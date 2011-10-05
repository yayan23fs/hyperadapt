package net.hyperadapt.pxweave.validation;


/**
 * 
 * 
 * @author msteinfeldt
 *
 */
public enum ValidationMode {

	/**
	 * Advices are validated with {@link DOML3ValidationValidator}. Input
	 * documents are validated with {@link DOML3ValidationParser} during parsing
	 * or with {@link SchemaValidator} if the weaver runs in a pipeline.
	 **/
	DomLevel3ValidationAPI,
	
	/**
	 * Advices are validated with {@link DOML3ValidationValidator}. Input
	 * documents are not validated.
	 **/	
	DomLevel3ValidationAPI_noInput,
	
	/**
	 * Advices are validated with {@link DOML3Validator}. Input documents are
	 * validated with {@link DOML3Parser} during parsing or with
	 * {@link SchemaValidator} if the weaver runs in a pipeline.
	 **/
	DomLevel3,
	
	/**
	 * Advices are validated with {@link DOML3Validator}. Input documents are
	 * validated with {@link DOML3Parser} during parsing or with
	 * {@link SchemaValidator} if the weaver runs in a pipeline.
	 **/
	DomLevel3_noInput,
	
	/**
	 * no validation of output or weaving operations
	 */
	None,

	/**
	 * no validation weaving operations, Input documents are
	 * validated with {@link DOML3Parser} during parsing or with
	 * {@link SchemaValidator} if the weaver runs in a pipeline. The output is not validated.
	 */
	OnlyInput,
	
	/**
	 * no validation of output or weaving operations
	 */	
	OnlyOutput,
	
	/**
	 * no validation of weaving operations
	 */
	NoOperationValidation;
	;
	
	/**
	 * @return True, if the result should be validated after weaving.
	 */
	public boolean isValidateResult() {
		return this == OnlyOutput ||
				this == NoOperationValidation;
	}
	
	/**
	 * 
	 * @return True, if single operations should be validated (Only possible with DOM L3).
	 */
	public boolean isValidateOperations() {
		return this == DomLevel3 ||
				this == DomLevel3_noInput ||
				this == DomLevel3ValidationAPI ||
				this == DomLevel3ValidationAPI_noInput;
	}
	
	/**
	 * @return True, if the input should be validated before weaving.
	 */

	public boolean isValidateInput() {
		return this == DomLevel3 ||
				this == DomLevel3ValidationAPI ||
				this == NoOperationValidation ||
				this == OnlyInput;
	}
	
	public IDOMFactory createDOMFactory(){
		if(this == DomLevel3ValidationAPI || this == DomLevel3ValidationAPI_noInput){
			return new DOML3ValidationFactory();
		}
		return new DOML3Factory();
	}
	
}
