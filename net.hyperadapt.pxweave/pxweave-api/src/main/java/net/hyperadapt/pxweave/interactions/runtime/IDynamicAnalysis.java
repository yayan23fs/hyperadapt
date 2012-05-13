package net.hyperadapt.pxweave.interactions.runtime;

import org.w3c.dom.Document;

import net.hyperadapt.pxweave.IPXWeaveNamespaceContext;
import net.hyperadapt.pxweave.aspects.IProgrammaticJoinpoint;
import net.hyperadapt.pxweave.aspects.ast.AdviceLocator;
import net.hyperadapt.pxweave.interpreter.IAdviceContext;

public interface IDynamicAnalysis {

	void setLogged(boolean logged);
	void setPPC(IProgrammaticJoinpoint ppc, AdviceLocator al);
	void logErrors();
	void addDocumentAsAfterForLastAdvice(Document afterDocument);
	void addAdvice(IAdviceToCheck advicetocheck);
	void setNamespaceContext(IPXWeaveNamespaceContext nsc);
	void setAdviceContext(IAdviceContext ac);
}