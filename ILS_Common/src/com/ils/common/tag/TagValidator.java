/**
 *   (c) 2015  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.inductiveautomation.ignition.common.tags.config.TagConfigurationModel;
import com.inductiveautomation.ignition.common.tags.config.types.TagObjectType;
import com.inductiveautomation.ignition.common.tags.model.TagPath;
import com.inductiveautomation.ignition.common.tags.model.TagProvider;
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;


/**
 *  Given a tag path, verify that the tag exists. The tags are assumed to be
 *  project-gateway tags. The "dataset" tag type is flagged as bad.
 */
public class TagValidator  {
	private static final String TAG = "TagValidator";
	private static final long TIMEOUT = 100;
	private final LoggerEx log;
	private final GatewayContext context;
	
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
				List<TagPath>paths = new ArrayList<>();
				paths.add(tp);
			    CompletableFuture<List<TagConfigurationModel>> future = provider.getTagConfigsAsync(paths,false,false);
			    try {
			    	future.get(TIMEOUT,TimeUnit.MILLISECONDS);
			    	exists = true;
			    }
			    catch(ExecutionException exex) {
			    	log.infof("%s.exists: Execution exception getting tag %s (%s)d",TAG,path,exex.getLocalizedMessage());
			    }
			    catch(TimeoutException timeex) {
			    	log.infof("%s.exists: Timeout exception getting tag %s (%s)d",TAG,path,timeex.getLocalizedMessage());
			    }
			    catch(InterruptedException iex) {
			    	log.infof("%s.exists: Interrupted exception getting tag %s (%s)",TAG,path,iex.getLocalizedMessage());
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
		if(path.endsWith("]") )           return null;  // Only the provider, no path
		try {
			TagPath tp = TagPathParser.parse(null,path);
			String providerName = TagUtility.providerNameFromPath(path);
			// NOTE: For a simple tag, this returns the internal provider, a real provider.
			TagProvider provider = context.getTagManager().getTagProvider(providerName);
			// We assume the same provider
			if(provider != null) {
				List<TagPath>paths = new ArrayList<>();
				paths.add(tp);
			    CompletableFuture<List<TagConfigurationModel>> future = provider.getTagConfigsAsync(paths,false,false);
			    try {
			    	List<TagConfigurationModel> configs = future.get(TIMEOUT,TimeUnit.MILLISECONDS);
			    	TagConfigurationModel tag = configs.get(0);
			    	if(tag.isRemoved()) {
			    		reason = " is removed";
			    	}
			    	else if(tag.getType().isComplexTag()   ) {
						reason = String.format("tag type (%s) is not a simple type",tag.getType().toString());
					}
			    	else {
			    		TagObjectType tot = tag.getType();
						if( !tot.equals(TagObjectType.AtomicTag)) {
							reason = String.format("datatype (%s) is not recognized",tot.toString());
						}
			    	}
			    }
			    catch(ExecutionException exex) {
			    	reason = String.format(" Execution exception getting tag %s (%s)d",path,exex.getLocalizedMessage());
			    }
			    catch(TimeoutException timeex) {
			    	reason = String.format(" Timeout exception getting tag %s (%s)d",path,timeex.getLocalizedMessage());
			    }
			    catch(InterruptedException iex) {
			    	reason = String.format(" Interrupted exception getting tag %s (%s)d",path,iex.getLocalizedMessage());
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
