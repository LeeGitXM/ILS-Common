/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.python.core.PyDictionary;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyTuple;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 *  PythonToJava is a class with methods for converting
 *  PyDictionaries and PyLists to "standard" java classes.
 */
public class PythonToJava {
	private static final String TAG = "PythonToJava";
	private static LoggerEx log = LogUtil.getLogger(PythonToJava.class.getPackage().getName());
	
	/**
	 * Assuming the contents of the PyList are PyDictionary, PyList, PyTuple or simple objects, recursively
	 * convert the PyList to a Java list. We require that all elements of the list be the same data type.
	 * 
	 * The result of this method is designed to be serializable through JSON.
	 * @param list the incoming PyList
	 * @return the resulting java list.
	 */
	public synchronized List<?> pyListToArrayList(PyList list) {
		List<Object> result = new ArrayList<Object>();
		log.tracef("%s: pyListToArrayList: Analyzing list ... %s",TAG,list.toString());
		if( list!=null ) {
			PyObject [] objs = list.getArray();
			for(Object obj : objs) {
				// The "simple" datatypes come through as standard Java, not Python
				if( obj instanceof String  ||
					obj instanceof Float   ||
					obj instanceof Double  ||
					obj instanceof Integer ||
					obj instanceof Boolean    ) {
					result.add(obj);
				}
				// Embedded table
				else if( obj instanceof PyDictionary ) {
					Hashtable<String,?> table = pyDictionaryToTable((PyDictionary)obj);
					result.add(table);
				}
				// Embedded List
				else if( (obj instanceof org.python.core.PyList)  ) {
					List<?> embeddedList = pyListToArrayList((PyList)obj);
					result.add(embeddedList);
				}
				// Embedded array
				else if( (obj instanceof org.python.core.PyTuple)  ) {
					List<?> embeddedList = pyTupleToArrayList((PyTuple)obj);
					result.add(embeddedList);
				}
				// "Simple datatypes" (In case we see the Python versions - shouldn't happen))
				else if( obj instanceof PyString ) {
					result.add(obj.toString());
				}
				else if( obj instanceof PyFloat ) {
					result.add(new Double(((PyFloat)obj).getValue()));
				}
				else if( obj instanceof PyInteger ) {
					result.add(new Integer(((PyInteger)obj).getValue()));
				}
				else {
					log.warnf("%s: pyListToArrayList: %s (unrecognized list element type)",TAG,obj.getClass().getName());
					result.add(obj.toString());
				}
			}
		}
		return result;
	}
	
	/**
	 * Assuming the contents of the PyTuple are PyDictionary, PyList, PyTuple or simple objects, recursively
	 * convert the PyList to a Java list. We require that all elements of the list be the same data type.
	 * 
	 * The result of this method is designed to be serializable through JSON.
	 * @param tuple the incoming PyTuple
	 * @return the resulting java list.
	 */
	public synchronized List<?> pyTupleToArrayList(PyTuple tuple) {
		List<Object> result = new ArrayList<Object>();
		log.tracef("%s: pyTupleToArrayList: Analyzing tuple ... %s",TAG,tuple.toString());
		if( tuple!=null ) {
			PyObject [] objs = tuple.getArray();
			for(Object obj : objs) {
				// The "simple" datatypes come through as standard Java, not Python
				if( obj instanceof String  ||
					obj instanceof Float   ||
					obj instanceof Double  ||
					obj instanceof Integer ||
					obj instanceof Boolean    ) {
					result.add(obj);
				}
				// Embedded table
				else if( obj instanceof PyDictionary ) {
					Hashtable<String,?> table = pyDictionaryToTable((PyDictionary)obj);
					result.add(table);
				}
				// Embedded List
				else if( (obj instanceof org.python.core.PyList)  ) {
					List<?> embeddedList = pyListToArrayList((PyList)obj);
					result.add(embeddedList);
				}
				// Embedded array
				else if( (obj instanceof org.python.core.PyTuple)  ) {
					List<?> embeddedList = pyTupleToArrayList((PyTuple)obj);
					result.add(embeddedList);
				}
				// "Simple datatypes" (In case we see the Python versions - shouldn't happen))
				else if( obj instanceof PyString ) {
					result.add(obj.toString());
				}
				else if( obj instanceof PyFloat ) {
					result.add(new Double(((PyFloat)obj).getValue()));
				}
				else if( obj instanceof PyInteger ) {
					result.add(new Integer(((PyInteger)obj).getValue()));
				}
				else {
					log.warnf("%s: pyTupleToArrayList: %s (unrecognized list element type)",TAG,obj.getClass().getName());
					result.add(obj.toString());
				}
			}
		}
		return result;
	}
	/**
	 * Assuming the contents of the PyList are strings, convert the PyList to a list 
	 * of strings.
	 * 
	 * The result of this method is designed to be serializable through JSON.
	 * @param list the incoming PyList
	 * @return the resulting java list.
	 */
	public synchronized List<String> pyListToStringList(PyList list) {
		List<String> result = new ArrayList<String>();
		log.tracef("%s: pyListToStringList: Analyzing list ... %s",TAG,list.toString());
		if( list!=null ) {
			PyObject [] objs = list.getArray();
			for(PyObject obj : objs) {
				result.add(obj.toString());
			}
		}
		return result;
	}
	/**
	 * Assuming the contents of the PyList are strings, convert the PyList to a list 
	 * of strings. We convert them to lowercase, presumably to perform a case-insensitive
	 * compare.
	 * 
	 * The result of this method is designed to be serializable through JSON.
	 * @param list the incoming PyList
	 * @return the resulting java list.
	 */
	public synchronized List<String> pyListToLowerCaseStringList(PyList list) {
		List<String> result = new ArrayList<String>();
		log.tracef("%s: pyListToLowerCaseStringList: Analyzing list ... %s",TAG,list.toString());
		if( list!=null ) {
			PyObject [] objs = list.getArray();
			for(PyObject obj : objs) {
				result.add(obj.toString().toLowerCase());
			}
		}
		return result;
	}
	
