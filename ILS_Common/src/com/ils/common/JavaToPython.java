/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 *  JavaToPython is a class with methods for converting
 *  HashTables and ArrayLists to PyDictionaries and PyLists.
 */
public class JavaToPython {
	private static final String TAG = "JavaToPython";
	private static LoggerEx log = LogUtil.getLogger(JavaToPython.class.getPackage().getName());
	
	@SuppressWarnings("unchecked")
	public PyObject objectToPy(Object obj) {
		PyObject result = null;
		
		// "Simple datatypes" 
		if( obj instanceof String ) {
			result = new PyString(obj.toString());
		}
		else if( obj instanceof Integer ) {
			result = new PyInteger(((Integer)obj).intValue());
		}
		else if( obj instanceof Double ) {
			result = new PyFloat(((Double)obj).doubleValue());
		}
		else if( obj instanceof Float ) {
			result = new PyFloat(((Float)obj).floatValue());
		}
		else if( obj instanceof Boolean ) {
			result = new PyBoolean(((Boolean)obj).booleanValue());
		}
		// Lists and tables
		else if( obj instanceof java.util.Hashtable ) {
			result = tableToPyDictionary((Hashtable<String,?>)obj);
		}
		else if( obj instanceof java.util.HashMap ) {
			result = tableToPyDictionary((HashMap<String,?>)obj);
		}
		else if (obj instanceof List<?> ) {
			log.tracef("%s: listToPyList: embedded list ...",TAG);
			result = listToPyList((List<?>)obj);
		}
		else {
			log.infof("%s.objectToPy: Error: %s (unknown datatype, ignored)",TAG,obj.getClass().getName());
		}
		return result;
	}
	/**
	 * Assuming the contents of the Java list are simple types or PyDictionary objects, recursively
	 * convert to a PyLists. If Hashtables, they are guaranteed to have string keys.
	 */
	public synchronized PyList listToPyList(List<?> list) {
		PyList result = new PyList();
		log.tracef("%s: listToPyList: Analyzing list ...",TAG);
		if( list!=null ) {
			for(Object obj : list) {
				if( obj instanceof Hashtable<?,?> ) {
					@SuppressWarnings("unchecked")
					PyDictionary dict = tableToPyDictionary((Hashtable<String,?>)obj);
					result.add(dict);
				}
				else if (obj instanceof List<?> ) {
					log.tracef("%s: listToPyList: embedded list ...",TAG);
					PyList embeddedlist = listToPyList((List<?>)obj);
					result.add(embeddedlist);
				}
				// "Simple datatypes" - let Jython take care of the conversions
				else if( obj instanceof String ||
                         obj instanceof Double ||
                         obj instanceof Float  ||
                         obj instanceof Integer||
                         obj instanceof Boolean     ) {
					result.add(obj);
				}
				else {
					log.infof("%s: listToPyList: Error: %s (unknown datatype, ignored)",TAG,obj.getClass().getName());
				}
			}
		}
		return result;
	}

	/**
	 * Assuming the contents of the table are either simple objects, lists, tables or maps, 
	 * recursively convert to a PyDictionary from a HashTable. The key is always taken to be a string. If
	 * the value is not a PyDictionary, then it is taken to be a string.
	 */
	public synchronized PyDictionary tableToPyDictionary(Hashtable<String,?> table) {
		PyDictionary result = new PyDictionary();
		log.tracef("%s: tableToPyDictionary: Analyzing table ...",TAG);
		if( table!=null ) {
			@SuppressWarnings("rawtypes")
			Set keys = table.keySet();
			for(Object key:keys ) {
				if( key instanceof String ) {
					Object value = table.get(key);
					if( value==null) {
						// Simply don't propagate a null parameter
						log.debugf("%s: tableToPyDictionary: key %s = null (ignored)",TAG,key);
					}
					// "Simple datatypes"
					else if( value instanceof String ||
							value instanceof Integer||
							value instanceof Double ||
							value instanceof Long ||
							value instanceof Float||
							value instanceof Boolean) {
						log.tracef("%s: tableToPyDictionary: key %s = %s",TAG,key,value.getClass().getName());
						result.put(key.toString(),value);
					}
					// Embedded dictionary
					else if( value instanceof Hashtable<?,?> ) {
						log.tracef("%s: tableToPyDictionary: key %s = embedded dictionary ...",TAG,key);
						@SuppressWarnings("unchecked")
						PyDictionary dict = tableToPyDictionary((Hashtable<String,?>)value);
						result.put(key.toString(), dict);
					}
					// Embedded list - can be list of Strings or list of Dictionaries 
					else if( value instanceof List<?> ) {
						log.tracef("%s: tableToPyDictionary: key %s = embedded list ...",TAG,key);
						PyList list = listToPyList((List<?>)value);
						result.put(key.toString(), list);
					}
					// Unknown, unhandled type
					else {
						log.infof("%s: tableToPyDictionary: key %s = %s (unhandled type)",TAG,key,value.getClass().getName());
					}	
				}
				else {
					log.infof("%s: tableToPyDictionary: Error: %s (key not a string, ignored)",TAG,key.getClass().getName());
				}
			}
		}
		return result;
	}
	/**
	 * Assuming the contents of the Map are either simple objects, lists, tables or maps, 
	 * recursively convert to a PyDictionary from a HashMap. The key is always taken to be a string. If
	 * the value is not a PyDictionary, then it is taken to be a string.
	 */
	public synchronized PyDictionary tableToPyDictionary(HashMap<String,?> table) {
		PyDictionary result = new PyDictionary();
		log.tracef("%s: tableToPyDictionary: Analyzing map ...",TAG);
		if( table!=null ) {
			@SuppressWarnings("rawtypes")
			Set keys = table.keySet();
			for(Object key:keys ) {
				if( key instanceof String ) {
					Object value = table.get(key);
					if( value==null) {
						// Simply don't propagate a null parameter
						log.debugf("%s: tableToPyDictionary: key %s = null (ignored)",TAG,key);
					}
					// "Simple datatypes"
					else if( value instanceof String ||
							value instanceof Integer||
							value instanceof Double ||
							value instanceof Long ||
							value instanceof Float||
							value instanceof Boolean) {
						log.tracef("%s: tableToPyDictionary: key %s = %s",TAG,key,value.getClass().getName());
						result.put(key.toString(),value);
					}
					// Embedded dictionary
					else if( value instanceof Hashtable<?,?> ) {
						log.tracef("%s: tableToPyDictionary: key %s = embedded dictionary ...",TAG,key);
						@SuppressWarnings("unchecked")
						PyDictionary dict = tableToPyDictionary((Hashtable<String,?>)value);
						result.put(key.toString(), dict);
					}
					// Embedded list - can be list of Strings or list of Dictionaries 
					else if( value instanceof List<?> ) {
						log.tracef("%s: tableToPyDictionary: key %s = embedded list ...",TAG,key);
						PyList list = listToPyList((List<?>)value);
						result.put(key.toString(), list);
					}
					// Unknown, unhandled type
					else {
						log.infof("%s: tableToPyDictionary: key %s = %s (unhandled type)",TAG,key,value.getClass().getName());
					}	
				}
				else {
					log.infof("%s: tableToPyDictionary: Error: %s (key not a string, ignored)",TAG,key.getClass().getName());
				}
			}
		}
		return result;
	}
}
