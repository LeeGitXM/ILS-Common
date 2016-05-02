/**
 *   (c) 2016  ILS Automation. All rights reserved. 
 */
package com.ils.common.db;

/**
 * This enumeration contains names of specific DBMS products for 
 * those cases where we have to handle specific idiosyncrasies.
 */
public enum DBMS
{
			ANSI,        // Default
			MYSQL,
			ORACLE,
			POSTGRES,
			SQLITE,
			SQLSERVER
            ;
   
 /**
  * @return  a comma-separated list of all step types in a single String.
  */
  public static String names()
  {
    StringBuffer names = new StringBuffer();
    for (DBMS type : DBMS.values()) {
      names.append(type.name()+", ");
    }
    return names.substring(0, names.length()-2);
  }
}
