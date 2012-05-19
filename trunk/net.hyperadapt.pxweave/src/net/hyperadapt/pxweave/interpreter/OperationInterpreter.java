/**
 * 
 */
package net.hyperadapt.pxweave.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.ast.ChangeOrder;
import net.hyperadapt.pxweave.logger.Logable;
import net.hyperadapt.pxweave.util.DOMOperations;
import net.hyperadapt.pxweave.validation.DOMValidator;
import net.hyperadapt.pxweave.validation.IDOMValidator;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class offers operations to realize advices. It also validates performed
 * operations with the given {@link DOMValidator}. If the constructor is invoked
 * without the DOMValidator-argument, the operations are not validated. This is
 * not a general purpose class for executing DOM Operations
 */
public class OperationInterpreter extends Logable{
	private final boolean validateOperations;
	private IDOMValidator validator;

	/**
	 * Constructor for OperationInterpreter
	 * 
	 * @param validator
	 *            - the {@link DOMValidator} that should be used to validate
	 *            Operations,
	 */
	public OperationInterpreter(final IDOMValidator validator) {
		this.validator = validator;
		this.validateOperations = true;
	}

	/**
	 * Constructor for OperationInterpreter If this constructor is used,
	 * operations are not validated
	 */
	public OperationInterpreter() {
		this.validateOperations = false;
	}

	/**
	 * This operation changes the order of the childNodes of all nodes in the
	 * given {@link NodeList}. The operation is only applicable if the NodeList
	 * contains solely instances of {@link Element}. For further information
	 * refer to {@link ChangeOrder}.
	 * 
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the nodes whose childNodes
	 *            are to be reordered.
	 * @param permutation
	 *            - the new order of the nodes in the list given as a
	 *            permutation - see {@link ChangeOrder#getPermutation()}
	 * @throws XMLWeaverException
	 *             if the operation is not valid or a {@link Node} of the list
	 *             is not an {@link Element}
	 */
	public void interpretChangeOrder(final NodeList joinPoints,
			String permutation) throws XMLWeaverException {
		permutation = permutation.substring(1, permutation.length() - 1);

		final String[] pos = permutation.split("\\s");
		for (int i = 0; i < joinPoints.getLength(); i++) {
			final HashMap<Integer, Integer> positions = new HashMap<Integer, Integer>();
			final Node joinPoint = joinPoints.item(i);
			DOMOperations.checkNodeType(joinPoint, Node.ELEMENT_NODE);
			final NodeList children = joinPoint.getChildNodes();
			final Node oldOrder = joinPoint.cloneNode(true);
			for (int j = 0; j < pos.length; j++) {
				final int newPosition = Integer.parseInt(pos[j]) - 1;
				if (positions.containsKey(newPosition)) {
					throw new XMLWeaverException(
							"The given permutation does not contain unique values, can't shift more than one node to position "
									+ newPosition + 1);
				}
				if (newPosition < 0 || newPosition + 1 > children.getLength()) {
					throw new XMLWeaverException("The position \""
							+ newPosition + 1
							+ "\" does not range between 1 and "
							+ children.getLength() + 1);
				}
				positions.put(newPosition, j);
			}
			if (children.getLength() != positions.size()) {
				throw new XMLWeaverException(
						"Insufficient length of permutation, length has to be equal to the number of children---permutation length:"
								+ positions.size()
								+ "---#of children:"
								+ joinPoint.getChildNodes().getLength());
			}
			for (int k = 0; k < children.getLength(); k++) {
				final Node clone = oldOrder.getChildNodes().item(
						positions.get(k)).cloneNode(true);
				joinPoint.replaceChild(clone, children.item(k));
				validateOperation(joinPoint);

			}
		}
	}

