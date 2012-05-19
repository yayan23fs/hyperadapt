package net.hyperadapt.pxweave.aspects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.aspects.ast.AspectOrder;
import net.hyperadapt.pxweave.aspects.ast.Interface;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * The purpose of this class is to order an arrayList of given aspects according
 * to the declarations in {@link Interface#getAspectOrder()} of those aspects.
 * 
 * @author msteinfeldt
 */
public class AspectOrderer {
	
	/**
	 * This method builds a digraph.<br>
	 * The declarations in {@link AspectOrder} in all given aspects can be seen
	 * as a digraph(V,E) with each aspect as an element of V. If an aspect a is
	 * declared as to be evaluated before an aspect b, an edge(a,b) is added to
	 * E. Likewise, an edge(b,a) if an aspect a is declared to be evaluated
	 * after an aspect b. If an aspect is declared "asLast" or "asFirst" edges
	 * are added from or to each of the other aspects. This method throws an
	 * {@link XMLWeaverException} if the graph can't be built because of errors
	 * in {@link AspectOrder}.
	 * 
	 * @param unorderedAspects2
	 *            - An {@link List} of {@link Aspect}s, for which this
	 *            graph will be built
	 * @throws XMLWeaverException
	 */
	private static DirectedGraph<Aspect, DefaultEdge> builtGraph(
			final List<Aspect> unorderedAspects) throws XMLWeaverException {
		final DirectedGraph<Aspect, DefaultEdge> aspectGraph = new DefaultDirectedGraph<Aspect, DefaultEdge>(
				DefaultEdge.class);
		final HashMap<String, Aspect> nameToAspect = new HashMap<String, Aspect>();
		for (final Aspect a : unorderedAspects) {
			aspectGraph.addVertex(a);
			nameToAspect.put(a.getName(), a);
		}

		boolean last = false;
		Aspect lastAspect = null;
		boolean first = false;
		Aspect firstAspect = null;

		for (final Aspect a : unorderedAspects) {
			if (a.getInterface().getAspectOrder() != null) {
				// addEdges to or from any other aspects if an aspect is
				// declared asFirst or asLast
				if (a.getInterface().getAspectOrder().getAbsolutePosition()
						.value().contentEquals("asLast")) {
					if (!last) {
						last = true;
						lastAspect = a;
						for (final Aspect b : unorderedAspects) {
							aspectGraph.addEdge(b, a);
						}
						aspectGraph.removeEdge(a, a);
					} else {
						throw new XMLWeaverException(
								"ERROR in:--Aspect:"
										+ a.getName()
										+ "---cause: aspect is declared as last aspect, but another aspect("
										+ a.getName()
										+ ") is declared as last aspect as well");
					}
				}
				if (a.getInterface().getAspectOrder().getAbsolutePosition()
						.value().contentEquals("asFirst")) {
					if (!first) {
						first = true;
						firstAspect = a;
						for (final Aspect b : unorderedAspects) {
							aspectGraph.addEdge(a, b);
						}
						aspectGraph.removeEdge(a, a);
					} else {
						throw new XMLWeaverException(
								"ERROR in:--Aspect:"
										+ a.getName()
										+ "---cause: aspect is declared as first aspect, but another aspect("
										+ a.getName()
										+ ") is declared as first aspect as well");
					}
				}

				final List<String> namesOfBeforeAspects = a.getInterface()
						.getAspectOrder().getBeforeAspect();
				final List<String> namesOfAfterAspects = a.getInterface()
						.getAspectOrder().getAfterAspect();
				if (namesOfBeforeAspects.size() != 0) {
					if (a == lastAspect) {
						throw new XMLWeaverException(
								"ERROR in:--Aspect:"
										+ a.getName()
										+ "---cause: aspect is declared as last aspect, but points to aspect(s) that should be evaluated after this aspect");
					}

					for (int i = 0; i < namesOfBeforeAspects.size(); i++) {
						if (nameToAspect.containsKey(namesOfBeforeAspects
								.get(i))) {
							aspectGraph.addEdge(a, nameToAspect
									.get(namesOfBeforeAspects.get(i)));
						} else {
							throw new XMLWeaverException(
									"error: Aspect \""
											+ namesOfBeforeAspects.get(i)
											+ "\" is missing---was declared as beforeAspect in \""
											+ a.getName() + "\"");
						}
					}
				}
				if (namesOfAfterAspects.size() != 0) {
					if (a == firstAspect) {
						throw new XMLWeaverException(
								"ERROR in:--Aspect:"
										+ a.getName()
										+ "---cause: aspect is declared as first aspect, but points to aspect(s) that should be evaluated ahead");
					}

					for (int i = 0; i < namesOfAfterAspects.size(); i++) {
						if (nameToAspect
								.containsKey(namesOfAfterAspects.get(i))) {
							aspectGraph.addEdge(nameToAspect
									.get(namesOfAfterAspects.get(i)), a);
						} else {
							throw new XMLWeaverException(
									"error: Aspect \""
											+ namesOfAfterAspects.get(i)
											+ "\" is missing---was declared as previousAspect in \""
											+ a.getName() + "\"");
						}
					}
				}
			}
		}
		return aspectGraph;
	}

	/**
	 * This method uses a topological sorting algorithm on the builtGraph (see
	 * {@link #builtGraph(List)}) to bring the aspects in the correct
	 * order.<br>
	 * The Graph is supposed to be a DAG, i.e. there are no cycles. If the graph
	 * isn't a DAG this method throws an {@link XMLWeaverException} and states
	 * the aspects that yield the cycle.
	 * 
	 * @return an {@link List} of ordered {@link Aspect}s.
	 * @throws XMLWeaverException
	 */
	public static List<Aspect> orderAspects(List<Aspect> aspects) throws XMLWeaverException {
		final ArrayList<Aspect> orderedAspects = new ArrayList<Aspect>();
		final DirectedGraph<Aspect, DefaultEdge> aspectGraph = builtGraph(aspects);
		boolean removedSink;

		while (aspectGraph.edgeSet().isEmpty() == false) {
			removedSink = false;
			Aspect[] remainingAspects = aspectGraph.vertexSet().toArray(
					new Aspect[0]);

			// remove sinks and their outgoing edges
			for (final Aspect a : remainingAspects) {
				if (aspectGraph.inDegreeOf(a) == 0) {
					aspectGraph.removeAllEdges(aspectGraph.edgesOf(a));
					aspectGraph.removeVertex(a);
					orderedAspects.add(a);
					removedSink = true;
				}
			}
			// if there are no sinks anymore, but still edges, there must be a
			// cycle
			if (!removedSink) {
				final String info = ("error:Cycle in aspect order. Cycle is between these aspects:");
				aspectGraph.removeAllVertices(orderedAspects);
				for (final Aspect c : aspectGraph.vertexSet()) {
					info.concat(c.getName());
				}
				throw new XMLWeaverException(info);
			}
		}
		// in case there are only aspects that do not have any constraints
		if (!aspectGraph.vertexSet().isEmpty()) {
			orderedAspects.addAll(aspectGraph.vertexSet());
		}
		return orderedAspects;
	}

}
