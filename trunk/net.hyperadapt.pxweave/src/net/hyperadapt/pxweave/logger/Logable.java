package net.hyperadapt.pxweave.logger;

import org.apache.log4j.Logger;

public abstract class Logable {
	
	private Logger logger = LoggerConfiguration.instance().getLogger(getClass());
	
	public Logger getLogger(){
		return logger;
	}
	
	public void setLogger(Logger newLogger){
		this.logger = newLogger;
	}
	
}
