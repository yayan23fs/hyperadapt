package net.hyperadapt.pxweave.interactions.algorithm.containment;


import java.io.IOException;
import java.io.StringReader;
import java.lang.Character;
import java.util.Stack;

import net.hyperadapt.pxweave.interactions.devtime.xml.DebugMode;

/*
 * Created on 10.03.2005
 */

/**
 * The class Parser.java changes an XPath expression into a boolean Tree Pattern.
 * @author       khaled
 */
public class Parser {

	/** Laying out attributes of the class **/
	private Node current;

	private char ch;

	private int c;

	private StringReader sr;

	private Stack<Node> stack = new Stack<Node>();

	private String chEnd = "";

	private boolean debug = DebugMode.debug;

	/**
	 * Transforms an XPath-expression into a boolean Tree Pattern.
	 * 
	 * @param query
	 *            the xpath-expression as a String
	 * 
	 * @return node the xpath-expression as a tree
	 */
	public Node parseQuery(String query) throws ParseQueryException, ParseFunctionException, IOException {

		if (query.equals("")) {
			throw new ParseQueryException("enter Query", chEnd);
		}
		sr = new StringReader(query);
		Node root = new Node("*");
		c = sr.read();
		ch = (char) c;
		chEnd = chEnd + ch;
		if (ch == '/') {
			c = sr.read();
			ch = (char) c;
			chEnd = chEnd + ch;
			if (ch == '/') {
				c = sr.read();
				ch = (char) c;
				chEnd = chEnd + ch;
				if (!Character.isLetter(ch) && (ch != '*')) {

					throw new ParseQueryException("Syntax Error : character expected, e.g. //a", chEnd);
				} else {
					String[] label = readLabel(false);
					Node node = new Node(label[0]);
					if (label[1] != null)
						node.setNamespace(label[1]);
					Edge edge = new Edge(node, true);
					node.setEdge(edge);
					root.addNode(node);
					current = node;
					if (debug)
						System.out.println("node " + node.getLabel() + " is descendent of node " + current.getLabel());
				}
			} else {
				if (!Character.isLetter(ch) && (ch != '*')) {

					throw new ParseQueryException("Syntax Error : character expected, e.g. /a", chEnd);
				}
				String[] label = readLabel(false);
				Node node = new Node(label[0]);
				if (label[1] != null)
					node.setNamespace(label[1]);
				Edge edge = new Edge(node, false);
				node.setEdge(edge);
				root.addNode(node);
				current = node;
				if (debug)
					System.out.println("node " + node.getLabel() + " is child of node " + current.getLabel());
			}
		} else {
			if (!Character.isLetter(ch) && (ch != '*')) {

				throw new ParseQueryException("Syntax Error : begin with /, // or character", chEnd);
			}
			String label = readLabel(false)[0];
			Node node = new Node(label);
			Edge edge = new Edge(node, false);
			node.setEdge(edge);
			root.addNode(node);
			current = node;
			if (debug)
				System.out.println("node " + node.getLabel() + " is child of node " + current.getLabel());
		}
		stack.clear();

		while (c != -1) {
			switch (ch) {
			case '/': {
				readEdge(sr);
				break;
			}
			case '[': {
				readOpenQualifier(sr);
				break;
			}
			case ']': {
				readEndQualifier();
				c = sr.read();
				ch = (char) c;
				chEnd = chEnd + ch;
				break;
			}
			default: {
				// for
				// System.err.println();

				throw new ParseQueryException("Syntax Error :please in this format, a/*[b//c]", chEnd);
			}
			}
		}
		if (!stack.empty()) {

			throw new ParseQueryException("Syntax Error : ']' expected, e.g. [a[b]]", chEnd);
		}

		Node node = new Node("#");
		Edge edge = new Edge(node, false);
		node.setEdge(edge);
		current.addNode(node);
		if (debug)
			System.out.println("node " + node.getLabel() + " is child of node " + current.getLabel());
		sr.close();
		return root;
	}

