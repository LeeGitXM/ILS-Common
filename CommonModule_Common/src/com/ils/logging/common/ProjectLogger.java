/**
 *   (c) 2018  ILS Automation. All rights reserved.
 */
package com.ils.logging.common;

import ch.qos.logback.classic.Logger;


/**
 * The project-aware logger extends a logback logger and maintains the name of its project. The logger is encased
 * as a member because the logger class is final. The project name is continuously set as a MDC variable with each
 * log.
 */
public class ProjectLogger {
	private final String NO_PROJECT = "no_project";
	private final Logger logger;
	private String projectName;
	
	public ProjectLogger(Object source) {
		this.logger = LogMaker.getLogger(source);
		this.projectName = NO_PROJECT;
	}
	
	public void setProject(String name) { this.projectName=name; }
	
}
