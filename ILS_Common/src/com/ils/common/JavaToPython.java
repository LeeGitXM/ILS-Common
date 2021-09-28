/**
 *   (c) 2013-2021  ILS Automation. All rights reserved.
 */
 package com.ils.common;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
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
 *  
 *  Note that a PyDataSet is not a PyObject.
 */
public class JavaToPython {
	private static final String TAG = "JavaToPython";
	private static LoggerEx log = LogUtil.getLogger(JavaToPython.class.getPackage().getName());
	
	@SuppressWarnings("unchecked")
	public PyObject objectToPy(Object obj) {
		PyObject result = null;
		
		// "Simple datatypes" 
		if( obj == null ) {
			result = new PyString("NULL");
		}
		else if( obj instanceof String ) {
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
			log.tracef("%s: objectToPy: embedded list ...",TAG);
			result = listToPyList((List<?>)obj);
		}
		else if (obj instanceof com.ils.common.GeneralPurposeDataContainer ) {
			log.tracef("%s: objectToPy: GeneralPurposeDataContainer ...",TAG);
			result = dataContainerToPy((GeneralPurposeDataContainer)obj);
		}
		else {
			log.infof("%s.objectToPy: Error: %s (unknown datatype) ... returning NULL",TAG,obj.getClass().getName());
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
				if( obj instanceof HashMap<?,?> ) {
					@SuppressWarnings("unchecked")
					PyDictionary dict = tableToPyDictionary((HashMap<String,?>)obj);
					result.add(dict);
				}
				else if( obj instanceof Hashtable<?,?> ) {
					@SuppressWarnings("unchecked")
					PyDictionary dict = tableToPyDictionary((Hashtable<String,?>)obj);
					result.add(dict);
				}
				else if (obj instanceof List<?> ) {
					log.tracef("%s: listToPyList: embedded list ...",TAG);
					PyList embeddedlist = listToPyList((List<?>)obj);
					result.add(embeddedlist);
				}
				else if (obj instanceof com.ils.common.GeneralPurposeDataContainer ) {
					log.tracef("%s: listToPyList: GeneralPurposeDataContainer ...",TAG);
					result.add(dataContainerToPy((GeneralPurposeDataContainer)obj));
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
	 * The data container has, potentially three embedded structures. The container itself is
	 * a list of the following dictionaries:
	 * properties: string keys, string values
	 * lists     : string keys, values are lists of strings
	 * maplists  : string keys, values are lists of dictionaries
	 * 
	 * We guarantee the presence of all three. Some may be empty
	 */
	public synchronized PyList dataContainerToPy(GeneralPurposeDataContainer container) {
		PyList result = new PyList();
		log.tracef("%s: dataContainerToPy: Analyzing container ...",TAG);
		if( container!=null ) {
			if( container.getProperties() !=null ) {
				PyDictionary dict = tableToPyDictionary(container.getProperties());
				result.add(dict);
			}
			else {
				result.add(new PyDictionary());
			}
			PyDictionary lists = new PyDictionary();
			if( container.getLists()!=null ) {
				 for(String key:container.getLists().keySet()) {
					 List<String> list = container.getLists().get(key);
					 PyList pylist = listToPyList(list);
					 lists.put(key,pylist);
				 }
			}
			result.add(lists);
			
			PyDictionary maplists = new PyDictionary();
			if( container.getMapLists()!=null ) {
				 for(String key:container.getMapLists().keySet()) {
					 List<Map<String,String>> maplist = container.getMapLists().get(key);
					 PyList pymaplist = new PyList();
					 for(Map<String,String> map:maplist) {
						 PyDictionary pymap = tableToPyDictionary(map);
						 pymaplist.add(pymap);
					 }
					 maplists.put(key,pymaplist);
				 }
			}
			result.add(maplists);

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
					else if( value instanceof HashMap<?,?> ) {
						log.tracef("%s: tableToPyDictionary: key %s = embedded dictionary ...",TAG,key);
						@SuppressWarnings("unchecked")
						PyDictionary dict = tableToPyDictionary((HashMap<String,?>)value);
						result.put(key.toString(), dict);
					}
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
					else if (value instanceof com.ils.common.GeneralPurposeDataContainer ) {
						log.tracef("%s: tableToPyDictionary: GeneralPurposeDataContainer ...",TAG);
						result.put(key.toString(),dataContainerToPy((GeneralPurposeDataContainer)value));
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
	public synchronized PyDictionary tableToPyDictionary(Map<String,?> table) {
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
					else if( value instanceof HashMap<?,?> ) {
						log.tracef("%s: tableToPyDictionary: key %s = embedded dictionary ...",TAG,key);
						@SuppressWarnings("unchecked")
						PyDictionary dict = tableToPyDictionary((HashMap<String,?>)value);
						result.put(key.toString(), dict);
					}
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
					else if (value instanceof com.ils.common.GeneralPurposeDataContainer ) {
						log.tracef("%s: tableToPyDictionary: GeneralPurposeDataContainer ...",TAG);
						result.put(key.toString(),dataContainerToPy((GeneralPurposeDataContainer)value));
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
