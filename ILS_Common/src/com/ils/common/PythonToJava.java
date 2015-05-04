/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
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
	/**
	 * Assuming the contents of the PyDictionary are either simple objects, other PyDictionary objects, 
	 * or PyLists, recursively convert the PyDictionary to a Hashtable. The key is always taken to be a string. If
	 * the value is not a simple datatype, a PyDictionary or PyList, then it is taken to be a string.
	 */
	public synchronized HashMap<String,?> pyDictionaryToMap(PyDictionary dict) {
		HashMap<String,Object> result = new HashMap<String,Object>();
		log.tracef("%s: pyDictionaryToMap: Analyzing map ...",TAG);
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
						log.tracef(TAG+"%s: pyDictionaryToMap: key %s = embedded dictionary ...",TAG,key);
						HashMap<String,?> map = pyDictionaryToMap((PyDictionary)value);
						result.put(key.toString(), map);
					}
					// Embedded List -- why does the instanceof fail?
					else if( (value instanceof org.python.core.PyList) || value.getClass().getName().equalsIgnoreCase("org.python.core.PyList") ) {
						log.tracef(TAG+"%s: pyDictionaryToMap: key %s = embedded list ...",TAG,key);
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
						log.warnf("%s: pyDictionaryToMap: %s = %s (unhandled hashtable type)",TAG,key,value.getClass().getName());
						log.tracef("%s: pyDictionaryToMap: value isPyList: %s",TAG,(value instanceof org.python.core.PyList?"true":"false"));
					}	
				}
				else {
					log.warnf("%s: pyDictionaryToMap: Error: key not a string (%s), ignored",TAG,key.getClass().getName());
				}
			}
		}
		return result;
	}
	
	/**
	 * Update the contents of the map with the contents of the dictionary.
	 * The keys of both are guaranteed to be Strings. This is used when
	 * updating a map argument with a script result, so it is important
	 * to update the original object, not a copy.
	 * @param map
	 * @param dict
	 */
	public void updateMapFromDictionary(Map<String,Object> map, PyDictionary dict) {
		if(dict!=null ) {
			@SuppressWarnings("rawtypes")
			Set keys = dict.keySet();
			for(Object key:keys ) {
				if( key instanceof String ) {
					Object value = dict.get(key);
					if( value==null) {
						// Simply don't propogate a null parameter
						log.debug(TAG+"updateMapFromDictionary: "+key+"= null, ignored");
					}
					// The "simple" datatypes come through as standard Java, not Python
					// thanks to Jython
					else if( value instanceof String  ||
							 value instanceof Float   ||
							 value instanceof Double  ||
							 value instanceof Integer ||
							 value instanceof Boolean    ) {
						map.put(key.toString(),value);
					}
					// Embedded dictionary
					else if( value instanceof PyDictionary ) {
						log.tracef(TAG+"%s: updateMapFromDictionary: key %s = embedded dictionary ...",TAG,key);
						HashMap<String,?> maparg = pyDictionaryToMap((PyDictionary)value);
						map.put(key.toString(), maparg);
					}
					// Embedded List -- why does the instanceof fail?
					else if( (value instanceof org.python.core.PyList) || value.getClass().getName().equalsIgnoreCase("org.python.core.PyList") ) {
						log.tracef(TAG+"%s: updateMapFromDictionary: key %s = embedded list ...",TAG,key);
						List<?> list = pyListToArrayList((PyList)value);
						map.put(key.toString(), list);
					}
					// Embedded array
					else if( (value instanceof org.python.core.PyTuple)  ) {
						List<?> list = pyTupleToArrayList((PyTuple)value);
						map.put(key.toString(), list);
					}
					// Unknown, unhandled type
					else {
						log.warnf("%s.updateMapFromDictionary: %s = %s (unhandled hashtable type)",TAG,key,value.getClass().getName());
						log.tracef("%s.updateMapFromDictionary: value isPyList: %s",TAG,(value instanceof org.python.core.PyList?"true":"false"));
					}	
				}
				else {
					log.warnf("%s.updateMapFromDictionary: Error: key not a string (%s), ignored",TAG,key.getClass().getName());
				}
			}
		}
	}
	
	/**
	 * Update the contents of the supplied datacontainer with the contents of the list.
	 * We expect a fixed structure of dictionaries and lists in the Python designed to match
	 * the container structure. Keys and data everywhere are guaranteed to be Strings. This is
	 * used when updating the container with a script result, so it is important to update the
	 * original object, not a copy. This does not clear existing values in the container.
	 * 
	 * We expect the Python list to hold 3 dictionaries: properties, lists, maplists
	 * @param container
	 * @param pylist
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateDataContainerFromPython(GeneralPurposeDataContainer container, PyList pylist) {
		if(pylist!=null ) {
			int index = 0;
			for( Object pyobj: pylist ) {
				index++;
				if( index==1 ) {
					// Properties
					if( pyobj instanceof PyDictionary ) {
						PyDictionary pyprops = (PyDictionary)pyobj;
						Map<String,String> properties = container.getProperties();
						if( properties==null ) {
							properties = new HashMap<>();
							container.setProperties(properties);
						}
						Set keys = pyprops.keySet();
						for(Object key:keys ) {
							// Both key and value must be strings
							if( key instanceof String ) {
								Object value = pyprops.get(key);
								if( value==null) {
									// Simply don't propagate a null parameter
									log.infof("%s.updateDataContainerFromPython: %s= null, ignored",TAG,value);
								}
								else if( value instanceof String ) {
									properties.put(key.toString(), value.toString());
								}
								else if( value instanceof Double ) {
									properties.put(key.toString(), value.toString());
								}
								else if( value instanceof Integer ) {
									properties.put(key.toString(), value.toString());
								}
								else {
									log.infof("%s.updateDataContainerFromPython: %s, not a String, ignored",TAG,value.getClass().getName());
								}
							}
							else {
								log.infof("%s.updateDataContainerFromPython: key %s not a String, ignored",TAG,key.getClass().getName());
							}
						}
					}
					else {
						log.warnf("%s.updateDataContainerFromPython: Error: properties not a dictionary (%s), ignored",TAG,pyobj.getClass().getName());
					}
				}
				else if( index==2 ) {
					// Lists
					if( pyobj instanceof PyDictionary ) {
						PyDictionary pyprops = (PyDictionary)pyobj;
						Map<String,List<String>> lists = container.getLists();
						if( lists==null ) {
							lists = new HashMap<>();
							container.setLists(lists);
						}
						Set keys = pyprops.keySet();
						for(Object key:keys ) {
							// Both key and value must be strings
							if( key instanceof String ) {
								Object value = pyprops.get(key);
								if( value==null) {
									// Simply don't propogate a null parameter
									log.infof("%s.updateDataContainerFromPython: %s= null, ignored",TAG,value);
								}
								else if( value instanceof org.python.core.PyList ) {
									List<String> list = (List<String>)pyListToArrayList((PyList)value);
									lists.put(key.toString(), list);
								}
								else if( value instanceof org.python.core.PyTuple ) {
									List<String> list = (List<String>)pyTupleToArrayList((PyTuple)value);
									lists.put(key.toString(), list);
								}
								else {
									log.infof("%s.updateDataContainerFromPython: %s = %s, not a String, ignored",TAG,value.getClass().getName());
								}
							}
							else {
								log.infof("%s.updateDataContainerFromPython: key %s not a String, ignored",TAG,key.getClass().getName());
							}
						}
					}
					else {
						log.warnf("%s.updateDataContainerFromPython: Error: properties not a dictionary (%s), ignored",TAG,pyobj.getClass().getName());
					}
				}				
				else if( index==3 ) {
					// Maplists
					if( pyobj instanceof PyDictionary ) {
						PyDictionary pyprops = (PyDictionary)pyobj;
						Map<String,List<Map<String,String>>> maplists = container.getMapLists();
						if( maplists==null ) {
							maplists = new HashMap<>();
							container.setMapLists(maplists);
						}
						Set keys = pyprops.keySet();
						for(Object key:keys ) {
							// Both key and value must be strings
							if( key instanceof String ) {
								Object value = pyprops.get(key);
								if( value==null) {
									// Simply don't propogate a null parameter
									log.infof("%s.updateDataContainerFromPython: %s= null, ignored",TAG,value);
								}
								// The lists contain dictionaries
								else if( value instanceof org.python.core.PyList ) {
									List list = pyListToArrayList((PyList)value);
									List<Map<String,String>> maplist = new ArrayList<>();
									for(Object obj:list) {
										// NOTE: Hashtable implements the map interface
										if( obj instanceof Hashtable ) {
											Map<String,String>map = new HashMap<String,String>((Map<String,String>)obj);
											maplist.add(map);
										}
										else if( obj instanceof Map ) {
											Map<String,String>map = new HashMap<String,String>((Map<String,String>)obj);
											maplist.add(map);
										}
										else {
											log.infof("%s.updateDataContainerFromPython: maplist map is %s",TAG,obj.getClass().getName());
										}
									}
									maplists.put(key.toString(), maplist);
								}
								else if( value instanceof org.python.core.PyTuple ) {
									List list = pyTupleToArrayList((PyTuple)value);
									List<Map<String,String>> maplist = new ArrayList<>();
									for(Object obj:list) {
										/// NOTE: Hashtable implements the map interface
										if( obj instanceof Hashtable ) {
											Map<String,String>map = new HashMap<String,String>((Map<String,String>)obj);
											maplist.add(map);
										}
										else if( obj instanceof Map ) {
											Map<String,String>map = new HashMap<String,String>((Map<String,String>)obj);
											maplist.add(map);
										}
										else {
											log.infof("%s.updateDataContainerFromPython: maplist map is %s",TAG,obj.getClass().getName());
										}
									}
									maplists.put(key.toString(), list);
								}
								else {
									log.infof("%s.updateDataContainerFromPython: %s = %s, not a String, ignored",TAG,value.getClass().getName());
								}
							}
							else {
								log.infof("%s.updateDataContainerFromPython: key %s not a String, ignored",TAG,key.getClass().getName());
							}
						}
					}
					else {
						log.warnf("%s.updateDataContainerFromPython: Error: properties not a dictionary (%s), ignored",TAG,pyobj.getClass().getName());
					}
				}
			}
		}
	}
}
