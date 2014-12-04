/**
 *   (c) 2013  ILS Automation. All rights reserved. 
 */
package com.ils.common.collector;


/**
 * This enumeration class represents the permissible states of a a data collector
 */
public enum CollectorState {
	ACQUIRING,
	INVALID,
	RESET
	;
           
    /**
     * @return a comma-separated list of all Collector in a single String.
     */
     public static String names() {
        StringBuffer names = new StringBuffer();
         for (CollectorState type : CollectorState.values()) {
            names.append(type.name()+", ");
        }
        return names.substring(0, names.length()-2);
    }
}
