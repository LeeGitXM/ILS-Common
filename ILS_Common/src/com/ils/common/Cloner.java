/**
 *   (c) 2013-2021  ILS Automation. All rights reserved.
 */
package com.ils.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;

/**
 *  Perform a deep clone of a Hashtable or ArrayList that itself contains nested hashtables,
 *  arraylists or "simple" datatypes. 
 */
public class Cloner {
	private static final String CLSS = "Cloner";
	private static ILSLogger log = LogMaker.getLogger(Cloner.class.getPackage().getName());

	/**
	 * Assuming the contents of the hashtable are either simple objects, other hashtables objects, 
	 * or arraylists, clone recursively. Hashtable keys are always taken to be a string. If
	 * the value is not a hashtable or arraylist, then it is taken to be a string.
	 */
	public synchronized Hashtable<String,?> clone(Hashtable<String,?> table) {
		Hashtable<String,Object> result = new Hashtable<String,Object>();
		log.tracef("%s: clone: Analyzing table ...",CLSS);
		if( table!=null ) {
			@SuppressWarnings("rawtypes")
			Set keys = table.keySet();
			for(Object key:keys ) {
				if( key instanceof String ) {
					Object value = table.get(key);
					if( value==null) {
						// Simply don't propagate a null parameter
						log.debug(CLSS+"clone: key "+key+"= null, ignored");
					}
					// "Simple datatypes"
					else if( value instanceof String ||
							value instanceof Integer||
							value instanceof Double ||
							value instanceof Boolean) {
						log.trace(CLSS+"clone: key "+key+"="+value.getClass().getName());

						String val = value.toString();
						result.put(key.toString(),val);
					}
					// Embedded dictionary
					else if( value instanceof Map<?,?> ) {
						log.debug(CLSS+"clone: key "+key+"= embedded map ...");
						@SuppressWarnings("unchecked")
						Map<String,?> dict = clone((Map<String,?>)value);
						result.put(key.toString(), dict);
					}
					else if( value instanceof Hashtable<?,?> ) {
						log.debug(CLSS+"clone: key "+key+"= embedded table ...");
						@SuppressWarnings("unchecked")
						Hashtable<String,?> dict = clone((Hashtable<String,?>)value);
						result.put(key.toString(), dict);
					}
					// Embedded list
					else if( value instanceof ArrayList<?> ) {
						log.debug(CLSS+"clone: key "+key+"= embedded list ...");
						ArrayList<?> list = clone((ArrayList<?>)value);
						result.put(key.toString(), list);
					}
					// Unknown, unhandled type
					else {
						log.infof("%s.clone hashtable: Unhandled type (%s)",CLSS,value.getClass().getName());
					}
				}
				else {
					log.info(CLSS+"clone: Error: "+key.getClass().getName()+" key not a string, ignored");
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
		log.tracef("%s: clone: Analyzing map ...",CLSS);
		if( table!=null ) {
			@SuppressWarnings("rawtypes")
			Set keys = table.keySet();
			for(Object key:keys ) {
				if( key instanceof String ) {
					Object value = table.get(key);
					if( value==null) {
						// Simply don't propagate a null parameter
						log.debug(CLSS+"clone: key "+key+"= null, ignored");
					}
					// "Simple datatypes"
					else if( value instanceof String ||
							value instanceof Integer||
							value instanceof Double ||
							value instanceof Boolean) {
						log.trace(CLSS+"clone: key "+key+"="+value.getClass().getName());

						String val = value.toString();
						result.put(key.toString(),val);
					}
					// Embedded dictionary
					else if( value instanceof Map<?,?> ) {
						log.debug(CLSS+"clone: key "+key+"= embedded map ...");
						@SuppressWarnings("unchecked")
						Map<String,?> dict = clone((Map<String,?>)value);
						result.put(key.toString(), dict);
					}
					else if( value instanceof Hashtable<?,?> ) {
						log.debug(CLSS+"clone: key "+key+"= embedded table ...");
						@SuppressWarnings("unchecked")
						Hashtable<String,?> dict = clone((Hashtable<String,?>)value);
						result.put(key.toString(), dict);
					}
					// Embedded list
					else if( value instanceof ArrayList<?> ) {
						log.debug(CLSS+"clone: key "+key+"= embedded list ...");
						ArrayList<?> list = clone((ArrayList<?>)value);
						result.put(key.toString(), list);
					}
					// Unknown, unhandled type
					else {
						log.infof("%s.clone map: Unhandled type (%s)",CLSS,value.getClass().getName());
					}	
				}
				else {
					log.info(CLSS+"clone: Error: "+key.getClass().getName()+" key not a string, ignored");
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
		log.debug(CLSS+"clone: Analyzing list ...");
		if( list!=null ) {
			for(Object obj:list ) {

				// "Simple datatypes"
				if( obj instanceof String ||
						obj instanceof Integer||
						obj instanceof Double ||
						obj instanceof Boolean) {
					log.debug(CLSS+"clone: simple ="+obj.getClass().getName());

					String val = obj.toString();
					result.add(val);
				}
				// Embedded dictionary
				else if( obj instanceof Map<?,?> ) {
					log.debug(CLSS+"clone:  embedded table ...");
					@SuppressWarnings("unchecked")
					Map<String,?> map = clone((Map<String,?>)obj);
					result.add(map);
				}
				else if( obj instanceof Hashtable<?,?> ) {
					log.debug(CLSS+"clone:  embedded table ...");
					@SuppressWarnings("unchecked")
					Hashtable<String,?> table = clone((Hashtable<String,?>)obj);
					result.add(table);
				}
				// Embedded list
				else if( obj instanceof ArrayList<?> ) {
					log.debug(CLSS+"clone:  embedded list ...");
					ArrayList<?> array = clone((ArrayList<?>)obj);
					result.add(array);
				}
				// Unknown, unhandled type
				else {
					log.infof("%s.clone array: Unhandled type (%s)",CLSS,obj.getClass().getName());
				}	
			}
		}
		return result;
	}
}
