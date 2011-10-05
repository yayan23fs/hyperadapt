package net.hyperadapt.pxweave.interactions.devtime.xml;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NodeList;


//import algorithm.TestContainment;

import net.hyperadapt.pxweave.XMLWeaverException;
import net.hyperadapt.pxweave.aspects.ast.Advice;
import net.hyperadapt.pxweave.aspects.ast.AdviceGroup;
import net.hyperadapt.pxweave.aspects.ast.Aspect;
import net.hyperadapt.pxweave.aspects.ast.BasicAdvice;
import net.hyperadapt.pxweave.aspects.ast.ProgrammaticScope;
import net.hyperadapt.pxweave.interactions.algorithm.containment.ParseFunctionException;
import net.hyperadapt.pxweave.interactions.algorithm.containment.ParseQueryException;
import net.hyperadapt.pxweave.interactions.algorithm.containment.TestContainment;
import net.hyperadapt.pxweave.interactions.algorithm.intersection.AutomatonPreworker;
import net.hyperadapt.pxweave.interactions.algorithm.intersection.ProductAutomaton;
import net.hyperadapt.pxweave.interactions.devtime.patternmatrix.ConflictMatrix;
import net.hyperadapt.pxweave.util.JAXBHelper;

/**
 * class to find all advice and errors
 * 
 * @author danielkadner
 *
 */
public class AdviceTracking {
	
	private boolean debug = DebugMode.debug;
//	private boolean debug = true;
	private List<AdviceToCheck> allAdvices;
	private List<String> allAspectFiles;
	private List<AdviceToCheck> staticAdvices;
	private List<AdviceToCheck> documentAdvices;
	private ArrayList<AdviceError> errorList;
	private ConflictMatrix cm = ConflictMatrix.getInstance();
	private NodeList pipeStations = PipelineStations.getStations();

	/**
	 * constructor to create advice and error tracking
	 * @param allApectFiles a list with all used aspect files
	 */
	public AdviceTracking(List<String> allApectFiles) {
		this.allAdvices = new ArrayList<AdviceToCheck>();
		this.allAspectFiles = allApectFiles;
		this.staticAdvices = new ArrayList<AdviceToCheck>();
		this.documentAdvices = new ArrayList<AdviceToCheck>();
//		cm.showAllErrors();
		readOutAllAdvices();
	}

	/**
	 * method to add new aspect file for tracking 
	 * @param filename
	 */
	public void addOrUpdateAspectFile(String filename) {
		for (String s : allAspectFiles) {
			if (s.equalsIgnoreCase(filename)) {
				readOutAllAdvices();
				return;
			}
		}
		allAspectFiles.add(filename);
		readOutAllAdvices();
	}

	/**
	 * method to read out all advices from the given aspectfiles
	 */
	public void readOutAllAdvices() {
		allAdvices = new ArrayList<AdviceToCheck>();
		List<AdviceGroup> currentAdviceGroup;
		List<BasicAdvice> baList;

		try {
			for (String s : allAspectFiles) {
				File aspectFile = new File(s);
				//TODO Check if URL resolving works
				Aspect aspect = null;
				try {
					aspect = JAXBHelper.unmarshallAspect(aspectFile.toURI().toURL());
				} catch (MalformedURLException e) {
					e.printStackTrace();
					return;
				}
				currentAdviceGroup = aspect.getAdviceGroup();
				for (AdviceGroup ag : currentAdviceGroup) {
					Advice advice = ag.getAdvices();
					baList = advice.getChooseVariantOrChangeOrderOrChangeValue();
					for (BasicAdvice ba : baList) {
						AdviceToCheck atc = new AdviceToCheck(s, aspect, ag, ba);
						if (isXPathWithoutFunctions(atc.getXPath()))
							staticAdvices.add(atc);
						else
							documentAdvices.add(atc);
						allAdvices.add(atc);
					}

				}
				// aspects.put(s, a);
				// allAspects.add(a);
			}

		} catch (XMLWeaverException e) {
			e.printStackTrace();
		}
		findAllErrors();
	}

	
	private boolean isXPathWithoutFunctions(String path) {
		if (path.contains("("))
			return false;
		return true;
	}

