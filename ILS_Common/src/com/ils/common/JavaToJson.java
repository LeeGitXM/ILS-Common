/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common;

import java.util.Hashtable;

import com.google.gson.Gson;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 *  JavaToJson is a class with static methods for converting
 *  HashTables and ArrayLists to JSON Strings.
 */
public class JavaToJson {
	private static final String TAG = "JavaToJson";
	private static LoggerEx log = LogUtil.getLogger(JavaToJson.class.getPackage().getName());
	
	/**
	 * Create a GSON equivalent of a single object.
	 * 
	 * @param obj an object serializable to a Json string
	 * @return the Json equivalent of the table that was supplied.
	 */
	public synchronized String objectToJson(Object obj) {
		Gson gson = new Gson();
		String json="";
		try {
			json = gson.toJson(obj);
		}
		catch(Exception ge) {
			log.warnf("%s: objectToJson (%s)",TAG,ge.getMessage());
		}
		return json;
	}	
	
	/**
	 * This trivial. Given that the Java consists of Hashtables, arraylists and
	 * primitives, simply use the Gson converter.
	 * 
	 * @param table a Hashtable serializable to a Json string
	 * @return the Json equivalent of the table that was supplied.
	 */
	public synchronized String tableToJson(Hashtable<String,?> table) {
		Gson gson = new Gson();
		String json="";
		try {
			json = gson.toJson(table);
		}
		catch(Exception ge) {
			log.warnf("%s: tableToJson (%s)",TAG,ge.getMessage());
		}
		return json;
	}	
}
