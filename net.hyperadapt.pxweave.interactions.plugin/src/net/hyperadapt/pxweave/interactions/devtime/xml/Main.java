package net.hyperadapt.pxweave.interactions.devtime.xml;

//import interaction.algorithm.containment.PrintTree;
//import interaction.triggersinhibits.TriggeringChecker;
//import interaction.triggersinhibits.TriggersAndInhibition;

//import java.io.Console;
//import java.util.ArrayList;
//import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.eclipse.core.internal.resources.RegexFileInfoMatcher;

//import net.hyperadapt.pxweave.aspects.ast.ChangeValue;

public class Main {

//	private static String aspectfile = "adaptationaspects.xml";
//	private static String aspectfile = "AspectChangeOrder.xml";
	@SuppressWarnings("unused")
	private static String configfile = "ConfigChangeOrder.xml";
//	private static String aspectfile = "testdata/InsertSomething.xml";

	/**
	 * @param argsj
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		String s = "//aco:AmaSetComponent[@id='j91t3sk7']/aco:SubComponents/aco:AmaAccessElement/aco:SubComponents/aco:AmaImageComponent[@id='aqwrz6if']/aco:MetaInformation/amet:ImageMetaData/amet:source/@text";
		String t = "//aco:AmaSetComponent[@id='j91t3sk7']/aco:SubComponents/aco:AmaAccessElement/aco:SubComponents/aco:AmaImageComponent[@id='aqwrz6if']/aco:MetaInformation/amet:ImageMetaData/amet:source[@text='images/home.png']";
		String u = "hallo $name with $surname";
		
		String a = "(\\S)*(amet:source\\[@text='images/home.png'\\])(\\S)*";
		String b = "(\\S)*(amet:source\\[@text=')(\\S)*('\\])(\\S)*";
//		 = "(\\S)*(amet:source[@text='(\\S)*'])(\\S)*";
		String c = "(\\S)*\\[";
		String d = "(\\S)*(\\()(\\S)*(\\))(\\S)*";
		String e = "(\\$\\w+)";
		
		
		
		System.out.println(u.matches(e));
//		Console console = System.out;
		
		System.out.println(checkForVariable(u));
		
//		List<String>fl = new ArrayList<String>();
//		fl.add(aspectfile);
//		PipelineStations.setFile("testdata/sitemap.xmap");
//
//		
////		AdviceTracking container = new AdviceTracking(fl);
////		
////		List<AdviceToCheck> la = container.getAllAdvices();
//		
////		for (AdviceToCheck atc : la) {
////			if (atc.getAdviceType() instanceof ChangeValue){
////				System.out.println(((ChangeValue) atc.getAdviceType()).getValue().getTextTemplate().getValue());
////				
////			}
//////			System.out.println(atc.getAdviceType().getPointcut().getValue());
////		}
//
////		List<AdviceError> err = container.getErrors();
////		
////		TriggersAndInhibition ti = new TriggersAndInhibition(la, err);
////		List<AdviceError> errTI = ti.getAllAfterEffects();
////		
////		
////		
////		for (AdviceError ae : errTI) {
////			if (ae.getSecondAdvice() == null){
////				
////				System.out.println(" possible " + ae.isDependent() + " found, becaus e of variable " + ae.getFirstAdvice().getExtraContentIfExisting());
////			}
////		}
//		
////		System.out.println(TriggeringChecker.linearize("/aco:SubComponents/aco:AmaImageComponent[@id='aqwrz6if']/aco:MetaInformation/amet:ImageMetaData/amet:source[./text()='dhdj']/text()"));
//		String p = "/aco:SubComponents/aco:AmaImageComponent[@id='aqwrz6if']/aco:MetaInformation/amet:ImageMetaData/amet:source/@id";
//		System.out.println("p "+p);
//		String linearizedXPath = TriggeringChecker.linearize(p);
//		String lastPartOfLinearizedXPath = linearizedXPath.substring(linearizedXPath.lastIndexOf("/")+1, linearizedXPath.length());
//		
//		String attr ="nichts";
//		if (lastPartOfLinearizedXPath.matches("(attribute)::[a-z]*") || lastPartOfLinearizedXPath.matches("(@)[a-z]*")){
//			// wenn das letzte Teilstück entweder attribute::?? oder @?? ist 
//			attr = lastPartOfLinearizedXPath+"='HalloAttribute'";
//		}
//		System.out.println("letzte stück "+lastPartOfLinearizedXPath);
//		System.out.println(" wird ersetzt "+attr);
//		
//		String[] sa = p.split(lastPartOfLinearizedXPath);
//		
//		String newXPath = "";
//		for (int i = 0; i < sa.length; i++){
//			newXPath += sa[i];
//			newXPath +=lastPartOfLinearizedXPath;
//		}
//		
//		System.out.println(newXPath);
//		
//		newXPath = newXPath.substring(0, newXPath.lastIndexOf(lastPartOfLinearizedXPath));
//		newXPath += attr;
////		newXPath += sa[sa.length-1];
//		
//		System.out.println(p);
//		
//		
////		String newXpath = p.replace(, attr);
//		System.out.println(newXPath);
//		
//		PrintTree.printTheTree(p);
//		PrintTree.printTheTree(newXPath);
		
//		System.out.println(lastPartOfLinearizedXPath);
		// Liste aller advices die eine Wechselwirkung haben
//		List<AdviceError> adviceInteractions = container.getErrors();
		
		
		
//		try {
			
//			File a = new File(aspectfile);
//			File c = new File(configfile);
//			ContainmentPreworker cp = new ContainmentPreworker(configfile);
//			System.out.println(a.exists());

//			URI aspectURL = URI.create(aspect);
			//Aspect aspect = JAXBHelper.unmarshallAspect(aspectfile);
//			JAXBContext context = JAXBContext.newInstance(contextName);
//			Unmarshaller loader = context.createUnmarshaller();
//			Object o = loader.unmarshal(resourceURL);
//			if(o!=null){
			
//				return o instanceof JAXBElement?((JAXBElement<Object>)o).getValue():o;
//			}
			
//			System.out.println();
			
//		} catch (XMLWeaverException e) {
//			e.printStackTrace();
//		}
	}
	
	private static String checkForVariable(String value) {
		String variable = "";
		Pattern pattern = Pattern.compile("(\\$\\w+)");
		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {
			variable += matcher.group() + ", ";
		}

		return variable.substring(0, variable.length()-2);
	}

}
