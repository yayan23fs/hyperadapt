package net.hyperadapt.pxweave.integration.util;

import java.util.List;
import java.util.Map;

import net.hyperadapt.pxweave.aspects.ast.AdviceGroup;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.aspects.ast.Dependency;
import net.hyperadapt.pxweave.context.IWeavingContext;
import net.hyperadapt.pxweave.interpreter.AspectInterpreter;
import net.hyperadapt.pxweave.util.CallbackHelper;
import net.hyperadapt.pxweave.util.ContextModelHelper;

/**
 * It caches requests from the same user or different users with the same
 * evaluation result of the available aspects (equal profile behavior).
 * 
 * @author Martin Lehmann
 * 
 */
public class WeavingCache {

	private List<Aspect> aspects;
	private IWeavingContext context;
	private Long adaptedID = 0L;
	private CallbackHelper helper;
	private String sessionId;

	/**
	 * Constructor, which get a list of available aspects, which are woven
	 * during the evaluation process of PX-Weave. Furthermore the cache has
	 * access to the context model to evaluate the advice constraints within the
	 * aspects.
	 * 
	 * @param aspectsList
	 *            - list of available aspects
	 * @param aContext
	 *            - to get the current context parameters
	 * @param aSessionId
	 *            - to save changed context parameters within CROCO
	 */
	public WeavingCache(List<Aspect> aspectsList, IWeavingContext aContext,
			String aSessionId) {
		aspects = aspectsList;
		context = aContext;
		helper = CallbackHelper.getInstance();
		sessionId = aSessionId;
	}

	/**
	 * Return the context model, after checking whether the context parameters
	 * changed.
	 * 
	 * @return IWeavingContext - current context model
	 */
	public IWeavingContext getContext() {
		Map<String, String> newValues = helper.getEntryForSession(sessionId);
		if (newValues != null) {
			ContextModelHelper.saveResult(newValues, context);
		}
		return context;
	}

	/**
	 * Evaluate all aspects with the help of the current context model and
	 * generate an unique id for the evaluation result. If one of the needed
	 * context parameter aren't available in the context model, the unique id is
	 * 0 (cache can't be used).
	 * 
	 * @return true, if the adapted site already exists (weaving process will be
	 *         cancled).
	 */
	public boolean isCaching() {
		for (Aspect aspect : aspects) {
			List<AdviceGroup> groups = aspect.getAdviceGroup();
			for (AdviceGroup adviceGroup : groups) {
				Dependency depends = adviceGroup.getDepends();
				String bool = ContextModelHelper.replaceContextParams(
						depends.getBoolExpr(), context, true);
				if (bool == null) {
					adaptedID = 0L;
					return false;
				}
				Boolean boolValue = AspectInterpreter.boolExpressionMap
						.get(bool);
				if (boolValue != null) {
					adaptedID += boolValue.hashCode();
				} else {
					adaptedID += bool.hashCode();
				}
			}
		}
		return true;
	}

	/**
	 * Return the unique id from the evaluation process.
	 * 
	 * @return Long - unique id for the adapted file name.
	 */
	public Long getAdaptedID() {
		if (adaptedID == 0L) {
			isCaching();
		}
		return adaptedID;
	}
}
