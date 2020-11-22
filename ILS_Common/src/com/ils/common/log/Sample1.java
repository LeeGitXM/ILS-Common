package com.ils.common.log;

import ch.qos.logback.classic.Logger;

/**
 * Example of a logging class that does knows its project at logger creation time.
 * @author chuckc
 *
 */
public class Sample1 {
	private final Logger log;
	private static final String CLSS = "Sample1";
	
	public Sample1(String project) {
		this.log = LogMaker.getLogger(this,project);
	}
	
	public void info(String msg) {
		log.info(String.format("%s.info: Test info message (%s)",CLSS,msg));
	}
	
	public void warn(String msg) {
		log.info(String.format("%s.warn: Test warning message (%s)",CLSS,msg));
	}
	
	public void trace(String msg) {
		log.trace(String.format("%s.trace: Test trace message (%s)",CLSS,msg));
	}
	
	public void work() {
		info("tuba");
		warn("sax");
		trace("triangle");
	}
}
