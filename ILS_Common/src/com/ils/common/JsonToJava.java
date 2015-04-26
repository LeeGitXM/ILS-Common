/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 *  JsonToJava is a class with  methods for converting a Json
 *  string into a bean object, HashMap or ArrayList. We need to know
 *  if the root object is a table,list or serializable object.
 *  
 *  We use Jackson as it handles generic Maps and Lists.
 *  @see http://wiki.fasterxml.com/JacksonHome.
 */
public class JsonToJava {
	private static final String TAG = "JsonToJava";
	private static LoggerEx log = LogUtil.getLogger(JsonToJava.class.getPackage().getName());
	private static ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * We must know the expected class of the object being deserialized.
	 * 
	 * @param json the incoming Json document
	 * @param clss the expected class of the object
	 * @return the equivalent object
	 */
	public synchronized Object jsonToObject(String json,Class<?> clss) {
		Object result = null;
		try {
			result = mapper.readValue(json, clss);
		} 
		catch (JsonParseException jpe) {
			log.warn(TAG+".jsonToObject: parsing exception ("+jpe.getLocalizedMessage()+")",jpe);
		} 
		catch (JsonMappingException jme) {
			log.warn(TAG+".jsonToObject: mapping exception("+jme.getLocalizedMessage()+")",jme);
		} 
		catch (IOException ioe) {
			log.warn(TAG+".jsonToObject: io exception("+ioe.getLocalizedMessage()+")",ioe);
		}
		return result;
	}
	
	/**
	 * Deserialize to a Map<String,String>. This is pretty restrictive.
	 * 
	 * @param json the incoming Json document
	 * @param clss the expected class of the object
	 * @return the equivalent object
	 */
	public synchronized Map<String,String> jsonToMap(String json) {
		Map<String,String> result = null;
		try {
			result = mapper.readValue(json,new TypeReference<Map<String,String>>() { });
		} 
		catch (JsonParseException jpe) {
			log.warn(TAG+".jsonToObject: parsing exception ("+jpe.getLocalizedMessage()+")",jpe);
		} 
		catch (JsonMappingException jme) {
			log.warn(TAG+".jsonToObject: mapping exception("+jme.getLocalizedMessage()+")",jme);
		} 
		catch (IOException ioe) {
			log.warn(TAG+".jsonToObject: io exception("+ioe.getLocalizedMessage()+")",ioe);
		}
		return result;
	}
}

