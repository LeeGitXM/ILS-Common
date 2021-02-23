package com.ils.common.log.test;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;


/**
 * Example of a logging class that does not know its project at logger creation time.
 * @author chuckc
 *
 */
public class Sample2 {
	private final ILSLogger log;
	private static final String CLSS = "Sample2";
	
	public Sample2() {
		this.log = LogMaker.getLogger(this);
		
	}
	
	public void setProject(String project) {
		this.log.setProject(project);
	}
	
	public void info(String msg) {
		log.info(String.format("%s.info: Test info message (%s)",CLSS,msg));
	}
	
	public void infof(String format,String arg1,String arg2) {
		
		log.infof(format,arg1,arg2);
	}
	
	public void warn(String msg,String arg) {
		log.info(msg,arg);
	}
	
	public void trace(String msg) {
		log.trace(String.format("%s.trace: Test trace message (%s)",CLSS,msg));
	}
	public void work() {
		info("bassoon");
		infof("wood%s %s","win","quartet");
		warn("oboe {}","trio");
		trace("piccolo");
	}
}