	/**
	 * This operation changes the values of the nodes in the given
	 * {@link NodeList}. It is only applicable for a NodeList that contains
	 * instances of {@link Attr}s or {@link Element}s or both. For {@link Attr}s
	 * the value is set to the value of the corresponding {@link Text} in the
	 * {@link ArrayList}. For {@link Element}s the corresponding {@link Text}
	 * from the {@link ArrayList} is set as the only child that is an instance
	 * of {@link Text}. Other textNodes are removed.
	 * 
	 * @param joinPoints
	 *            - {@link NodeList} that contains the nodes whose values are
	 *            supposed to be changed
	 * @param valueList
	 *            - {@link ArrayList} that contains the new values
	 * @throws XMLWeaverException
	 *             if any node in the list is not an {@link Attr} or
	 *             {@link Element} or if the operation is invalid according to
	 *             {@link #validateOperation(Node)}.
	 */
	public void interpreteChangeValue(final NodeList joinPoints,
			final ArrayList<Text> valueList) throws XMLWeaverException {
		for (int i = 0; i < joinPoints.getLength(); i++) {
			final Node joinPoint = joinPoints.item(i);
			final Node value = valueList.get(i);
			DOMOperations.checkNodeType(joinPoint, Node.ATTRIBUTE_NODE,
					Node.ELEMENT_NODE);
			DOMOperations.checkNodeType(value, Node.TEXT_NODE, Node.ATTRIBUTE_NODE);
			if (joinPoint.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attr = (Attr)joinPoint;
				getLogger().debug("Changing attribute value of "
						+ attr.getOwnerElement().getNodeName() + ".@" + joinPoint.getNodeName() + ".");

				attr.setValue(value.getNodeValue());
			} else {
				DOMOperations.changeTextContent((Element) joinPoint, (Text) value);
			}
			validateOperation(joinPoint);
		}
	}

