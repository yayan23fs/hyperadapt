package net.hyperadapt.pxweave.interactions.devtime.xml;

import net.hyperadapt.pxweave.aspects.ast.BasicAdvice;
import net.hyperadapt.pxweave.aspects.ast.ChangeOrder;
import net.hyperadapt.pxweave.aspects.ast.ChangeValue;
import net.hyperadapt.pxweave.aspects.ast.ChooseVariant;
import net.hyperadapt.pxweave.aspects.ast.CollapseElement;
import net.hyperadapt.pxweave.aspects.ast.Delete;
import net.hyperadapt.pxweave.aspects.ast.EnrichContent;
import net.hyperadapt.pxweave.aspects.ast.ExpandElement;
import net.hyperadapt.pxweave.aspects.ast.InsertElement;
import net.hyperadapt.pxweave.aspects.ast.MoveElement;
import net.hyperadapt.pxweave.aspects.ast.ReduceContent;

/**
 * class for converting string to corresponding instances of basic advices
 * 
 * @author danielkadner
 * 
 */
public class AdviceClassName {

	public static String getClassName(BasicAdvice ba) {
		if (ba instanceof ChangeOrder)
			return "ChangeOrder";
		if (ba instanceof ChangeValue)
			return "ChangeValue";
		if (ba instanceof CollapseElement)
			return "CollapseElement";
		if (ba instanceof ChooseVariant)
			return "ChooseVariant";
		if (ba instanceof Delete)
			return "DeleteElement";
		if (ba instanceof EnrichContent)
			return "EnrichContent";
		if (ba instanceof ExpandElement)
			return "ExpandElement";
		if (ba instanceof InsertElement)
			return "InsertElement";
		if (ba instanceof MoveElement)
			return "MoveElement";
		if (ba instanceof ReduceContent)
			return "ReduceContent";
		return null;
	}

}
