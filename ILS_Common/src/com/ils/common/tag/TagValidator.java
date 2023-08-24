/**
 *   (c) 2015  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.inductiveautomation.ignition.common.browsing.BrowseFilter;
import com.inductiveautomation.ignition.common.browsing.Results;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.tags.browsing.NodeDescription;
import com.inductiveautomation.ignition.common.tags.config.TagConfigurationModel;
import com.inductiveautomation.ignition.common.tags.config.properties.WellKnownTagProps;
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
	private static final String CLSS = "TagValidator";
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
	
	private void browseNode(TagProvider provider, TagPath parentPath) {
		log.infof("In browseNode()");
		try {
		    Results<NodeDescription> results = provider.browseAsync(parentPath, BrowseFilter.NONE).get(30, TimeUnit.SECONDS);
		    
		    if(results.getResultQuality().isNotGood()) {
		        log.infof("*** Bad search - Tag does not exist ***");
		        return;
		    }
		 
		    Collection<NodeDescription> nodes = results.getResults();
		    StringBuilder structure = new StringBuilder();
		    for(int i = 0; i<parentPath.getPathLength(); i++) {
		        structure.append("\t");
		    }
		 
		    String formatted = structure.toString() + "[%s] objectType=%s, dataType=%s, subTypeId=%s, currentValue=%s, displayFormat=%s, attributes=%s, hasChildren=%s";
		    for(NodeDescription node: nodes) {
		        String currentValue = node.getCurrentValue().getValue() != null ? node.getCurrentValue().getValue().toString(): "null";
		        String descr = String.format(formatted, node.getName(),
		            node.getObjectType(),
		            node.getDataType(),
		            node.getSubTypeId(),
		            currentValue,
		            node.getDisplayFormat(),
		            node.getAttributes().toString(),
		            node.hasChildren());
		        log.infof("Tag description: %s", descr);

		    
			 // Browse child nodes, but not Document nodes such as UDT parameters
			 if(node.hasChildren() && DataType.Document != node.getDataType()) {
			            TagPath childPath = parentPath.getChildPath(node.getName());
			            browseNode(provider, childPath);
			        }
			    }
		}
	    catch(ExecutionException exex) {
	    	log.infof("%s.exists: Execution exception getting tag %s (%s)d", CLSS, parentPath.toString(), exex.getLocalizedMessage());
	    }
	    catch(TimeoutException timeex) {
	    	log.infof("%s.exists: Timeout exception getting tag %s (%s)d", CLSS, parentPath.toString(), timeex.getLocalizedMessage());
	    }
	    catch(InterruptedException iex) {
	    	log.infof("%s.exists: Interrupted exception getting tag %s (%s)", CLSS, parentPath.toString(), iex.getLocalizedMessage());
	    }
	}
	
	/*
	 * This was totally reworked by Pete 8/24/2023.  It was not correctly upgraded for Ignition 8.
	 */
	public boolean exists(String path) {
		log.tracef("%s.exists(): Checking if tag <%s> exists...", CLSS, path);
		boolean exists = false;
		TagPath tp = null;
		try {
			tp = TagPathParser.parse(null,path);
			String providerName = tp.getSource();
			// If provider is null, then the tagPath parser will use the default provider
			TagProvider provider = context.getTagManager().getTagProvider(providerName);
			if(provider != null) {
				
			    Results<NodeDescription> results = provider.browseAsync(tp, BrowseFilter.NONE).get(30, TimeUnit.SECONDS);
			    
			    if(results.getResultQuality().isNotGood()) {
			        log.tracef("*** Tag does not exist ***");
			        exists = false;
			    }
			    else {
			    	log.tracef("--- the tag exists ---");
			    	exists = true;
			    }
			}
		}
	    catch(ExecutionException exex) {
	    	log.warnf("%s.exists: Execution exception getting tag %s (%s)d",CLSS,path,exex.getLocalizedMessage());
	    }
	    catch(TimeoutException timeex) {
	    	log.warnf("%s.exists: Timeout exception getting tag %s (%s)d",CLSS,path,timeex.getLocalizedMessage());
	    }
	    catch(InterruptedException iex) {
	    	log.warnf("%s.exists: Interrupted exception getting tag %s (%s)",CLSS,path,iex.getLocalizedMessage());
	    }		
		catch(IOException ioe) {
			log.warnf("%s.exists: Exception parsing tag %s (%s)",CLSS,path,ioe.getMessage());
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
		log.debugf("%s.validateTag: %s",CLSS,path);
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
				log.warnf("%s.validateTag: no provider for %s ",CLSS,path);
				reason = "is not known to any provider";
			}
		}
		catch( IOException ioe) {
			log.warnf(CLSS+"%s.localRequest: parse exception for path %s (%s)",CLSS,path,ioe.getMessage());
			reason = "has an unparsable tag path";
		}
		catch(Exception ex) {
			log.warn(CLSS+".validateTag: Exception ("+ex.getLocalizedMessage()+")");
			reason = ex.getMessage();
		}
		return reason;
	}
}
