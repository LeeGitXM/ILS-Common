/**
 *   (c) 2015  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.sqltags.TagDefinition;
import com.inductiveautomation.ignition.common.sqltags.model.Tag;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.sqltags.simple.SimpleProviderInterface;
import com.inductiveautomation.ignition.gateway.sqltags.simple.WriteHandler;

/**
 *  Extend the SimpleProviderInterface to account for common 
 *  provider methods in our TestFrame module.
 *  
 *  NOTE: This interface has largely been made obsolete by our discovery
 *        of a way to write past time-stamps using a normal tag provider.
 */
public interface ILSTagProvider extends SimpleProviderInterface  {
	/**
	 * @return the Gateway context
	 */
	public GatewayContext getContext();
	/**
	 * @return the name of the provider
	 */
	public String getName();
	/**
	 * @return details of a tag given the path.
	 */
	public Tag getTag(TagPath tp);
	/**
	 * @return a tag's definition given its path.
	 */
	public TagDefinition getTagDefinition(TagPath tp);
	/**
	 * Associates a write handler with the specified path.
	 * @param path
	 * @param handler
	 */
	public void registerWriteHandler(TagPath path, WriteHandler handler);
	/**
	 * Register with the context's tag manager.
	 * @param context
	 */
	public void startup(GatewayContext context);

	/**
	 * Unregister with the context's tag manager.
	 */
	public void shutdown();
	/**
	 * Sets the value of the specified tag to the value provided, 
	 * including the quality and timestamp.
	 */
	public void updateValue(TagPath path, QualifiedValue value);
}
	
