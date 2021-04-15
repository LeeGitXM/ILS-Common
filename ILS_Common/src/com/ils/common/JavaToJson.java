/**
 *   (c) 2013-2021  ILS Automation. All rights reserved.
 */
package com.ils.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;

/**
 *  JavaToJson is a class with static methods for converting
 *  HashTables and ArrayLists to JSON Strings. We use Jackson
 *  as it handles generic Maps and Lists.
 *  @see http://wiki.fasterxml.com/JacksonHome.
 */
public class JavaToJson {
	private static final String CLSS = "JavaToJson";
	private static ILSLogger log = LogMaker.getLogger(JavaToJson.class.getPackage().getName());
	private static ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Create a JSON string equivalent of a single object. The string
	 * is a compressed (not pretty-print) version.
	 * 
	 * @param obj an object serializable to a Json string. It may be
	 *            a POJO object, Map or List
	 * @return the Json string equivalent of the object that was supplied.
	 */
	public synchronized String objectToJson(Object bean) {
		String result = null;
		try {
			result = mapper.writeValueAsString(bean);
		}
		catch( JsonProcessingException jpe) {
			log.warn(CLSS+".objectToJson: Parse exception ("+jpe.getLocalizedMessage()+")",jpe);
		}
		return result; 
	}	
}
