/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 *  JsonToJava is a class with  methods for converting a Json
 *  string into an object, Hashtable or ArrayList. We need to know
 *  if the root object is a table,list or serializable object.
 */
public class JsonToJava {
	private static final String TAG = "JsonToJava";
	private static LoggerEx log = LogUtil.getLogger(JsonToJava.class.getPackage().getName());

	/**
	 * Assuming the contents of the Dictionary represented by the Json string are Hashtables, 
	 * ArrayLists, or simple objects, recursively convert the Json to a Java hashtable. 
	 * If the input is null, return null.
	 * 
	 * @param json the incoming Json document
	 * @param clss the expected class of the object
	 * @return the equivalent object
	 */
	public synchronized Object jsonToObject(String json,Class<?> clss) {
		
		Object result = null; 
		if( json!=null && json.length()>0 )  {
			Gson gson = new Gson();
			try {
				result = gson.fromJson(json, clss);
			}
			catch(Exception ex) {
				log.error(TAG+"jsonToObject: deserialization exception ("+ex.toString()+")",ex);
			}
		}
		return result;
	}
	
	/**
	 * Assuming the contents of the Dictionary represented by the Json string are Hashtables, 
	 * ArrayLists, or simple objects, recursively convert the Json to a Java hashtable. 
	 * If the input is null, return null.
	 * 
	 * @param json the incoming Json document
	 * @return the equivalent hashtable
	 */
	public synchronized Hashtable<String,?> jsonToTable(String json) {
		Hashtable<String,?> result = null;
		if( json!=null && json.length()>0 )  {
			JsonParser parser = new JsonParser();
			try {
				JsonObject jobj = parser.parse(json).getAsJsonObject();
				result = parseJsonDictionary(jobj);
			}
			catch(Exception ex) {
				log.error(TAG+"jsonToTable: deserialization exception ("+ex.toString()+")",ex);
			}
		}
		return result;
	}
	/**
	 * Assuming the contents of the List represented by the Json string are Hashtables, 
	 * ArrayLists, or simple objects, recursively convert the Json to a Java list. 
	 * We require that all elements of the list be the same data type.
	 * 
	 * @param json the incoming Json document
	 * @return the equivalent arraylist
	 */
	public synchronized List<?> jsonToList(String json) {
		List<?> result = null;
		if( json!=null && json.length()>0 )  {
			JsonParser parser = new JsonParser();
			try {
				JsonArray array = parser.parse(json).getAsJsonArray();
				result = parseJsonArray(array);
			}
			catch(Exception ex) {
				log.error(TAG+": jsonToList: deserialization exception ("+ex.toString()+")",ex);
			}
		}
		return result;
	}

	// Recognize dictionary elements as embedded dictionaries, lists or simple objects.
	private Hashtable<String,?> parseJsonDictionary(JsonObject obj) {
		Hashtable<String,Object> table = new Hashtable<String,Object>();
		Set<Map.Entry<String, JsonElement>> set = obj.entrySet();
		Iterator<Map.Entry<String, JsonElement>> iterator = set.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, JsonElement> entry = iterator.next();
			String key = entry.getKey();
			JsonElement value = entry.getValue();
			if (!value.isJsonPrimitive()) {
				if( value instanceof JsonObject ) {
					Hashtable<String,?> subtable = parseJsonDictionary((JsonObject)value);
					log.tracef("%s: parseJsonDictionary: DICTIONARY %s = %s",TAG,key,subtable);
					table.put(key, subtable);
				}
				else if( value instanceof JsonArray ) {
					List<?> list = parseJsonArray((JsonArray)value);
					log.tracef("%s: parseJsonDictionary: ARRAY %s = %s",TAG,key,list.toString());
					table.put(key, list);
				}
				else {
					log.warnf("%s: parseJsonDictionary: not a JsonObject %s, class=%s",TAG,key,value.getClass().getName());
				}
			} 
			else {
				log.tracef("%s: parseJsonDictionary: %s=%s",TAG,key,value.getAsString());
				table.put(key, value.getAsString());
			}
		}
		return table;
	}

	// Recognize array elements as embedded dictionaries, lists or simple objects.
	private List<?> parseJsonArray(JsonArray array) {
		List<Object> list = new ArrayList<Object>();
		int count = array.size();
		int index = 0;
		while( index < count ) {
			JsonElement value = array.get(index);
			if (!value.isJsonPrimitive()) {
				if( value instanceof JsonObject ) {
					Hashtable<String,?> subtable = parseJsonDictionary((JsonObject)value);
					log.debug(TAG+"parseJsonArray: DICTIONARY "+subtable);
					list.add(subtable);
				}
				else if( value instanceof JsonArray ) {
					List<?> sublist = parseJsonArray((JsonArray)value);
					log.debug(TAG+"parseJsonArray: ARRAY "+sublist);
					list.add(sublist);
				}
				else {
					log.debug(TAG+"parseJsonArray: not a JsonObject class="+value.getClass().getName());
				}
			} 
			else {
				log.debug(TAG+"parseJsonArray: "+value.getAsString());
				list.add(value.getAsString());
			}
			index++;
		}
		return list;
	}
}
