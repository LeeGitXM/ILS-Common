/**
 *   (c) 2013-2015  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *  The registry is meant to exist for the lifetime of a designer or client session. It
 *  holds modifiers for tag providers for other-than normal operation. It also holds a 
 *  list of ILSTagProviders which are custom "simple" providers and do not survive a
 *  Gateway restart. Custom providers have a different interface for writing to tags
 *  and are, for the most part, obsolete.
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
	public final static String PROVIDER_TYPE_CURRENT = "current";
	public final static String PROVIDER_TYPE_HISTORY = "history";
	public final static String PROVIDER_TYPE_TEST    = "test";  
	private final HashMap<String,DatabaseStoringProviderDelegate> historyProviderMap;
	private final HashMap<String,ILSTagProvider> simpleProviderMap;
	private final Set<String> testProviders;

	/**
	 * Constructor is private per the Singleton pattern.
	 */
	public ProviderRegistry() {
		historyProviderMap = new HashMap<String,DatabaseStoringProviderDelegate>();
		simpleProviderMap = new HashMap<String,ILSTagProvider>();
		testProviders = new HashSet<String>();
	}
	/**
	 * Inform the registry of the existence of a particular simple tag
	 * provider. This action is necessary before the tag factory or tag
	 * writer can take advantage of this provider. We assume that the delegate has 
	 * been initialized (backing tables created). We perform the startup() here. 
	 * @param provider
	 */
	public void addDatabaseStorageDelegate(DatabaseStoringProviderDelegate delegate) {
		DatabaseStoringProviderDelegate existing = historyProviderMap.get(delegate.getName());
		if( existing ==null ) {
			historyProviderMap.put(delegate.getName(), delegate);
			delegate.startup();
		}
	}
	/**
	 * Inform the registry of the existence of a particular simple tag
	 * provider. This action is necessary before the tag factory or tag
	 * writer can take advantage of this provider. 
	 * @param provider
	 */
	public void addSimpleProvider(ILSTagProvider provider) {
		ILSTagProvider existing = simpleProviderMap.get(provider.getName());
		if( existing ==null ) {
			simpleProviderMap.put(provider.getName(), provider);
			provider.startup(provider.getContext());
		}
	}
	/**
	 * Inform the registry that the named provider will henceforth
	 * write tags with the time-stamp specified in the qualified value
	 * to be written. This action is necessary before the tag writer
	 * can take advantage of this provider. 
	 * @param provider
	 */
	public void defineTestProvider(String name) {
		testProviders.add(name);
	}
	/**
	 * Return the simple provider, if any, by the specified name.
	 * @param name
	 * @return may be null, if the provider has not previously been defined.
	 */
	public DatabaseStoringProviderDelegate getDatabaseStoringDelegate(String name) {
		return historyProviderMap.get(name);
	}
	/**
	 * Return the simple provider, if any, by the specified name.
	 * @param name
	 * @return may be null, if the provider has not previously been defined.
	 */
	public BasicILSTagProvider getSimpleProvider(String name) {
		ILSTagProvider provider = simpleProviderMap.get(name);
		if( provider !=null && provider instanceof BasicILSTagProvider) {
			return (BasicILSTagProvider)provider;
		}
		return null;
	}

	/**
	 * @param name
	 * @return true if the named provider is in "test" mode.
	 */
	public boolean isTestMode(String name) {
		return testProviders.contains(name);
	}

	/**
	 * Remove the named provider. If this is a simple provider, 
	 * this action removes all the tags that it provides.
	 * 
	 * @param name
	 */
	public void removeProvider(String name) {
		DatabaseStoringProviderDelegate delegate = historyProviderMap.get(name);
		if( delegate!=null ) {
			delegate.shutdown();
			historyProviderMap.remove(name);
		}
		ILSTagProvider provider = simpleProviderMap.get(name);
		if( provider!=null ) {
			provider.shutdown();
			simpleProviderMap.remove(name);
		}
		testProviders.remove(name);
 	}

	/**
	 * This method is meant to be called by the GatewayHook on module
	 * shutdown. It shuts down all the providers and clears the list.
	 */
	public void shutdown() {
		for( DatabaseStoringProviderDelegate delegate:historyProviderMap.values() ) {
			delegate.shutdown();
		}
		historyProviderMap.clear();
		
		for( ILSTagProvider provider:simpleProviderMap.values() ) {
			provider.shutdown();
		}
		simpleProviderMap.clear();
		testProviders.clear();
	}
}
