package com.ils.common.log.test;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;

/**
 * Example of a logging class that does knows its project at logger creation time.
 * @author chuckc
 *
 */
public class Sample1 {
	private final ILSLogger log;
	private static final String CLSS = "Sample1";
	
	public Sample1(String project) {
		this.log = LogMaker.getLogger(this,project);
	}
	
	public void info(String msg) {
		log.info(String.format("%s.info: Test info message (%s)",CLSS,msg));
	}
	
	public void infof(String format,String arg) {
		log.infof(format,arg);
	}
	
	public void warn(String msg) {
		log.info(String.format("%s.warn: Test warning message (%s)",CLSS,msg));
	}
	
	public void trace(String msg) {
		log.trace(String.format("%s.trace: Test trace message (%s)",CLSS,msg));
	}
	
	public void work() {
		info("tuba");
		infof("sousa%s","phone");
		warn("sax");
		trace("triangle");
	}
}
