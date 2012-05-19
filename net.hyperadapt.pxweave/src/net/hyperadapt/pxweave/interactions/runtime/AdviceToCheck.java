package net.hyperadapt.pxweave.interactions.runtime;

import net.hyperadapt.pxweave.aspects.ast.BasicAdvice;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * class representing an advice, with the document and nodes before applying
 * this advice to the document and afterwards.
 * 
 * @author dkadner
 */
public class AdviceToCheck {

	private Document before;
	private Document after;
	private BasicAdvice advice;
	private NodeList nodesBefore;
	private NodeList nodesBeforeOtherAdvice;
	private String xpath;

	public AdviceToCheck(BasicAdvice basicadvice, NodeList nodelist) {
		this.advice = basicadvice;
		this.nodesBefore = nodelist;
		this.nodesBeforeOtherAdvice = null;
		this.before = null;
		this.after = null;
		this.xpath = null;
	}

	public Document getBeforeDocument() {
		return before;
	}

	public void setBeforeDocument(Document before) {
		this.before = before;
	}

	public Document getAfterDocument() {
		return after;
	}

	public void setAfterDocument(Document after) {
		this.after = after;
	}

	public BasicAdvice getBasicAdvice() {
		return advice;
	}

	public void setBasicAdvice(BasicAdvice ba) {
		this.advice = ba;
	}

	public NodeList getNodesBefore() {
		return nodesBefore;
	}

	public void setNodesBefore(NodeList nodesBefore) {
		this.nodesBefore = nodesBefore;
	}

	public String getXPath() {
		return xpath;
	}

	public void setXPath(String xpath) {
		this.xpath = xpath;
	}

	public String toString() {
		return advice.toString();
	}

	public void setNodesBeforeOtherAdvice(NodeList nodesAfter) {
		this.nodesBeforeOtherAdvice = nodesAfter;
	}

	public AdviceToCheck getCopy() {
		AdviceToCheck atc = new AdviceToCheck(advice, nodesBefore);
		atc.setAfterDocument(after);
		atc.setBeforeDocument(before);
		atc.setNodesBeforeOtherAdvice(nodesBeforeOtherAdvice);
		return atc;
	}

	public NodeList getNodesBeforeOtherAdvice() {
		return nodesBeforeOtherAdvice;
	}
}
