package net.hyperadapt.pxweave.integration.util;

import net.hyperadapt.pxweave.integration.common.IntegrationConstraints;

/**
 * Helper, which uses the PropertyHelper to activate or deactivate the different
 * integration solutions.
 * 
 * @author Martin Lehmann
 * 
 */
public class PXWeaveIntegrationHelper {

	private static PXWeaveIntegrationHelper instance = null;

	private boolean genericIntegration = false;
	private boolean jsfaopIntegration = false;

	/**
	 * Constructor, which only can be instantiate by a static method within this
	 * class.
	 */
	private PXWeaveIntegrationHelper() {
		/* nothing to do */
	}

	/**
	 * Instantiation of the integration helper by the use of the singleton
	 * pattern.
	 * 
	 * @return PXWeaveIntegrationHelper
	 */
	public static synchronized PXWeaveIntegrationHelper getInstance() {
		if (instance == null) {
			instance = new PXWeaveIntegrationHelper();
			instance.init();
		}
		return instance;
	}

	/**
	 * Activate or deactivate the Pre- and Post-Processing.
	 * 
	 * @return true, if the PreProcessing and the PostProcessing are activated
	 *         (otherwise: false).
	 */
	public boolean isGenericIntegration() {
		return genericIntegration;
	}

	/**
	 * Activate or deactivate the programmatic advice.
	 * 
	 * @return true, if the programmatic advice is activated (otherwise: false).
	 */
	public boolean isJSFAOPIntegration() {
		return jsfaopIntegration;
	}

	/**
	 * Initialization of the integration helper by the use of the external
	 * properties.
	 */
	private void init() {
		PropertyHelper helper = new PropertyHelper(
				IntegrationConstraints.APPLICATION_CONTEXTPATH,
				IntegrationConstraints.APPLICATION_PROPERTY);
		genericIntegration = helper.getBooleanProperty(
				IntegrationConstraints.INTEGRATION_GENERIC, false);
		jsfaopIntegration = helper.getBooleanProperty(
				IntegrationConstraints.INTEGRATION_JSFAOP, false);
	}

}
