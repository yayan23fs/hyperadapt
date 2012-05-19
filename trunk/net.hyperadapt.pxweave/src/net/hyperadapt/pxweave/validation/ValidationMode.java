package net.hyperadapt.pxweave.validation;

import java.net.URI;

import net.hyperadapt.pxweave.XMLWeaverException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.validation.NodeEditVAL;


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
			return createDOM3ValidationAPIFactory();	
		}
		if(this==None)
			return createDOM3DummyFactory();
		return createDOM3Factory();
	}
	
	/**
	 * 	Factory that creates DOM3 API parser, but only a dummy validator.
	 * 
	 * @return
	 */
	public static IDOMFactory createDOM3DummyFactory(){
		return new IDOMFactory() {	
			@Override
			public IDOMValidator createDOMValidator() {
				return new IDOMValidator() {				
					@Override
					public short validateElement(Element element) throws XMLWeaverException {
						return NodeEditVAL.VAL_TRUE;
					}
					
					@Override
					public short validateDocument(Document document) throws XMLWeaverException {
						return NodeEditVAL.VAL_TRUE;
					}
					
					@Override
					public void setSchema(URI schemaURI) {
						//do nothing
					}
					
					@Override
					public boolean needsSchema() {
						return false;
					}
				};
			}
			
			@Override
			public IDOMParser createDOMParser() {
				return new DOML3Parser(false);
			}
		};
	}
	
	
	/**
	 * 	Factory that uses DOM3 API, but not DOM Level 3 Validation API.
	 * 
	 * @return
	 */
	public static IDOMFactory createDOM3Factory(){
		return new IDOMFactory() {	
			@Override
			public IDOMValidator createDOMValidator() {
				return new DOML3Validator();
			}
			
			@Override
			public IDOMParser createDOMParser() {
				return new DOML3Parser(true);
			}
		};
	}
	
	/**
	 * Factory that uses DOM-LVL3 Validation API.
	 * 
	 * @return
	 */
	public static IDOMFactory createDOM3ValidationAPIFactory(){
		return new IDOMFactory() {
			@Override
			public IDOMValidator createDOMValidator() {			
				return new DOML3ValidationValidator();
			}
			
			@Override
			public IDOMParser createDOMParser() {
				return new DOML3ValidationParser();
			}
		};
	}
	
}
