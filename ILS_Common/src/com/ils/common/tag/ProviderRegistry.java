/**
 *   (c) 2013-2015  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.util.HashMap;

/**
 *  The Singleton instance is a container for currently defined ILSTagProvider
 *  instances. The hashtable holding instances of the provider is keyed by
 *  the provider name.
 *  
 *  ILSTagProviders implement the SimpleTagProviderInterface. Currently tags created with 
 *  these providers do not persist. However, they do acquire the timestamp of the QualifiedValue 
 *  data. Consequently, we will look in this registry first when looking for a provider.
 */
public class ProviderRegistry   {
	private static ProviderRegistry instance = null;
	private final HashMap<String,ILSTagProvider> providerMap;
	
	/**
	 * Static method to create and/or fetch the single instance.
	 */
	public static ProviderRegistry getInstance() {
		if( instance==null) {
			synchronized(ProviderRegistry.class) {
				instance = new ProviderRegistry();
			}
		}
		return instance;
	}
	/**
	 * Constructor is private per the Singleton pattern.
	 */
	private ProviderRegistry() {
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