	/**
	 * method to find all errors (including triggers & inhibits)
	 */
	public void findAllErrors() {
		String p, q;
		if (debug) {
			System.out.println(">>>> Find errors:");
		}
		// entweder null wenn kein Wert gesetzt, oder mit spezifischem Wert
		String boolExprP, boolExprQ;
		List<ProgrammaticScope> pPCP, pPCQ;
		errorList = new ArrayList<AdviceError>();
		for (int i = 0; i < allAdvices.size() - 1; i++) {
			AdviceToCheck atcFirst = allAdvices.get(i);
			p = atcFirst.getXPath();
			if (atcFirst.getAdviceGroup().getDepends() == null)
				boolExprP = null;
			else
				boolExprP = atcFirst.getAdviceGroup().getDepends().getBoolExpr();
			if (atcFirst.getAdviceGroup().getScope() == null)
				pPCP = null;
			else
				pPCP = atcFirst.getAdviceGroup().getScope().getJoinpoint();

			if (debug) {
				System.out.println("for advice " + atcFirst.getName() + " from aspect " + atcFirst.getAspect().getName() + " (" + atcFirst.getFilename() + ")" + " XPath: " + atcFirst.getXPath());
				printPPC(pPCP);
				System.out.println("\tboolExp " + boolExprP);
			}

			for (int j = i + 1; j < allAdvices.size(); j++) {
				AdviceToCheck actSecond = allAdvices.get(j);
				q = actSecond.getXPath();

				// nur wenn in ConfliktMatrix zwischen den beiden Primitiven
				// ein(e) Fehler/Warnung besteht
				if (cm.isConflictBetween(atcFirst.getName(), actSecond.getName())) {

					// nur wenn keine Funktionen vorhanden sind
					if (isXPathWithoutFunctions(p) && isXPathWithoutFunctions(q)) {

						if (actSecond.getAdviceGroup().getDepends() == null)
							boolExprQ = null;
						else
							boolExprQ = actSecond.getAdviceGroup().getDepends().getBoolExpr();
						if (actSecond.getAdviceGroup().getScope() == null)
							pPCQ = null;
						else
							pPCQ = actSecond.getAdviceGroup().getScope().getJoinpoint();

						if (debug) {
							System.out.println("\twith Advice " + actSecond.getName() + " from aspect " + actSecond.getAspect().getName() + " (" + actSecond.getFilename() + ")" + " XPath: "
									+ actSecond.getXPath());
							printPPC(pPCQ);
							System.out.println("\t\tboolExp " + boolExprQ);
						}

						// vergleiche ob beim selben PPC (after/before)
						List<ProgrammaticScope> samePPC = checkForSamePPC(pPCP, pPCQ);
						if (samePPC != null) {
							if (debug) {
								System.out.println("\t\t-> same PPC");
							}
							// vergleiche ob Depends bei beiden gleich ist
							if (checkForSameBoolExpr(boolExprP, boolExprQ)) {
								if (debug) {
									System.out.println("\t\t-> same depends");
								}

								// checke auf containment
								if (checkForErrors(TestContainment.testContainment(p, q))) {
									if (debug) {
										System.out.println("\t\t->  have containment error");
									}
									AdviceError ae = new AdviceError(atcFirst, actSecond);
									ae.setCommonPPCs(samePPC);
									//Status des AdviceError abfragen und speichern
									if (cm.getStatusFor(atcFirst.getName(), actSecond.getName()).equals("error")) ae.setIsError(true);
									else ae.setIsError(false);
									errorList.add(ae);
								} else {
									if (debug) {
										System.out.println("\t\t->  have NOT containment error");
//										System.out.println(" HIER SAGS MAL "+ p + "       "+q);
									}
									// checke auf intersection
									AutomatonPreworker ap = new AutomatonPreworker();
									try {
										ap.createAutomatons(p, q);
									} catch (ParseQueryException e) {
										e.printStackTrace();
									} catch (ParseFunctionException e) {
										e.printStackTrace();
									} catch (IOException e) {
										e.printStackTrace();
									}
									ProductAutomaton pa = new ProductAutomaton(ap.getAutomatonP(), ap.getAutomatonQ());
									if (pa.isIntersection()) {
										if (debug) {
											System.out.println("\t\t->  have intersection error");
										}
										AdviceError ae = new AdviceError(atcFirst, actSecond);
										ae.setCommonPPCs(samePPC);
										//Status des AdviceError abfragen und speichern
										if (cm.getStatusFor(atcFirst.getName(), actSecond.getName()).equals("error")) ae.setIsError(true);
										else ae.setIsError(false);
										errorList.add(ae);
									} else {
										if (debug) {
											System.out.println("\t\t->  have NOT intersection error");
										}
									}
								}
							} else {
								if (debug) {
									System.out.println("\t\t-> NOT same depends");
								}
							}

						} else {
							if (debug) {
								System.out.println("\t\t-> NOT same PPC");
							}
						}
					}
				}
			}
		}
		// return errorList;
	}

