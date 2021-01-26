package com.ils.common.log.test;

import org.apache.log4j.MDC;

import com.ils.common.log.LogMaker;

import ch.qos.logback.classic.Logger;

/**
 * Example of a logging class that does not know its project at logger creation time.
 * @author chuckc
 *
 */
public class Sample2 {
	private final Logger log;
	private static final String CLSS = "Sample2";
	
	public Sample2() {
		this.log = LogMaker.getLogger(this);
	}
	
	public void setProject(String project) {
		MDC.put(LogMaker.PROJECT_KEY, project);
	}
	
	public void info(String msg) {
		log.info(String.format("%s.info: Test info message (%s)",CLSS,msg));
	}
	
	public void infof(String format,String arg1,String arg2) {
		log.info(format,arg1,arg2);
		log.info("{} {} {}",1,2,3);
	}
	
	public void warn(String msg) {
		log.info(String.format("%s.warn: Test warning message (%s)",CLSS,msg));
	}
	
	public void trace(String msg) {
		log.trace(String.format("%s.trace: Test trace message (%s)",CLSS,msg));
	}
	public void work() {
		info("bassoon");
		infof("wood{} {}","win","quartet");
		warn("oboe");
		trace("piccolo");
	}
}
