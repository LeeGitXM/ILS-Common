/**
 *   (c) 2015  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.io.IOException;

import com.inductiveautomation.ignition.common.sqltags.TagDefinition;
import com.inductiveautomation.ignition.common.sqltags.model.Tag;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagType;
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
	
	/**
	 * Determine if the tag path is valid. If there is an issue a reason will be returned.
	 * A null denotes success.
	 * 
	 * @param path fully qualified tag path
	 * @return reason that tag is invalid, else null
	 */
	public String validateTag(String path) {
		log.debugf("%s.validateTag: %s",TAG,path);
		if( context==null) return null;
		// Not initialized yet.
		String reason = null;
		if(path==null || path.isEmpty() ) return null;  // Path or value not set
		try {
			TagPath tp = TagPathParser.parse(path);

			String providerName = TagUtility.providerNameFromPath(path);
			// NOTE: For a simple tag, this returns the internal provider, a real provider.
			TagProvider provider = context.getTagManager().getTagProvider(providerName);
			// We assume the same provider
			if( provider!= null  ) {
				Tag tag = provider.getTag(tp);
				if( tag!=null ) {
					if( !tag.isEnabled() ) {
						reason = "is disabled";
					}
					else if(tag.getType().equals(TagType.Folder) || tag.getType().equals(TagType.UDT_DEF) ||
							tag.getType().equals(TagType.UDT_INST)   ) {
						reason = String.format("tag type (%s) is not a simple type",tag.getType().toString());
					}
					else {
						DataType dt = tag.getDataType();
						DataType[] classicTypes = DataType.CLASSIC_TYPES_NO_DATASET;
						for( DataType classicType:classicTypes) {
							if(dt.equals(classicType))  return null;    // Good, a match
						}
						reason = String.format("datatype (%s) is not recognized",dt.toString());
					}
				}
				else {
					log.warnf("%s.validateTag: Provider %s did not find tag %s",TAG,providerName,path);
					reason = String.format("is unknown to provider %s", providerName);
				}
			}
			else {
				log.warnf("%s.validateTag: no provider for %s ",TAG,path);
				reason = "is not known to any provider";
			}
		}
		catch( IOException ioe) {
			log.warnf(TAG+"%s.localRequest: parse exception for path %s (%s)",TAG,path,ioe.getMessage());
			reason = "has an unparsable tag path";
		}
		catch(Exception ex) {
			log.warn(TAG+".validateTag: Exception ("+ex.getLocalizedMessage()+")");
			reason = ex.getMessage();
		}
		return reason;
	}
}
