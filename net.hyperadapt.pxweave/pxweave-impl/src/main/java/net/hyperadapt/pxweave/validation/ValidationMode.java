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
public class ValidationMode implements IValidationMode {

	private Mode mode;

	public ValidationMode(Mode aMode) {
		mode = aMode;
	}

	protected Mode getMode() {
		return mode;
	}

	public enum Mode {

		/**
		 * Advices are validated with {@link DOML3ValidationValidator}. Input
		 * documents are validated with {@link DOML3ValidationParser} during
		 * parsing or with {@link SchemaValidator} if the weaver runs in a
		 * pipeline.
		 **/
		DomLevel3ValidationAPI,

		/**
		 * Advices are validated with {@link DOML3ValidationValidator}. Input
		 * documents are not validated.
		 **/
		DomLevel3ValidationAPI_noInput,

		/**
		 * Advices are validated with {@link DOML3Validator}. Input documents
		 * are validated with {@link DOML3Parser} during parsing or with
		 * {@link SchemaValidator} if the weaver runs in a pipeline.
		 **/
		DomLevel3,

		/**
		 * Advices are validated with {@link DOML3Validator}. Input documents
		 * are validated with {@link DOML3Parser} during parsing or with
		 * {@link SchemaValidator} if the weaver runs in a pipeline.
		 **/
		DomLevel3_noInput,

		/**
		 * no validation of output or weaving operations
		 */
		None,

		/**
		 * no validation weaving operations, Input documents are validated with
		 * {@link DOML3Parser} during parsing or with {@link SchemaValidator} if
		 * the weaver runs in a pipeline. The output is not validated.
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
	};

	/**
	 * @return True, if the result should be validated after weaving.
	 */
	public boolean isValidateResult() {
		return getMode() == Mode.OnlyOutput
				|| getMode() == Mode.NoOperationValidation;
	}

	/**
	 * 
	 * @return True, if single operations should be validated (Only possible
	 *         with DOM L3).
	 */
	public boolean isValidateOperations() {
		return getMode() == Mode.DomLevel3
				|| getMode() == Mode.DomLevel3_noInput
				|| getMode() == Mode.DomLevel3ValidationAPI
				|| getMode() == Mode.DomLevel3ValidationAPI_noInput;
	}

	/**
	 * @return True, if the input should be validated before weaving.
	 */

	public boolean isValidateInput() {
		return getMode() == Mode.DomLevel3
				|| getMode() == Mode.DomLevel3ValidationAPI
				|| getMode() == Mode.NoOperationValidation
				|| getMode() == Mode.OnlyInput;
	}

	public IDOMFactory createDOMFactory() {
		if (getMode() == Mode.DomLevel3ValidationAPI
				|| getMode() == Mode.DomLevel3ValidationAPI_noInput) {
			return createDOM3ValidationAPIFactory();
		}
		if (getMode() == Mode.None)
			return createDOM3DummyFactory();
		return createDOM3Factory();
	}

	/**
	 * Factory that creates DOM3 API parser, but only a dummy validator.
	 * 
	 * @return
	 */
	private IDOMFactory createDOM3DummyFactory() {
		return new IDOMFactory() {

			public IDOMValidator createDOMValidator() {
				return new IDOMValidator() {

					public short validateElement(Element element)
							throws XMLWeaverException {
						return NodeEditVAL.VAL_TRUE;
					}

					public short validateDocument(Document document)
							throws XMLWeaverException {
						return NodeEditVAL.VAL_TRUE;
					}

					public void setSchema(URI schemaURI) {
						// do nothing
					}

					public boolean needsSchema() {
						return false;
					}
				};
			}

			public IDOMParser createDOMParser() {
				return new DOML3Parser(false);
			}
		};
	}

	/**
	 * Factory that uses DOM3 API, but not DOM Level 3 Validation API.
	 * 
	 * @return
	 */
	private IDOMFactory createDOM3Factory() {
		return new IDOMFactory() {

			public IDOMValidator createDOMValidator() {
				return new DOML3Validator();
			}

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
	private IDOMFactory createDOM3ValidationAPIFactory() {
		return new IDOMFactory() {

			public IDOMValidator createDOMValidator() {
				return new DOML3ValidationValidator();
			}

			public IDOMParser createDOMParser() {
				return new DOML3ValidationParser();
			}
		};
	}

}
