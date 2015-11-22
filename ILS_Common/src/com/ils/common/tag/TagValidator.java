/**
 *   (c) 2013  ILS Automation. All rights reserved.
 *  
 *   The tag factory is designed to be called from the client
 *   via RPC. The client presents the same interface to scripting functions.
 */
package com.ils.common.tag;

import java.io.IOException;

import com.inductiveautomation.ignition.common.sqltags.TagDefinition;
import com.inductiveautomation.ignition.common.sqltags.model.Tag;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.sqltags.TagProvider;

/**
 *  Given a tag path, verify that the tag exists. The tags are assumed to be
 *  project-gateway tags.
 */
public class TagValidator  {
	private static final String TAG = "TagValidator";
	private final LoggerEx log;
	private final GatewayContext context;
	private final ProviderRegistry registry = ProviderRegistry.getInstance();
	
	/**
	 * Constructor.
	 */
	public TagValidator(GatewayContext ctx) {
		this.context = ctx;
		log = LogUtil.getLogger(getClass().getPackage().getName());
	}

	/**
	 * Test a tag path for validity/existence. We look both in the Gateway tag
	 * manager and our private repository of "simple" tags.
	 * 
	 * @param path the tag path
	 * @return true if the tag path can be parsed and exists
	 */
	public boolean exists(String path) {
		boolean exists = false;
		TagPath tp = null;
		try {
			tp = TagPathParser.parse(null,path);
			String providerName = tp.getSource();
			// If provider is null, then the tagPath parser will use the default provider
			TagProvider provider = context.getTagManager().getTagProvider(providerName);
			if(provider != null) {
			    Tag tag = provider.getTag(tp);
			    if( tag!=null) exists = true;
			    else  {
			    	log.tracef("%s.exists: tag %s not found",TAG,path);
			    }
			}
			else {
				ILSTagProvider prov = registry.getProvider(providerName);
				if( prov!=null) {
					TagDefinition td = prov.getTagDefinition(tp);
					if( td!=null) exists = true;
				    else  {
				    	log.tracef("%s.exists: tag %s not found",TAG,path);
				    }
				}
				else {
					log.infof("%s.exists: provider %s for tag %s not found",TAG,providerName,path);
				}
				
			}
		}
		catch(IOException ioe) {
			log.warnf("%s.exists: Exception parsing tag %s (%s)",TAG,path,ioe.getMessage());
		}
		return exists;
	}
}
