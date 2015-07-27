/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 *  Perform a deep clone of a Hashtable or ArrayList that itself contains nested hashtables,
 *  arraylists or "simple" datatypes. 
 */
public class Cloner {
	private static final String TAG = "Cloner";
	private static LoggerEx log = LogUtil.getLogger(Cloner.class.getPackage().getName());

	/**
	 * Assuming the contents of the hashtable are either simple objects, other hashtables objects, 
	 * or arraylists, clone recursively. Hashtable keys are always taken to be a string. If
	 * the value is not a hashtable or arraylist, then it is taken to be a string.
	 */
	public synchronized Hashtable<String,?> clone(Hashtable<String,?> table) {
		Hashtable<String,Object> result = new Hashtable<String,Object>();
		log.tracef("%s: clone: Analyzing table ...",TAG);
		if( table!=null ) {
			@SuppressWarnings("rawtypes")
			Set keys = table.keySet();
			for(Object key:keys ) {
				if( key instanceof String ) {
					Object value = table.get(key);
					if( value==null) {
						// Simply don't propagate a null parameter
						log.debug(TAG+"clone: key "+key+"= null, ignored");
					}
					// "Simple datatypes"
					else if( value instanceof String ||
							value instanceof Integer||
							value instanceof Double ||
							value instanceof Boolean) {
						log.trace(TAG+"clone: key "+key+"="+value.getClass().getName());

						String val = value.toString();
						result.put(key.toString(),val);
					}
					// Embedded dictionary
					else if( value instanceof Map<?,?> ) {
						log.debug(TAG+"clone: key "+key+"= embedded map ...");
						@SuppressWarnings("unchecked")
						Map<String,?> dict = clone((Map<String,?>)value);
						result.put(key.toString(), dict);
					}
					else if( value instanceof Hashtable<?,?> ) {
						log.debug(TAG+"clone: key "+key+"= embedded table ...");
						@SuppressWarnings("unchecked")
						Hashtable<String,?> dict = clone((Hashtable<String,?>)value);
						result.put(key.toString(), dict);
					}
					// Embedded list
					else if( value instanceof ArrayList<?> ) {
						log.debug(TAG+"clone: key "+key+"= embedded list ...");
						ArrayList<?> list = clone((ArrayList<?>)value);
						result.put(key.toString(), list);
					}
					// Unknown, unhandled type
					else {
						log.infof("%s.clone hashtable: Unhandled type (%s)",TAG,value.getClass().getName());
					}
				}
				else {
					log.info(TAG+"clone: Error: "+key.getClass().getName()+" key not a string, ignored");
				}
			}
		}
		return result;
	}
	
	/**
	 * Assuming the contents of the hashtable are either simple objects, other hashtables objects, 
	 * or arraylists, clone recursively. Hashtable keys are always taken to be a string. If
	 * the value is not a hashtable or arraylist, then it is taken to be a string.
	 */
	public synchronized Map<String,?> clone(Map<String,?> table) {
		Map<String,Object> result = new HashMap<String,Object>();
		log.tracef("%s: clone: Analyzing map ...",TAG);
		if( table!=null ) {
			@SuppressWarnings("rawtypes")
			Set keys = table.keySet();
			for(Object key:keys ) {
				if( key instanceof String ) {
					Object value = table.get(key);
					if( value==null) {
						// Simply don't propagate a null parameter
						log.debug(TAG+"clone: key "+key+"= null, ignored");
					}
					// "Simple datatypes"
					else if( value instanceof String ||
							value instanceof Integer||
							value instanceof Double ||
							value instanceof Boolean) {
						log.trace(TAG+"clone: key "+key+"="+value.getClass().getName());

						String val = value.toString();
						result.put(key.toString(),val);
					}
					// Embedded dictionary
					else if( value instanceof Map<?,?> ) {
						log.debug(TAG+"clone: key "+key+"= embedded map ...");
						@SuppressWarnings("unchecked")
						Map<String,?> dict = clone((Map<String,?>)value);
						result.put(key.toString(), dict);
					}
					else if( value instanceof Hashtable<?,?> ) {
						log.debug(TAG+"clone: key "+key+"= embedded table ...");
						@SuppressWarnings("unchecked")
						Hashtable<String,?> dict = clone((Hashtable<String,?>)value);
						result.put(key.toString(), dict);
					}
					// Embedded list
					else if( value instanceof ArrayList<?> ) {
						log.debug(TAG+"clone: key "+key+"= embedded list ...");
						ArrayList<?> list = clone((ArrayList<?>)value);
						result.put(key.toString(), list);
					}
					// Unknown, unhandled type
					else {
						log.infof("%s.clone map: Unhandled type (%s)",TAG,value.getClass().getName());
					}	
				}
				else {
					log.info(TAG+"clone: Error: "+key.getClass().getName()+" key not a string, ignored");
				}
			}
		}
		return result;
	}
	
	/**
	 * Assuming the contents of the list are either simple objects, other list objects, 
	 * or hashtables, clone recursively. If the value is not a hashtable or arraylist, 
	 * then it is taken to be a string.
	 */
	public synchronized ArrayList<?> clone(ArrayList<?> list) {
		ArrayList<Object> result = new ArrayList<Object>();
		log.debug(TAG+"clone: Analyzing list ...");
		if( list!=null ) {
			for(Object obj:list ) {

				// "Simple datatypes"
				if( obj instanceof String ||
						obj instanceof Integer||
						obj instanceof Double ||
						obj instanceof Boolean) {
					log.debug(TAG+"clone: simple ="+obj.getClass().getName());

					String val = obj.toString();
					result.add(val);
				}
				// Embedded dictionary
				else if( obj instanceof Map<?,?> ) {
					log.debug(TAG+"clone:  embedded table ...");
					@SuppressWarnings("unchecked")
					Map<String,?> map = clone((Map<String,?>)obj);
					result.add(map);
				}
				else if( obj instanceof Hashtable<?,?> ) {
					log.debug(TAG+"clone:  embedded table ...");
					@SuppressWarnings("unchecked")
					Hashtable<String,?> table = clone((Hashtable<String,?>)obj);
					result.add(table);
				}
				// Embedded list
				else if( obj instanceof ArrayList<?> ) {
					log.debug(TAG+"clone:  embedded list ...");
					ArrayList<?> array = clone((ArrayList<?>)obj);
					result.add(array);
				}
				// Unknown, unhandled type
				else {
					log.infof("%s.clone array: Unhandled type (%s)",TAG,obj.getClass().getName());
				}	
			}
		}
		return result;
	}
}
