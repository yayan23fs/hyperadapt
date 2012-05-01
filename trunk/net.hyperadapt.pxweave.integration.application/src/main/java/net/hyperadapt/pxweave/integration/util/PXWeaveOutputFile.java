package net.hyperadapt.pxweave.integration.util;

import java.io.File;

/**
 * Extension of java.io.File to generate an alternative absolute path after the
 * aspect evaluation process of PX-Weave. Its possible to create a unique id
 * within the file name to cache requests by an adapted id from the weaving
 * cache.
 * 
 * @author Martin Lehmann
 * 
 */
public class PXWeaveOutputFile extends File {

	private static final long serialVersionUID = 3374190844732179086L;
	private String site;
	private WeavingCache cache;

	/**
	 * Constructor, which gets the path of the application context.
	 * 
	 * @param pathname
	 *            - path of the application context
	 */
	private PXWeaveOutputFile(String pathname) {
		super(pathname);
	}

	/**
	 * This constructor gets the path of the application context, an allocated
	 * file and a reference to the weaving cache.
	 * 
	 * @param pathname
	 *            - path of the application context
	 * @param aSite
	 *            - path of the allocated file
	 * @param aCache
	 *            - weaving cache to generate an unique id within the file name
	 */
	public PXWeaveOutputFile(String pathname, String aSite, WeavingCache aCache) {
		this(pathname);
		site = aSite;
		cache = aCache;
	}

	/**
	 * The method overrides the standard mechanism from java.file.io to generate
	 * an unique id within the file name. The adapted file is stored under this
	 * name after the aspect evaluation process from PX-Weave.
	 */
	@Override
	public String getAbsolutePath() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getPath());
		buffer.append(File.separator);
		buffer.append(site);
		buffer.append("_");

		/*
		 * If the cache isn't empty, it generate an unique id based on the
		 * available aspects and the current context model.
		 */
		if (cache != null) {
			buffer.append(cache.getAdaptedID());
		} else {
			buffer.append(0);
		}
		buffer.append(".xhtml");
		return buffer.toString();
	}

}