	/**
	 * Assuming the contents of the PyDictionary are either simple objects, other PyDictionary objects, 
	 * or PyLists, recursively convert the PyDictionary to a Hashtable. The key is always taken to be a string. If
	 * the value is not a simple datatype, a PyDictionary or PyList, then it is taken to be a string.
	 */
	public synchronized Hashtable<String,?> pyDictionaryToTable(PyDictionary dict) {
		Hashtable<String,Object> result = new Hashtable<String,Object>();
		log.tracef("%s: pyDictionaryToTable: Analyzing table ...",TAG);
		if(dict!=null ) {
			@SuppressWarnings("rawtypes")
			Set keys = dict.keySet();
			for(Object key:keys ) {
				if( key instanceof String ) {
					Object value = dict.get(key);
					if( value==null) {
						// Simply don't propogate a null parameter
						log.debug(TAG+"pyDictionaryToTable: "+key+"= null, ignored");
					}
					// The "simple" datatypes come through as standard Java, not Python
					// thanks to Jython
					else if( value instanceof String  ||
							 value instanceof Float   ||
							 value instanceof Double  ||
							 value instanceof Integer ||
							 value instanceof Boolean    ) {
						result.put(key.toString(),value);
					}
					// Embedded dictionary
					else if( value instanceof PyDictionary ) {
						log.tracef(TAG+"%s: pyDictionaryToTable: key %s = embedded dictionary ...",TAG,key);
						Hashtable<String,?> table = pyDictionaryToTable((PyDictionary)value);
						result.put(key.toString(), table);
					}
					// Embedded List -- why does the instanceof fail?
					else if( (value instanceof org.python.core.PyList) || value.getClass().getName().equalsIgnoreCase("org.python.core.PyList") ) {
						log.tracef(TAG+"%s: pyDictionaryToTable: key %s = embedded list ...",TAG,key);
						List<?> list = pyListToArrayList((PyList)value);
						result.put(key.toString(), list);
					}
					// Embedded array
					else if( (value instanceof org.python.core.PyTuple)  ) {
						List<?> list = pyTupleToArrayList((PyTuple)value);
						result.put(key.toString(), list);
					}
					// Unknown, unhandled type
					else {
						log.warnf("%s: pyDictionaryToTable: %s = %s (unhandled hashtable type)",TAG,key,value.getClass().getName());
						log.tracef("%s: pyDictionaryToTable: value isPyList: %s",TAG,(value instanceof org.python.core.PyList?"true":"false"));
					}	
				}
				else {
					log.warnf("%s: pyDictionaryToTable: Error: key not a string (%s), ignored",TAG,key.getClass().getName());
				}
			}
		}
		return result;
	}
}
