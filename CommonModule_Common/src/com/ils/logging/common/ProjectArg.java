/**
 *   (c) 2018-2020  ILS Automation. All rights reserved.
 */
package com.ils.logging.common;

/**
 * This is an MDC argument that can be added directly to a log message.
 * The MDC key = LogMaker.PROJECT_KEY.
 * 
 * In reality this is just needed as a marker for varargs methods
 * in ProjectLogger.
 */
public class ProjectArg {
	private String projectName;
	
	public ProjectArg(String pname) {
		this.projectName = pname;
	}
	
	public void setProject(String name) { this.projectName=name; }
	public String getProjectName() { return this.projectName; }
}
