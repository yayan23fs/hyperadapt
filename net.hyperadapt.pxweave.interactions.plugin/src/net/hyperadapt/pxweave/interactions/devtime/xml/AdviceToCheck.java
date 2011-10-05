package net.hyperadapt.pxweave.interactions.devtime.xml;

import net.hyperadapt.pxweave.aspects.ast.AdviceGroup;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.aspects.ast.BasicAdvice;
//import net.hyperadapt.pxweave.aspects.ast.ChangeOrder;
import net.hyperadapt.pxweave.aspects.ast.ChangeValue;
import net.hyperadapt.pxweave.aspects.ast.CollapseElement;
//import net.hyperadapt.pxweave.aspects.ast.EnrichContent;
import net.hyperadapt.pxweave.aspects.ast.ExpandElement;
import net.hyperadapt.pxweave.aspects.ast.InsertElement;
import net.hyperadapt.pxweave.aspects.ast.MoveElement;

/**
 * class to represent an advice who got interactions to other advices
 * 
 * @author danielkadner
 * 
 */
public class AdviceToCheck {

	private String filename;
	private AdviceGroup ag;
	private Aspect ac;
	private String xpath;
	private BasicAdvice ba;

	public AdviceToCheck(String filename, Aspect ac, AdviceGroup ag, BasicAdvice ba) {
		this.ac = ac;
		this.ag = ag;
		this.ba = ba;
		this.filename = filename;
		setXPath();
	}

	public String getPathName() {
		return filename;
	}

	public String getFilename() {
		String name = filename;
		if (filename.contains("/")) {
			name = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
		}
		return name;

	}

	public Object getExtraContentIfExisting() {
//		if (this.getAdviceType() instanceof ChangeOrder) {
//			if (((ChangeOrder)this.getAdviceType()).getPermutation() != null || ((ChangeOrder)this.getAdviceType()).getPermutation() != ""){
//				return ((ChangeOrder)this.getAdviceType()).getPermutation();
//			}
//			if (((ChangeOrder)this.getAdviceType()).getReverseOrder() != null){
//				return ((ChangeOrder)this.getAdviceType()).getReverseOrder();
//			}
//			if (((ChangeOrder)this.getAdviceType()).getSortByName() != null){
//				return ((ChangeOrder)this.getAdviceType()).getSortByName();
//			}
//		}
		if (this.getAdviceType() instanceof ChangeValue) {
			return ((ChangeValue)this.getAdviceType()).getValue().getTextTemplate();
		}
		if (this.getAdviceType() instanceof CollapseElement) {
			return ((CollapseElement)this.getAdviceType()).getValue().getTextTemplate();
		}
//		if (this.getAdviceType() instanceof EnrichContent) {
//			return ((EnrichContent)this.getAdviceType()).getValue().getTextTemplate();
//		}
		if (this.getAdviceType() instanceof ExpandElement) {
			return ((ExpandElement)this.getAdviceType()).getValue().getElementTemplate();
		}
		if (this.getAdviceType() instanceof InsertElement) {
			return ((InsertElement)this.getAdviceType()).getValue().getElementTemplate();
		}
		if (this.getAdviceType() instanceof MoveElement) {
			return ((MoveElement)this.getAdviceType()).getTo();
		}
		return null;
	}

	public AdviceGroup getAdviceGroup() {
		return ag;
	}

	public void setAdviceGroup(AdviceGroup ag) {
		this.ag = ag;
	}

	public Aspect getAspect() {
		return ac;
	}

	public void setAspect(Aspect a) {
		this.ac = a;
	}

	public String getXPath() {
		setXPath();
		xpath = xpath.replace(" ", "");
		xpath = xpath.replace("\t", "");
		xpath = xpath.replace("\n", "");
		return xpath;
	}

	public String getPointcutXpath() {
		return ba.getPointcut().getValue();
	}

	public String getName() {
		return AdviceClassName.getClassName(ba);
	}

	private void setXPath() {
		String basicScope = ag.getScope().getXpath().get(0).getValue();
		boolean end = basicScope.endsWith("/");

		String xpathStart = ba.getPointcut().getValue();
		boolean start = xpathStart.startsWith("/");

		xpath = basicScope + xpathStart;

		if (end) {
			if (start)
				xpath = basicScope + xpathStart.substring(1, xpathStart.length());
		} else if (!start)
			xpath = basicScope + "/" + xpathStart;

	}

	public BasicAdvice getAdviceType() {
		return ba;
	}
}