	private List<ProgrammaticScope> checkForSamePPC(List<ProgrammaticScope> a, List<ProgrammaticScope> b) {
		List<ProgrammaticScope> errorList = new ArrayList<ProgrammaticScope>();

		if (a.size() == 0)
			return b;
		if (b.size() == 0)
			return a;

		for (ProgrammaticScope aPPC : a) {
			for (ProgrammaticScope bPPC : b) {
				// wenn Name Ÿbereinstimmt
				if (aPPC.getName().equals(bPPC.getName()) &&
				// wenn Location passt (after/before)
						aPPC.getLocator().toString().equals(bPPC.getLocator().toString())) {
					errorList.add(aPPC);
				}
				
				//wenn after vor einem before des nachfolgenden
				//wenn aPPC(after) in sitemap/transformers direkt vor bPPC(before)
//				System.out.println("PIPESTATIONS LENGTH "+pipeStations.getLength());
				for (int i = 0; i < pipeStations.getLength()-1; i++){
					if (aPPC.getName().equals(pipeStations.item(i).getAttributes().getNamedItem("name").getNodeValue()) &&
							bPPC.getName().equals(pipeStations.item(i+1).getAttributes().getNamedItem("name").getNodeValue()) &&
							aPPC.getLocator().toString().equalsIgnoreCase("after") &&
							bPPC.getLocator().toString().equalsIgnoreCase("before")){
						errorList.add(aPPC);
					}
					//wenn bPPC(after) in sitemap/transformers direkt vor aPPC(before)
					if (bPPC.getName().equals(pipeStations.item(i).getAttributes().getNamedItem("name").getNodeValue()) &&
							aPPC.getName().equals(pipeStations.item(i+1).getAttributes().getNamedItem("name").getNodeValue()) &&
							bPPC.getLocator().toString().equalsIgnoreCase("after") &&
							aPPC.getLocator().toString().equalsIgnoreCase("before")){
						errorList.add(aPPC);
					}			
				}
			}
		}
		if (errorList.size() == 0)
			return null;
		return errorList;
	}

	private boolean checkForSameBoolExpr(String a, String b) {
		if (a == null)
			return true;
		if (b == null)
			return true;
		if (a.equals(b))
			return true;
		return false;
	}

	public List<AdviceError> getErrors() {
		return errorList;
	}

	public List<AdviceToCheck> getAllAdvices() {
		return allAdvices;
	}


	public boolean checkForErrors(boolean[] in) {
		for (int i = 0; i < in.length; i++) {
			if (in[i] == true)
				return true;
		}
		return false;
	}

	private void printPPC(List<ProgrammaticScope> a) {
		String s = "";
		for (ProgrammaticScope ppc : a) {
			s += ppc.getName() + "/" + ppc.getLocator().toString() + " ";
		}
		if (s.length() == 0)
			System.out.println("\tPPC null");
		else
			System.out.println("\t\tPPC " + s);
	}
	
	public boolean containsAdviceWithFunctions(){
		for (AdviceToCheck atc : allAdvices) {
			String xp = atc.getXPath();
//			System.out.println("XP "+xp);
			if (xp.matches("(\\S)*(\\()(\\S)*(\\))(\\S)*")){
			String[] xpA = xp.split("(\\()(\\S)*(\\))");
//			System.out.println(xpA);
			for (String s : xpA) {
//				System.out.println("TEILSTRING "+s);
				if (s.split("'").length % 2 == 1) return true;
			}
			}
		}
		
		return false;
	}
}
