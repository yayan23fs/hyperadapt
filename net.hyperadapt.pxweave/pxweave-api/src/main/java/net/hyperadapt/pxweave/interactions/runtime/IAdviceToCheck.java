package net.hyperadapt.pxweave.interactions.runtime;

import net.hyperadapt.pxweave.aspects.ast.BasicAdvice;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * interface representing an advice, with the document and nodes before applying
 * this advice to the document and afterwards.
 * 
 * @author Martin Lehmann
 */
public interface IAdviceToCheck {

	Document getBeforeDocument();

	void setBeforeDocument(Document before);

	Document getAfterDocument();

	void setAfterDocument(Document after);

	BasicAdvice getBasicAdvice();

	void setBasicAdvice(BasicAdvice ba);

	NodeList getNodesBefore();

	void setNodesBefore(NodeList nodesBefore);

	String getXPath();

	void setXPath(String xpath);

	String toString();

	void setNodesBeforeOtherAdvice(NodeList nodesAfter);

	IAdviceToCheck getCopy();

	NodeList getNodesBeforeOtherAdvice();
}
