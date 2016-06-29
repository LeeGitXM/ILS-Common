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
public interface RepositoryScriptingInterface   {

	/**
	 * Retrieve a value from the repository.
	 * @return the value associated with the supplied key.
	 */
	public Object retrieveFromRepository(String key);
	/**
	 * Add or replace an entry in the save area (repository)
	 */
	public void storeIntoRepository(String key,Object value);
	/**
	 * Remove an entry from the repository
	 */
	public void removeFromRepository(String key);
}
	
