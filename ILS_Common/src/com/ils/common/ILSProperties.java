/**
 *   (c) 2012-2021  ILS Automation. All rights reserved.
 *  
 *   Class contains static constants that have meaning to the ILS-Common library.
 *   NOTE: This class should be removed. ROOT is used only by ils-common and AED,
 *                                       TIMESTAMP_FORMAT is used only by SFC
 */
package com.ils.common;


/**
 *  Global properties for the ILS-Common library.
 */
public interface ILSProperties   {
	// These are the standard node names for the Gateway module configuration menus
	public static String ROOT                     = "ILS";                      // Root of Gateway menu tree
	public final static String TIMESTAMP_FORMAT   = "yyyy.MM.dd HH:mm:ss.SSS";     // Format for writing timestamp
}
