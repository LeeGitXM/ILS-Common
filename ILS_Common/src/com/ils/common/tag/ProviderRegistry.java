/**
 *   (c) 2013-2015  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.util.HashMap;

/**
 *  The registry is meant to exist for the lifetime of a designer or client session. It
 *  holds modifiers for tag providers for other-than normal operation. It also holds a 
 *  list of ILSTagProviders which are custom "simple" providers and do not survive a
 *  Gateway restart. Custom providers have a different interface for writing to tags.
 *  
 *  The provider types are:
 *      current - this is the default behavior. Tag values are written at the current real-time.
 *                Setting a provider's mode to "current" REMOVES it from the registry.
 *      test -    The timestamp for tag writes is the time given the tag by the writer.
 *                It can be in the past.
 *      history - In addition to writing with specified timestamps, like in "test" mode,
 *                the tag writer adds values to a specified database instance and table.
 *                (The database and table are required to make this work).
 */
public class ProviderRegistry   {
	private final HashMap<String,ILSTagProvider> providerMap;
	

	/**
	 * Constructor is private per the Singleton pattern.
	 */
	public ProviderRegistry() {
		providerMap = new HashMap<String,ILSTagProvider>() ;
	}
	/**
	 * 
	 * @param name
	 * @return may be null, if the provider has not previously been defined.
	 */
	public void addProvider(ILSTagProvider provider) {
		ILSTagProvider existing = providerMap.get(provider.getName());
		if( existing ==null ) {
			providerMap.put(provider.getName(), provider);
			provider.startup(provider.getContext());
		}
	}
	/**
	 * 
	 * @param name
	 * @return may be null, if the provider has not previously been defined.
	 */
	public BasicILSTagProvider getProvider(String name) {
		ILSTagProvider provider = providerMap.get(name);
		if( provider !=null && provider instanceof BasicILSTagProvider) {
			return (BasicILSTagProvider)provider;
		}
		return null;
	}


	/**
	 * Remove the named provider. Removing a provider, removes all the tags
	 * that it provides.
	 * 
	 * @param name
	 */
	public void removeProvider(String name) {
		ILSTagProvider provider = providerMap.get(name);
		if( provider!=null ) {
			provider.shutdown();
			providerMap.remove(name);
		}
 	}
	
	/**
	 * This method is meant to be called by the GatewayHook on module
	 * shutdown. It shuts down all the providers and clears the list.
	 */
	public void shutdown() {
		for( ILSTagProvider provider:providerMap.values() ) {
			provider.shutdown();
		}
		providerMap.clear();
	}
}