	/**
	 * This operation removes all siblings of the chosen element node that are
	 * elementNodes themselves. I.e. it chooses that element node to be the only
	 * child of its parent that's an instance of {@link Element}. The operation
	 * is performed on each node of the {@link NodeList} given as argument.
	 * 
	 * @param joinPoints
	 *            - {@link NodeList} that contains the nodes on which the
	 *            operation will be performed
	 * @throws XMLWeaverException
	 *             if any node in the list is not an {@link Element} or if the
	 *             operation is invalid according to
	 *             {@link #validateOperation(Node)}..
	 */
	public void interpreteChooseVariant(final NodeList joinPoints)
			throws XMLWeaverException {

		for (int i = 0; i < joinPoints.getLength(); i++) {
			final Node childToKeep = joinPoints.item(i);
			DOMOperations.checkNodeType(childToKeep, Node.ELEMENT_NODE);
			final Node parent = childToKeep.getParentNode();
			final NodeList children = parent.getChildNodes();
			for (int k = 0; k < children.getLength();) {
				final Node child = children.item(k);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					if (child != childToKeep) {
						if(child.getNodeName().contentEquals(childToKeep.getNodeName())){
							parent.removeChild(child);
							getLogger().debug("Deleted node "
									+ child.getNodeName()+".");
						} else {
							k++;
						}
					} else {
						k++;
					}
				} else {
					k++;
				}
			}
			validateOperation(parent);
		}
	}

	/**
	 * This operation replaces all child nodes in each node in the given
	 * {@link NodeList} by one textNode. This operation is only applicable for a
	 * NodeList that contains instances of {@link Attr}s or {@link Element}s or
	 * both. For {@link Attr}s the value is set to the value of the
	 * corresponding {@link Text} in the {@link ArrayList}. For {@link Element}s
	 * the corresponding {@link Text} from the {@link ArrayList} is set as the
	 * only child that is an instance of {@link Text}. Other textNodes are
	 * removed.
	 * 
	 * @param joinPoints
	 *            - {@link NodeList} that contains the nodes whose values are
	 *            supposed to be changed
	 * @param valueList
	 *            - a {@link ArrayList} with that contains the textNodes that
	 *            replace all children of the given {@link Node}
	 * @throws XMLWeaverException
	 *             if any node in the list is not an {@link Element} or if the
	 *             operation is invalid according to
	 *             {@link #validateOperation(Node)}.
	 */
	public void interpreteCollapseElement(final NodeList joinPoints,
			final ArrayList<Text> evaluatedTemplate) throws XMLWeaverException {
		for (int i = 0; i < joinPoints.getLength(); i++) {
			final Text value = evaluatedTemplate.get(i);
			DOMOperations.checkNodeType(joinPoints.item(i), Node.ELEMENT_NODE);
			DOMOperations.changeTextContent((Element) joinPoints.item(i), null);
			final NodeList nodeList = joinPoints.item(i).getChildNodes();
			final int counter = nodeList.getLength();
			for (int k = 0; k < counter; k++) {
				joinPoints.item(i).removeChild(nodeList.item(0));
			}
			DOMOperations.changeTextContent((Element) joinPoints.item(i), value);
			validateOperation(joinPoints.item(i));
		}
	}

	/**
	 * This operation deletes all {@link Node}s in the {@link NodeList}.
	 * 
	 * @param joinPoints
	 *            - the {@link NodeList} thats contains the nodes that are to be
	 *            deleted
	 * @throws XMLWeaverException
	 *             if the operation is invalid according to
	 *             {@link #validateOperation(Node)}
	 */
	public void interpreteDelete(final NodeList joinPoints)
			throws XMLWeaverException {
		for (int i = 0; i < joinPoints.getLength(); i++) {
			final Node joinPoint = joinPoints.item(i);
			final Node parent = DOMOperations.deleteNode(joinPoint);
			validateOperation(parent);
		}
	}

	/**
	 * This operation inserts a textNode at the given position in each node in
	 * the {@link NodeList}
	 * 
	 * @param joinPoints
	 *            - the {@link NodeList} that contains the elementNodes that are
	 *            changed.
	 * @param position
	 *            - the position at which the textNode is going to be inserted
	 * @param valueList
	 *            - {@link NodeList} that contains the {@link Text} that is
	 * @throws XMLWeaverException
	 *             if the NodeList contains a {@link Node} that is not an
	 *             {@link Element} or if the operation is invalid according to
	 *             {@link #validateOperation(Node)}
	 */
	public void interpreteEnrichContent(final NodeList joinPoints,
			final int position, final ArrayList<Text> valueList)
			throws XMLWeaverException {
		for (int i = 0; i < joinPoints.getLength(); i++) {
			final Node insertedNode = valueList.get(i);
			final Node target = joinPoints.item(i);
			DOMOperations.checkNodeType(target, Node.ELEMENT_NODE);
			DOMOperations.insertNode(target, insertedNode, position);
			validateOperation(target);
		}
	}

	/**
	 * This operation replaces all textNodes of an {@link Element} with one
	 * {@link Element}. The operation is performed for each {@link Node} in the
	 * {@link NodeList} joinPoints with the corresponding {@link Element} of the
	 * {@link ArrayList} evaluatedTemplate.
	 * 
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the nodes on which the
	 *            operation will be performed.
	 * @param evaluatedTemplate
	 *            - an {@link ArrayList} that contains the corresponding element
	 *            that replaces the textNodes of the {@link Node}
	 * @throws XMLWeaverException
	 *             if a {@link Node} in the {@link NodeList} is not an
	 *             {@link Element} or if the operation is invalid. See
	 *             {@link DOMValidator#validateDocument(Document)} for more
	 *             Information.
	 */
	public void interpreteExpandElement(final NodeList joinPoints,
			final ArrayList<Element> evaluatedTemplate)
			throws XMLWeaverException {
		for (int i = 0; i < joinPoints.getLength(); i++) {
			DOMOperations.checkNodeType(joinPoints.item(i), Node.ELEMENT_NODE);
			final Node joinPoint = joinPoints.item(i);
			// delete all textNodes
			DOMOperations.changeTextContent((Element) joinPoint, null);
			validateOperation(joinPoint);
		}
		interpreteInsertElement(joinPoints, evaluatedTemplate, 0);
	}

	/**
	 * This operation inserts all children of a sourceComponent in the
	 * corresponding joinPoint (as a clone). There must be a sourceComponent for
	 * each joinPoint and they must not be the same node. The sourceComponents
	 * and joinPoints must be {@link Element}s. The children are inserted in the
	 * same order.
	 * 
	 * @param nameOfSourceID
	 *            - identifies a targetComponent
	 * @param nameOfTargetID
	 *            - identifies a sourceComponent
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the joinPoints
	 * @param sourceComponents
	 *            - a {@link NodeList} that contains the sourceComponents
	 * @throws XMLWeaverException
	 *             if one of the above preconditions is not met or the operation
	 *             is not valid. See
	 *             {@link DOMValidator#validateDocument(Document)} for more
	 *             Information.
	 */
	public void interpreteFillComponentByID(final String nameOfSourceID,
			final String nameOfTargetID, final NodeList joinPoints,
			final List<Element> sourceComponents) throws XMLWeaverException {
		if (joinPoints.getLength() == sourceComponents.size()) {
			for (int i = 0; i < joinPoints.getLength(); i++) {
				DOMOperations.checkNodeType(joinPoints.item(i), Node.ELEMENT_NODE);
				final Element joinPoint = (Element) joinPoints.item(i);
				getLogger().debug("Component selected, ID is '"
						+ joinPoint.getAttribute(nameOfTargetID) + "'.");
				Element source = sourceComponents.get(i);
				if (!source.getAttribute(nameOfSourceID).equals(
						joinPoint.getAttribute(nameOfTargetID))) {
					// sizes of both lists are expected to be equal, so there must be a match
					// that's not unique
					throw new XMLWeaverException(
							"There must be exactly one source component for each joinPoint---but more than one match was found for a joinPoint---identifier should be checked for uniqueness");
				} else {
					if (source.getOwnerDocument() != joinPoint
							.getOwnerDocument()) {
						source = (Element) joinPoint.getOwnerDocument()
								.importNode(source, true);
					}
					if (source == joinPoint) {
						throw new XMLWeaverException(
								"JoinPoint and sourceComponent must not be identical");
					}
					final NodeList sourceSubcomponents = source.getChildNodes();
					for (int j = 0; j < sourceSubcomponents.getLength(); j++) {
						final Node node = sourceSubcomponents.item(j)
								.cloneNode(true);
						joinPoint.appendChild(node);
					}
					validateOperation(joinPoint);
				}
			}
		} else {
			throw new XMLWeaverException(
					"There must be exactly one sourceComponent for each target component---#of targets:"
							+ joinPoints.getLength()
							+ "---#of sources:"
							+ sourceComponents.size());
		}
	}

	/**
	 * This Operation inserts an {@link Element} in a {@link Node} at the given
	 * position for each Node in the {@link NodeList}.
	 * 
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the nodes on which the
	 *            operation will be performed
	 * @param valueList
	 *            - an {@link ArrayList} that contains the {@link Element}s that
	 *            will be inserted in the corresponding Node of the
	 *            {@link NodeList}
	 * @param position
	 *            - The position at which an element is to be inserted in the
	 *            corresponding node.
	 * @throws XMLWeaverException
	 *             if a Node in the NodeList is not an {@link Element}, the
	 *             operation is invalid (See
	 *             {@link DOMValidator#validateDocument(Document)} for more
	 *             Information)or the position exceeds the number of children in
	 *             the node.
	 */
	public void interpreteInsertElement(final NodeList joinPoints,
			final ArrayList<Element> valueList, final int position)
			throws XMLWeaverException {
		for (int i = 0; i < joinPoints.getLength(); i++) {
			final Node insertedElement = valueList.get(i);
			final Node target = joinPoints.item(i);
			DOMOperations.checkNodeType(target, Node.ELEMENT_NODE, Node.DOCUMENT_NODE);
			DOMOperations.insertNode(target, insertedElement, position);
			validateOperation(target);
		}
	}

	/**
	 * This operation moves an {@link Element} to a target, i.e. it inserts the
	 * elementNode at the given Position in the target. See
	 * {@link #interpreteInsertElement(NodeList, ArrayList, int)} for further
	 * information.
	 * 
	 * @param source
	 *            - {@link Node} to be moved
	 * @param target
	 *            - {@link Node} that is the new parent for the source
	 * @param position
	 *            - the inserting position
	 * @throws XMLWeaverException
	 *             if source or target are not elementNodes, i.e. instances of
	 *             {@link Element} or the operation is not valid. See
	 *             {@link DOMValidator#validateDocument(Document)} for more
	 *             Information.
	 */
	public void interpreteMoveElement(final Node source, final Node target,
			final int position) throws XMLWeaverException {
		DOMOperations.checkNodeType(source, Node.ELEMENT_NODE);
		try {
			DOMOperations.checkNodeType(target, Node.ELEMENT_NODE,
					Node.DOCUMENT_NODE);
		} catch (final XMLWeaverException e) {
			throw new XMLWeaverException("Target node " + e.getMessage(),e);
		}
		final Element sourceParent = (Element) source.getParentNode();
		DOMOperations.insertNode(target, source, position);
		validateOperation(target);
		getLogger().debug("Deleting original node ....");
		validateOperation(sourceParent);

	}

	/**
	 * This operation reduces the content of a Number of {@link Node}s given as
	 * {@link NodeList}. For any Node (elementNode) of the NodeList it does the
	 * following: it removes a String "part" of each textNode's value or any
	 * textNodes for <code>part==""</code>.
	 * 
	 * @param joinPoints
	 *            - a {@link NodeList} that contains the Nodes to which this
	 *            operation will be applied
	 * @param part
	 *            - the part of the textNodes that is supposed to be removed
	 * @throws XMLWeaverException
	 *             if the NodeList contains a {@link Node} that is not an
	 *             {@link Element} or if the operation is invalid according to
	 *             {@link #validateOperation(Node)}
	 */
	public void interpreteReduceContent(final NodeList joinPoints,
			final String part) throws XMLWeaverException {
		for (int i = 0; i < joinPoints.getLength(); i++) {
			DOMOperations.checkNodeType(joinPoints.item(i), Node.ELEMENT_NODE);
			final Element joinPoint = (Element) joinPoints.item(i);
			// get all children, select all textNodes and remove them or part of
			// their textContent
			final NodeList children = joinPoint.getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				if (children.item(j).getNodeType() == Node.TEXT_NODE) {
					if (part == null || part.contentEquals("")) {
						joinPoint.removeChild(children.item(j));
					} else {
						final String oldText = children.item(j).getNodeValue();
						final String newText = oldText.replace(part, "");
						children.item(j).setTextContent(newText);
					}
				}
			}
			getLogger().debug("Reducing content of element "
					+ joinPoint.getNodeName() + ".");
			validateOperation(joinPoint);
		}
	}

	/**
	 * This operation uses the {@link DOMValidator} to validate an operation,
	 * i.e. it validates the resulting {@link Node} of an operation. If the node
	 * is invalid an {@link XMLWeaverException} is thrown.
	 * 
	 * @param node
	 *            {@link Node} that is the result of an operation.
	 * @throws XMLWeaverException
	 *             if the node is invalid. See
	 *             {@link DOMValidator#validateDocument(Document)} for more
	 *             Information.
	 */
	private void validateOperation(final Node node) throws XMLWeaverException {
		if (validateOperations) {
			DOMOperations.checkNodeType(node, Node.DOCUMENT_NODE,
					Node.ELEMENT_NODE, Node.ATTRIBUTE_NODE);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				validator.validateElement((Element) node);
			} else if (node.getNodeType() == Node.ELEMENT_NODE) {
				validator.validateDocument((Document) node);
			} else {
				validator.validateElement(((Attr) node).getOwnerElement());
			}
		}
	}

}
