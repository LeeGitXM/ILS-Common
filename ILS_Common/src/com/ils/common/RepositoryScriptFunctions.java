/**
 *   (c) 2015  ILS Automation. All rights reserved.
 */
package com.ils.common;


/**
 *  This interface allows designer or client hook classes to be 
 *  used to store generic global objects during a client session.
 *  The normal pattern of using RootComponents for this does not
 *  work for generic classes.
 */
public class RepositoryScriptFunctions   {
	private static RepositoryScriptingInterface hook = null;
	
	/**
	 * This must be executed before any other methods.
	 */
	public static void setHook(RepositoryScriptingInterface h) { hook = h; }
	/**
	 * Retrieve a value from the repository.
	 * @return the value associated with the supplied key.
	 */
	public static Object retrieve(String key) {
		return hook.retrieveFromRepository(key);
	}

	/**
	 * Add or replace an entry in the save area (repository)
	 */
	public static void store(String key,Object value) {
		hook.storeIntoRepository(key, value);
	}
	/**
	 * Remove an entry from the repository
	 */
	public static void remove(String key) {
		hook.removeFromRepository(key);
	}
}
	