	/**
	 * This methode is called by the methode parseQuery() whenever an edge('/')
	 * character is found.
	 * 
	 * @param sr
	 *            the StringReader
	 * 
	 * @return true if there is no syntax errors and false otherwise
	 */
	private void readEdge(StringReader sr) throws ParseQueryException, ParseFunctionException {
		try {
			ch = (char) sr.read();
			chEnd = chEnd + ch;
			// System.out.println(ch);
			if (ch == '/') {
				ch = (char) sr.read();
				chEnd = chEnd + ch;
				// System.out.println(ch);
				if (!Character.isLetter(ch) && (ch != '*') && (ch != '@')) {
					throw new ParseQueryException("Syntax Error : character expected, e.g. //b ", chEnd);
				}

				if (ch == '@') {
					readAttribute(sr, false, true);
				} else {
					String[] label = readLabel(false);
					Node node = new Node(label[0]);
					if (label[1] != null)
						node.setNamespace(label[1]);
					Edge edge = new Edge(node, true);
					node.setEdge(edge);
					current.addNode(node);
					if (debug)
						System.out.println("node " + node.getLabel() + " is descendent of node " + current.getLabel());
					current = node;
				}
			} else if (Character.isLetter(ch) || (ch == '*')) {
				String[] label = readLabel(false);
				Node node = new Node(label[0]);
				if (label[1] != null)
					node.setNamespace(label[1]);
				Edge edge = new Edge(node, false);
				node.setEdge(edge);
				current.addNode(node);
				if (debug)
					System.out.println("node " + node.getLabel() + " is child of node " + current.getLabel());
				current = node;
			} else if (ch == '@') {
				readAttribute(sr, true, false);
			} else {

				throw new ParseQueryException("Syntax Error (global) : character expected, e.g. /c ", chEnd);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This methode is called by the methode parseQuery() whenever an edge('[')
	 * character is found.
	 * 
	 * @param sr
	 *            StringReader
	 * 
	 * @return true if there is no syntax errors and false otherwise
	 */
	private void readOpenQualifier(StringReader sr) throws ParseQueryException, ParseFunctionException {
		Node act = current;
		stack.push(act);
		try {
			ch = (char) sr.read();
			chEnd = chEnd + ch;
			if (ch == '.') {
				ch = (char) sr.read();
				chEnd = chEnd + ch;
				if (ch != '/') {

					throw new ParseQueryException("Syntax Error : '/' expected, e.g. a[.//b]", chEnd);
				}
				ch = (char) sr.read();
				chEnd = chEnd + ch;
				if (ch  == '@'){
					readAttribute(sr, false, false);
				} else	if (ch != '/') {
					String[] label = readLabel(false);
					Node node = new Node(label[0]);
					if (label[1] != null)
						node.setNamespace(label[1]);
					Edge edge = new Edge(node, false);
					node.setEdge(edge);
					current.addNode(node);
//					current = node;
					return;
//					throw new ParseQueryException("Syntax Error : '/' expected, e.g. a[.//b]", chEnd);
				}
//				ch = (char) sr.read();
//				chEnd = chEnd + ch;
//				if (!Character.isLetter(ch) && (ch != '*')) {
//
//					throw new ParseQueryException("Syntax Error : character expected, e.g. a[.//c]", chEnd);
//				}
				if (ch == ']') return;
				String[] label = readLabel(false);
				Node node = new Node(label[0]);
				if (label[1] != null)
					node.setNamespace(label[1]);
				Edge edge = new Edge(node, true);
				node.setEdge(edge);
				current.addNode(node);
				current = node;
				if (debug)
					System.out.println("node " + node.getLabel() + " is descendent of node " + current.getLabel());
			} else if (ch == '@') {
				readAttribute(sr, false, false);
			} else {
				int position = -1;
				if (Character.isDigit(ch)) {
					position = Integer.parseInt(String.valueOf(ch));
				} else if (!Character.isLetter(ch) && (ch != '*')) {

					throw new ParseQueryException("Syntax Error : character or '.' expected, e.g. a[b], a[.//b]", chEnd);
				}
				String[] label = readLabel(false);
				Node node = new Node(label[0]);
				node.setPosition(position);
				if (label[1] != null)
					node.setNamespace(label[1]);
				Edge edge = new Edge(node, false);
				node.setEdge(edge);
				current.addNode(node);
				current = node;
				if (debug)
					System.out.println("node " + node.getLabel() + " is child of node " + current.getLabel());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This methode is called by the methode parseQuery() whenever an edge(']')
	 * character is found.
	 * 
	 * @return true if there is no syntax errors and false otherwise
	 */
	private void readEndQualifier() throws ParseQueryException {
		if (stack.empty()) {
			throw new ParseQueryException("Syntax Error : unexpected ']' ", chEnd);
		} else {
			current = (Node) stack.pop();
		}
	}

	/**
	 * This methode is called by the methode parseQuery() whenever a character
	 * is found. It creates a String from a sequence of characters.
	 * 
	 * @return String the label made from a sequence of characters
	 */
	private String[] readLabel(boolean isAttribute) throws ParseFunctionException{
		String[] labelAndNS = new String[2];
		if (ch == '*') {
			String label = String.valueOf(ch);
			try {
				c = sr.read();
				ch = (char) c;
				chEnd = chEnd + ch;
			} catch (IOException e) {
				e.printStackTrace();
			}
			labelAndNS[0] = label;
			return labelAndNS;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(ch);
		while (true) {
			try {
				c = sr.read();
				ch = (char) c;
				chEnd = chEnd + ch;
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (c == -1 || (ch == '[') || (ch == ']')) {
				break;
			}
			if (!isAttribute){
				if (ch == '/') break;
			}
			if (ch == ':') {
				labelAndNS[1] = buffer.toString();
				buffer = new StringBuffer();
				try {
					c = sr.read();
				} catch (IOException e) {
					e.printStackTrace();
				}
				ch = (char) c;
				chEnd = chEnd + ch;
			}
			if (ch == '=') {
				try {
					c = sr.read();
				} catch (IOException e) {
					e.printStackTrace();
				}
				ch = (char) c;
				chEnd = chEnd + ch;
				break;
			}
			buffer.append(ch);
		}
		String label = new String(buffer);
		if (label.contains("(") || label.contains(")")) throw new ParseFunctionException("Functional Error: can't handle functions in XPath without a Document ", chEnd);
		labelAndNS[0] = label;
		return labelAndNS;
	}

	private void readAttribute(StringReader sr, boolean child, boolean descendent) throws ParseFunctionException{
		try {
			ch = (char) sr.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		chEnd = chEnd + ch;
		String name = readLabel(true)[0];
		if (name.equals("*"))
			try {
				ch = (char) sr.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
		String value = readLabel(true)[0];
//		System.out.println(value);
		if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
			value = value.substring(1, value.length() - 1);
			if (debug) {
				System.out.println("ein Value");
			}
		} else {
			value = "*"; //umŠndern in ?
			
			if (debug) {
				System.out.println("eine funktion");
			}
//			throw new ParseFunctionException("Functional Error: can't handle functions in XPath without a Document ", chEnd);
		}
		if (child) {
			if (descendent) {
				// child an descendent at same time
				System.err.println("something went wrong");
			} else {
				// if its child
//				String label = "*";
//				Node node = new Node(label);
//				Edge edge = new Edge(node, false);
//				node.setEdge(edge);
//				node.addAttribute(name, value);
//				String oldCurrent = current.getLabel();
//				current.addNode(node);
				current.addAttribute(name, value);
//				if (debug)
//					System.out.println("node " + current.getLabel() + " is child of node " + current.getLabel());
				if (debug)
					System.out.println("Attribute: " + name + "=" + value + " added in node " + current.getLabel());
			}
		} else {
			if (descendent) {
				// if its descendent
				String label = "*";
				Node node = new Node(label);
				Edge edge = new Edge(node, true);
				node.setEdge(edge);
				node.addAttribute(name, value);
				current.addNode(node);
				String oldCurrent = current.getLabel();
				current = node;
				if (debug)
					System.out.println("node " + node.getLabel() + " is descendent of node " + current.getLabel());
				if (debug)
					System.out.println("Attribute: " + name + "=" + value + " added in node " + current.getLabel() + " (is descendent of " + oldCurrent + ")");
			} else {

				current.addAttribute(name, value);
				if (debug)
					System.out.println("Attribute: " + name + "=" + value + " added in node " + current.getLabel());
			}
		}
	}

	/**
	 * Returns the longest number of *-nodes forming a path consisting
	 * exclusively of child edges
	 * 
	 * @return starLength the longest star nodes sequence
	 */
	public int getStarLength(String query) {
		int starLength = 0;
		int tmp = 0;
		StringReader sr = new StringReader(query);
		try {
			for (int ch; (ch = sr.read()) != -1;) {
				if (ch == '*') {
					tmp++;
					ch = sr.read();
					if (ch != '/') {
						if (tmp > starLength)
							starLength = tmp;
						tmp = 0;
					}
				} else {
					if (tmp > starLength)
						starLength = tmp;
					tmp = 0;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return starLength;

	}
}
