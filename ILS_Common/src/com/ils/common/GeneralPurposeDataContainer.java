/**
 *   (c) 2015  ILS Automation. All rights reserved. 
 */
package com.ils.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;


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
public class GeneralPurposeDataContainer implements Serializable, Cloneable {
	private static final long serialVersionUID = 5499297358912286066L;
	private static LoggerEx log = LogUtil.getLogger(GeneralPurposeDataContainer.class.getPackage().getName());
	private Map<String,String> properties;
	private Map<String,List<String>> lists;
	private Map<String,List<Map<String,String>>> maplists;
	
	public GeneralPurposeDataContainer() {	;
		properties = new HashMap<>();
		lists = new HashMap<>();
		maplists  = new HashMap<>();
	}
	/**
	 * Avoid isEmpty as a name since this leads to problems during serialization.
	 * 
	 * @return true if the object contains no useful data.
	 */
	public boolean containsData() {
		boolean result = false;
		if( properties.isEmpty() &&
			lists.isEmpty()      &&
			maplists.isEmpty()      ) result = true;
		return result;
	}
	public Map<String,String> getProperties() {return properties;}
	public void setProperties(Map<String,String> map) {this.properties = map;}
	public Map<String,List<String>> getLists() {return lists;}
	public void setLists(Map<String,List<String>> map) {this.lists = map;}
	public Map<String,List<Map<String,String>>> getMapLists() {return maplists;}
	public void setMapLists(Map<String,List<Map<String,String>>> list) {this.maplists = list;}
	
	// Describe the contents of the container in a log file
	public void dump() {
		for (String key:properties.keySet()) {
			log.infof("Properties: key = %s, value = %s", key, properties.get(key));
		}
		for (String key:lists.keySet()) {
			List<String> list = lists.get(key);
			for(String val:list) {
				log.infof("Lists: key = %s, value = %s", key, val);
			}
		}
		for (String key:maplists.keySet()) {
			List<Map<String,String>> maplist = maplists.get(key);
			for (Map<String,String> map:maplist) {
				for (String prop:map.keySet()) {
					log.infof("MapList(%s): name = %s, value = %s", key, prop, map.get(prop));
				}
			}
		}
	}
	
	@Override
	public GeneralPurposeDataContainer clone() {
		GeneralPurposeDataContainer dup = new GeneralPurposeDataContainer();
		dup.properties.putAll(this.properties);
		for(String key:lists.keySet()) {
			List<String> list = lists.get(key);
			List<String> duplist = new ArrayList<>();
			duplist.addAll(list);
			dup.lists.put(key, duplist);
		}
		for(String key:maplists.keySet()) {
			List<Map<String,String>> maplist = maplists.get(key);
			List<Map<String,String>> dupmaplist = new ArrayList<>();
			for(Map<String,String> map:maplist) {
				Map<String,String>dupmap = new HashMap<>();
				dupmap.putAll(map);
				dupmaplist.add(dupmap);
			}
			dup.maplists.put(key, dupmaplist);
		}
		return dup;
	}
}
