/**
 *   (c) 2015  ILS Automation. All rights reserved. 
 */
package com.ils.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Use this class to hold data structures that are more complex
 * than simple lists and maps. We design this structure to be
 * easily convertible between Java and Python. We allow:
 * The permissible data structures are contained in 3 maps:
 *    properties - Map<String,String>
 *    lists      - Map<String,List<String>>
 *    maplists   - Map<String,List<Map<String,String>>>
 * 
 * The top-level python representation is a list of the three maps
 * in the order shown above.
 * This structure is designed to be easily serializable.
 */
public class GeneralPurposeDataContainer implements Serializable {
	private static final long serialVersionUID = 5499297358912286066L;
	private Map<String,String> properties;
	private Map<String,List<String>> lists;
	private Map<String,List<Map<String,String>>> maplists;
	
	public GeneralPurposeDataContainer() {	;
		properties = new HashMap<>();
		lists = new HashMap<>();
		maplists  = new HashMap<>();
	}

	public Map<String,String> getProperties() {return properties;}
	public void setProperties(Map<String,String> map) {this.properties = map;}
	public Map<String,List<String>> getLists() {return lists;}
	public void setLists(Map<String,List<String>> map) {this.lists = map;}
	public Map<String,List<Map<String,String>>> getMapLists() {return maplists;}
	public void setMapLists(Map<String,List<Map<String,String>>> list) {this.maplists = list;}
}
