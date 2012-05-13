package net.hyperadapt.pxweave;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import net.hyperadapt.pxweave.aspects.AspectWeaver;
import net.hyperadapt.pxweave.interpreter.IInterpreterArgument;
import net.hyperadapt.pxweave.interpreter.InterpreterArgument;
import net.hyperadapt.pxweave.logger.LoggerConfiguration;

/**
 * Main-method invokes weaver from console
 */
public class Main {
	
	private static final String usage = "Input arguments must be structured like this:"
		+ "in:path/input.xml out:path/result.xml coreID:identifierForThisInputDocument";


	/**
	 * @param args
	 *            - see {@link AspectWeaver#weave(String[])}
	 */
	public static void main(final String[] args) {
		//TODO add extra parameter for overriding base URI
		File currentDir = new File(".");
		URI baseURI = currentDir.toURI();
		try {
			IEnvironment env = Environment.create(baseURI);
			ArrayList<IInterpreterArgument> arguments = evaluateStringArgs(args);
			AspectWeaver.weave(env,arguments);
		} catch (final XMLWeaverException e) {
			LoggerConfiguration.instance().getLogger(Main.class).error("Could not weave aspects.",e);
		}
	}
	
	/**
	 * This method evaluates {@link String}s given as arguments and creates the
	 * corresponding {@link InterpreterArgument}s.
	 * 
	 * @param args
	 *            - Strings that are structured as follows:
	 *            "in:path1/input.xml, out:path2/result.xml, coreId:core"
	 * @return an {@link ArrayList} of {@link InterpreterArgument}s
	 * @throws IXMLWeaverException
	 */
	public static ArrayList<IInterpreterArgument> evaluateStringArgs(
			final String... args) throws XMLWeaverException {
		final ArrayList<IInterpreterArgument> interpreterArgs = new ArrayList<IInterpreterArgument>();
		
		if (args.length % 3 != 0 || args.length == 0) {
			throw new XMLWeaverException(usage);
		}
		for (int i = 0; i < args.length; i += 3) {
			final String in = args[i].replace("in:", "");
			final File inFile = new File(in);
			final String out = args[i + 1].replace("out:", "");
			final File outFile = new File(out);
			final String core = args[i + 2].replace("coreID:", "");

			if (!inFile.canRead()) {
				throw new XMLWeaverException("Cannot read core document from '" + inFile.toURI() + "'. ["
						+ usage + "]");
			}
			final IInterpreterArgument fileArg = new InterpreterArgument(
					inFile, outFile, core);
			interpreterArgs.add(fileArg);
		}
		return interpreterArgs;

	}

}
