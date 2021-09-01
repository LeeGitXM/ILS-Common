/**
 *   (c) 2013-2021  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;
import com.ils.common.persistence.ToolkitProperties;
import com.ils.common.persistence.ToolkitRecordHandler;
import com.inductiveautomation.ignition.common.browsing.BrowseFilter;
import com.inductiveautomation.ignition.common.browsing.Results;
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.TagDefinition;
import com.inductiveautomation.ignition.common.sqltags.model.TagProp;
import com.inductiveautomation.ignition.common.sqltags.model.types.AccessRightsType;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.sqltags.model.types.ExpressionType;
import com.inductiveautomation.ignition.common.tags.browsing.NodeDescription;
import com.inductiveautomation.ignition.common.tags.config.BasicTagConfiguration;
import com.inductiveautomation.ignition.common.tags.config.CollisionPolicy;
import com.inductiveautomation.ignition.common.tags.config.TagConfiguration;
import com.inductiveautomation.ignition.common.tags.config.TagConfigurationModel;
import com.inductiveautomation.ignition.common.tags.config.types.TagObjectType;
import com.inductiveautomation.ignition.common.tags.model.SecurityContext;
import com.inductiveautomation.ignition.common.tags.model.TagPath;
import com.inductiveautomation.ignition.common.tags.model.TagProvider;
import com.inductiveautomation.ignition.common.tags.paths.BasicTagPath;
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

/**
 *  Provide for the creation, deleting and updating of SQLTags. We handle
 *  both "simple" tag providers and the built-in providers to the extent 
 *  possible.
 */
public class TagFactory  {
	private static final String CLSS = "TagFactory";
	private final ILSLogger log;
	private final GatewayContext context;
	private final List<TagPath> visitOrder;
	
	/**
	 * Constructor.
	 */
	public TagFactory(GatewayContext ctxt) {
		this.context = ctxt;
		this.visitOrder = new ArrayList<>();   // For tag replication
		log = LogMaker.getLogger(getClass().getPackage().getName());
	}
	
	/**
	 * An Expression is just a tag with an expression attribute. This method creates a tag with
	 * the supplied expression.
	 * The TagPath attribute "source" actually refers to the provider name. A full tag path includes
	 * the provider in brackets, a partial path does not. 
	 * @param providerName name of the tag provider
	 * @param tagPath
	 * @param type - data type
	 */
	public void createExpression(String providerName, String tagPath, String type, String expr) {
		tagPath = stripProvider(tagPath);
		log.infof("%s.createExpression: [%s] %s (%s) = %s",CLSS,providerName,tagPath,type,expr);
		TagPath tp = null;
		try {
			tp = TagPathParser.parse(providerName,tagPath);
		}
		catch(IOException ioe) {
			log.warnf("%s.createExpression: Exception parsing tag %s (%s)",CLSS,tagPath,ioe.getLocalizedMessage());
			return;
		}
		
		DataType dataType = dataTypeFromString(type);
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null ) {
			try {
				// Guarantee that parent paths exist
				createParents(provider,tp);
				List<TagConfiguration> toAdd = new ArrayList<>();	
				BasicTagConfiguration node = new BasicTagConfiguration();
				node.setPath(tp);
				node.setType(TagObjectType.AtomicTag);
				node.set(TagProp.DataType,dataType);
				node.set(TagProp.ExpressionType, new BasicQualifiedValue(ExpressionType.Expression));
				node.set(TagProp.Expression, new BasicQualifiedValue(expr));
				node.set(TagProp.Enabled,true);
				node.set(TagProp.AccessRights,AccessRightsType.Read_Write);
				toAdd.add(node);
				CompletableFuture<List<QualityCode>> future = provider.saveTagConfigsAsync(toAdd,CollisionPolicy.Overwrite,SecurityContext.systemContext());
				future.get();
			}
			catch(Exception ex) {
				log.warnf("%s.createExpression: Exception creating tag %s (%s)",CLSS,tagPath,ex.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.createExpression: Provider %s does not exist",CLSS,providerName);
		}
	}
	
	/**
	 * Create an expression that records a history. The supplied history provider
	 * must already exist as the tag history tables will be created in it.
	 * The TagPath attribute "source" actually refers to the provider name. A full tag path includes
	 * the provider in brackets, a partial path does not. 
	 * @param providerName name of the tag provider
	 * @param tagPath
	 * @param type - data type
	 * @param history
	 */
	public void createExpressionWithHistory(String providerName, String tagPath, String type, String expr,String historyProvider) {
		log.infof("%s.createExpressionWithHistory: [%s]%s (%s) = %s (%s)",CLSS,providerName,tagPath,type,expr,historyProvider);
		tagPath = stripProvider(tagPath);
		TagPath tp = null;
		try {
			tp = TagPathParser.parse(providerName,tagPath);
		}
		catch(IOException ioe) {
			log.warnf("%s.createExpressionWithHistory: Exception parsing tag %s (%s)",CLSS,tagPath,ioe.getLocalizedMessage());
			return;
		}
		
		DataType dataType = dataTypeFromString(type);
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null ) {
			try {
				// Guarantee that parent paths exist
				createParents(provider,tp);
				List<TagConfiguration> toAdd = new ArrayList<>();
				BasicTagConfiguration node = new BasicTagConfiguration();
				node.setPath(tp);
				node.setType(TagObjectType.AtomicTag);
				node.set(TagProp.DataType,dataType);
				node.set(TagProp.Enabled,true);
				node.set(TagProp.AccessRights,AccessRightsType.Read_Write);
				node.set(TagProp.ExpressionType, new BasicQualifiedValue(ExpressionType.Expression));
				node.set(TagProp.Expression, new BasicQualifiedValue(expr)); 
				node.set(TagProp.HistoryEnabled, new BasicQualifiedValue(Boolean.TRUE));
				node.set(TagProp.PrimaryHistoryProvider, new BasicQualifiedValue(historyProvider));
				node.set(TagProp.HistoricalScanclass, new BasicQualifiedValue("Default"));
				node.set(TagProp.ScanClass, new BasicQualifiedValue("Default"));
				toAdd.add(node);
				CompletableFuture<List<QualityCode>> future = provider.saveTagConfigsAsync(toAdd,CollisionPolicy.Overwrite,SecurityContext.systemContext());
				future.get();
			}
			catch(Exception ex) {
				log.warnf("%s.createExpressionWithHistory: Exception creating tag %s (%s)",CLSS,tagPath,ex.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.createExpressionWithHistory: Provider %s does not exist",CLSS,providerName);
		}
	}

	private void createParents(TagProvider provider,TagPath path) {	
		int segcount = path.getPathLength();
		int seg = 1;
		while(seg<segcount) {
			TagPath tp = BasicTagPath.subPath(path,0, seg);
			log.debugf("%s.createParents: Subpath = %s",CLSS,tp.toStringFull());
			BasicTagConfiguration tag = new BasicTagConfiguration();
			tag.setPath(tp);
			tag.setType(TagObjectType.Folder);
			try {
				List<TagConfiguration> toAdd = new ArrayList<>();
				toAdd.add(tag);
				CompletableFuture<List<QualityCode>> future = provider.saveTagConfigsAsync(toAdd,CollisionPolicy.Overwrite,SecurityContext.systemContext());
				future.get();
			}
			catch(Exception ex) {
				log.warnf("%s.createParents: Exception creating tag folder %s (%s)",CLSS,tp,ex.getLocalizedMessage());
			}
			seg++;
		}
	}
	/**
	 * Create a simple tag. 
	 * The TagPath attribute "source" actually refers to the provider name. A full tag path includes
	 * the provider in brackets, a partial path does not. 
	 * @param providerName
	 * @param tagPath
	 * @param type String version of datatype
	 */
	public void createTag(String providerName, String tagPath, String type) {
		DataType dataType = dataTypeFromString(type);
		createTag(providerName,tagPath,dataType);
	}
	/**
	 * The TagPath attribute "source" actually refers to the provider name. A full tag path includes
	 * the provider in brackets, a partial path does not. 
	 * @param providerName
	 * @param tagPath
	 * @param type String version of datatype
	 */
	public void createTag(String providerName, String tagPath, DataType dataType) {
		tagPath = stripProvider(tagPath);
		log.debugf("%s.createTag [%s]%s (%s)",CLSS,providerName,tagPath,dataType.name());
		TagPath tp = null;
		try {
			tp = TagPathParser.parse(providerName,tagPath);
		}
		catch(IOException ioe) {
			log.warnf("%s.createTag: Exception parsing tag %s (%s)",CLSS,tagPath,ioe.getLocalizedMessage());
			return;
		}
		// NOTE: Our experience here is that if the simple data provider, then the tags 
		//       show up in the designer SQLTagsBrowser. This is not the case when defining directly
		//       through the tag manager. Here the calls appear to succeed, but the tags do not show up.
		// In the cases where we need historical timestamps,  we use the simple tag provider.
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null ) {
			try {
				// Guarantee that parent paths exist
				createParents(provider,tp);
				List<TagConfiguration> toAdd = new ArrayList<>();

				BasicTagConfiguration node = new BasicTagConfiguration();
				node.setPath(tp);
				node.setType(TagObjectType.AtomicTag);
				node.set(TagProp.DataType,dataType);
				node.set(TagProp.Enabled,true);
				node.set(TagProp.AccessRights,AccessRightsType.Read_Write);
				toAdd.add(node);
				CompletableFuture<List<QualityCode>> future = provider.saveTagConfigsAsync(toAdd,CollisionPolicy.Overwrite,SecurityContext.systemContext());
				future.get();
			}
			catch(Exception ex) {
				log.warnf("%s.createTag: Exception creating tag %s (%s)",CLSS,tagPath,ex.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.createTag: Provider %s does not exist",CLSS,providerName);
		}
	}

	/**
	 * Create a tag that has history collection.  
	 * @param providerName (not a "simple" provider)
	 * @param tagPath
	 * @param type String version of datatype
	 * @param historyProvider the datasource containing the history
	 */
	public void createTagWithHistory(String providerName, String tagPath, String type,String historyProvider) {
		tagPath = stripProvider(tagPath);
		log.infof("%s.createTagWithHistory [%s]%s (%s) on %s",CLSS,providerName,tagPath,type,historyProvider);
		TagPath tp = null;
		try {
			tp = TagPathParser.parse(providerName,tagPath);
		}
		catch(IOException ioe) {
			log.warnf("%s.createTagWithHistory: Exception parsing tag %s (%s)",CLSS,tagPath,ioe.getLocalizedMessage());
			return;
		}
		DataType dataType = dataTypeFromString(type);
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null ) {
			try {
				// Guarantee that parent paths exist
				createParents(provider,tp);
				List<TagConfiguration> toAdd = new ArrayList<>();
				
				BasicTagConfiguration node = new BasicTagConfiguration();
				node.setPath(tp);
				node.setType(TagObjectType.AtomicTag);
				node.set(TagProp.DataType,dataType);
				node.set(TagProp.Enabled,true);;
				node.set(TagProp.AccessRights,AccessRightsType.Read_Write);
				node.set(TagProp.HistoryEnabled, new BasicQualifiedValue(Boolean.TRUE));
				node.set(TagProp.PrimaryHistoryProvider, new BasicQualifiedValue(historyProvider));
				node.set(TagProp.HistoricalScanclass, new BasicQualifiedValue("Default"));
				node.set(TagProp.ScanClass, new BasicQualifiedValue("Default"));
				toAdd.add(node);
				CompletableFuture<List<QualityCode>> future = provider.saveTagConfigsAsync(toAdd,CollisionPolicy.Overwrite,SecurityContext.systemContext());
				future.get();
			}
			catch(Exception ex) {
				log.warnf("%s.createTagWithHistory: Exception creating tag %s (%s)",CLSS,tagPath,ex.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.createTagWithHistory: Provider %s does not exist",CLSS,providerName);
		}
	}
	public void deleteTag(String providerName, String tagPath) {
		tagPath = stripProvider(tagPath);
		log.debugf("%s.deleteTag [%s]%s",CLSS,providerName,tagPath);
		TagPath tp = null;
		try {
			tp = TagPathParser.parse(providerName,tagPath);
		}
		catch(IOException ioe) {
			log.warnf("%s: deleteTag: Exception parsing tag %s (%s)",CLSS,tagPath,ioe.getLocalizedMessage());
			return;
		}

		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null  ) {
			List<TagPath> tags = new ArrayList<TagPath>();
			tags.add(tp);
			try {
				CompletableFuture<List<QualityCode>> future = provider.removeTagConfigsAsync(tags, SecurityContext.systemContext());
				future.get();
			}
			catch(Exception ex) {
				log.warnf("%s: deleteTag: Exception deleting tag %s (%s)",CLSS,tagPath,ex.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.deleteTag: Provider %s does not exist",CLSS,providerName);
		}
	}
	/**
	 * Rename a tag keeping the folder structure intact. If the tag does not
	 * exist or the rename fails, then create the tag as a String.
	 * @param provider name
	 * @param name new name
	 * @param path existing complete path. Any provider here will be ignored
	 */
	public void renameTag(String providerName,String name,String source) {
		log.infof("%s.renameTag %s [%s]%s",CLSS,name,providerName,source);
		String destination = TagUtility.replaceTagNameInPath(name,source);
		TagValidator validator = new TagValidator(context);
		if( validator.exists(source) ) {
			TagPath tp = null;
			try {
				tp = TagPathParser.parse(providerName,source);
				List<TagPath> tags = new ArrayList<>();
				tags.add(tp);
				TagPath destPath = TagPathParser.parse(providerName,destination);
				CompletableFuture<List<QualityCode>> future = context.getTagManager().moveTagsAsync(tags, destPath, false, CollisionPolicy.Overwrite);
				future.get();
			}
			catch(IOException ioe) {
				log.warnf("%s: renameTag: Exception parsing tag [%s]%s (%s)",CLSS,providerName,source,ioe.getLocalizedMessage());
				return;
			}
			catch(Exception ex) {
				log.warnf("%s: renameTag: Exception renaming tag [%s]%s (%s)",CLSS,providerName,source,ex.getLocalizedMessage());
				createTag(providerName,destination,DataType.String);
				return;
			}
		}
		else {
			log.warnf("%s: renameTag: referenced tag [%s] did not exist. %s created",CLSS,source,destination);
			createTag(providerName,destination,DataType.String);
			return;
		}
	}
	/**
	 * Copy the specified provider. If the flag is set, the destination provider will be cleared 
	 * before receiving the new tag definitions.
	 * 
	 * @param fromProvider, the name of the source provider.
	 * @param toProvider
	 * @param clear
	 */
	public void replicateProvider(String sourceProvider,String destProvider,boolean clear) {
		visitOrder.clear(); // Visit order keeps track of folder order
		if( clear ) clearTagsUnderProvider(destProvider);
		// The map contains a list of children keyed by parent path
		TagProvider provider = context.getTagManager().getTagProvider(sourceProvider);
		Map<TagPath, List<TagPath>> nodeMap = findNodesUnderProvider(provider);
		copyTagsToNewProvider(sourceProvider,destProvider,nodeMap);
		log.infof("%s.replicate: ========== Complete =================", CLSS);
	}
	/**
	 * Update a tag expression. If the tag was created by a simple provider, then we use that interface.
	 * Otherwise use the standard TagProvider constructs. If the value is "BAD", then conclude that 
	 * the quality is bad.
	 * 
	 * @param providerName
	 * @param tagPath
	 * @param expr, the new expression.
	 */
	public synchronized void updateExpression(String providerName, String tagPath, String expr) {
		tagPath = stripProvider(tagPath);
		log.debugf("%s: updateExpression %s to %s",CLSS,tagPath,expr);
		if(providerName==null || tagPath==null) return;

		TagPath tp = null; 
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null  ) {
			try {
				tp = TagPathParser.parse(providerName,tagPath);
			}
			catch(IOException ioe) {
				log.warnf(CLSS+"%s: updateExpression: Exception parsing tag name (%s)",CLSS,ioe.getLocalizedMessage());
				return;
			}
		}
		else {
			log.warn(CLSS+"updateExpression: Provider "+providerName+" does not exist");
			return;
		}

		if( provider!=null ) { 
			List<TagPath> paths = new ArrayList<>();
			paths.add(tp);
			CompletableFuture<List<TagConfigurationModel>> modelFuture = provider.getTagConfigsAsync(paths,false,false);
			try {
				List<TagConfigurationModel> models = modelFuture.get();
				List<TagConfiguration> tags = new ArrayList<>();
				TagConfiguration node = models.get(0).getLocalConfiguration(); // There is only one.
				QualifiedValue tv = new BasicQualifiedValue(expr);
				node.set(TagProp.Expression,tv);
				tags.add(node);

				CompletableFuture<List<QualityCode>> future = provider.saveTagConfigsAsync(tags,CollisionPolicy.Ignore,SecurityContext.systemContext());
				future.get();
			}
			catch(Exception ex) {
				log.warnf("%s: updateTag: Exception updating %s (%s)",CLSS,tp.toStringFull(),ex.getLocalizedMessage());
			}
		}
	}

	// =================================== Private Helper Methods ===================================
	private void clearTagsUnderProvider(String providerName) {
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		Map<TagPath, List<TagPath>> nodeMap = findNodesUnderProvider(provider);
		List<TagPath> pathsToDelete = new ArrayList<>();
		for (List<TagPath> node: nodeMap.values()) {
			for (TagPath path : node) {
				if (path != null) {
					pathsToDelete.add(path);
				}
			}
		}
		provider.removeTagConfigsAsync(pathsToDelete, SecurityContext.systemContext());
	}
	private void copyTagsToNewProvider(String source,String destination,Map<TagPath,List<TagPath>> nodeMap) {
		// In the case of Query tags going to and from production to isolation, we need to change the database 
		// binding also.  Start by reading the configuration ... If not configured, values are empty.
		ToolkitRecordHandler toolkitHandler = new ToolkitRecordHandler(context);
		String productionProvider = toolkitHandler.getToolkitProperty(ToolkitProperties.TOOLKIT_PROPERTY_PROVIDER);
		String isolationProvider  = toolkitHandler.getToolkitProperty(ToolkitProperties.TOOLKIT_PROPERTY_ISOLATION_PROVIDER);
		String productionDatabase = toolkitHandler.getToolkitProperty(ToolkitProperties.TOOLKIT_PROPERTY_DATABASE);
		String isolationDatabase  = toolkitHandler.getToolkitProperty(ToolkitProperties.TOOLKIT_PROPERTY_ISOLATION_DATABASE);
		boolean fromProductionToIsolation = false;
		if( source.equalsIgnoreCase(productionProvider) && destination.equalsIgnoreCase(isolationProvider)) fromProductionToIsolation = true;
		boolean fromIsolationToProduction = false;
		if( source.equalsIgnoreCase(isolationProvider) && destination.equalsIgnoreCase(productionProvider)) fromIsolationToProduction = true;
				
		TagProvider sourceProvider = context.getTagManager().getTagProvider(source);
		TagProvider destinationProvider = context.getTagManager().getTagProvider(destination);
		if( sourceProvider==null || destinationProvider==null ) {
			log.warnf("%s.copyTagsToNewProvider: One of providers (%s or %s) does not exist",CLSS,source,destination);
			return;
		}
		TagPath tp = null;
		try {
			for (TagPath parent : visitOrder) {
				List<TagPath> paths = nodeMap.get(parent);
				List<TagConfiguration>destinationTags = new ArrayList<>();
				if( paths!=null )  {
					CompletableFuture<List<TagConfigurationModel>> futureSource = sourceProvider.getTagConfigsAsync(paths, false, true);
					List<TagConfigurationModel> sourceTags = futureSource.get();
					// Convert the node to the target configuration
					for(TagConfigurationModel tagModel:sourceTags) {
						TagConfiguration node = tagModel.getLocalConfiguration();
						// Convert from source to destination tag path
						tp = node.getPath();
						String path = tp.toStringFull();
						String destFullpath = TagUtility.replaceProviderInPath(destination, path);
						TagPath destPath = TagPathParser.parse(destFullpath);
						node.setPath(destPath);
						// In the case of a SQL Query Expression, we may want to set the database
						Object db = node.get(TagProp.SQLBindingDatasource);
						if( db!=null ) {
							String database = db.toString();
							if(fromProductionToIsolation && productionDatabase.equalsIgnoreCase(database) ) {
								db = new BasicQualifiedValue(isolationDatabase);
								node.set(TagProp.SQLBindingDatasource,db );
							}
							else if(fromIsolationToProduction && isolationDatabase.equalsIgnoreCase(database) ) {
								db = new BasicQualifiedValue(productionDatabase);
								node.set(TagProp.SQLBindingDatasource,db);
							}		 
						}
						log.tracef("%s.copyTagstoNewProvider: tag=%s", CLSS, path);
						destinationTags.add(node);
					}

					try {
						destinationProvider.saveTagConfigsAsync(destinationTags, CollisionPolicy.MergeOverwrite, SecurityContext.systemContext());
					} 
					catch (Exception ex) {
						log.warnf("%s.copyTagstoNewProvider: %s unsuccessful (%s)",
								CLSS, parent.toString(), ex.getMessage());
					}
				}
				else {
					// This is normal if we've cleared folders first
					log.debugf("%s.copyTagstoNewProvider: No nodes found under parent=%s",
							CLSS, parent.toStringFull());
				}
			}
		}
		catch(InterruptedException ie) {
			log.warnf("%s.copyTagstoNewProvider: Interrupted for %s (%s)",CLSS, tp.toStringFull(),ie.getLocalizedMessage());
		}
		catch(ExecutionException exex) {
			log.warnf("%s.copyTagstoNewProvider: Executopm error for %s (%s)",CLSS, tp.toStringFull(),exex.getLocalizedMessage());
		}
		catch(IOException ioe) {
			log.warnf("%s.copyTagstoNewProvider: Parse error for %s (%s)",CLSS, tp.toStringFull(),ioe.getLocalizedMessage());
		}
	}

	/**
	 * Convert a string data type into a data type object
	 */
	private DataType dataTypeFromString( String type ) {
		DataType result = DataType.valueOf(type);
		return result;
	}

	private Map<TagPath, List<TagPath>> findNodesUnderProvider(TagProvider provider) {
		Map<TagPath, List<TagPath>> nodeMap = new HashMap<>();
		BrowseFilter filter = new BrowseFilter();
		filter.setRecursive(true);
		TagPath root = new BasicTagPath(provider.getName());
		recursivelyBrowse(root, provider, filter, nodeMap);
		return nodeMap;
	}

	/**
	 * Add to the map. Map is keyed by folder. For each folder we create a list
	 * of nodes. This allows us to add as a group later on ...
	 * @param provider 
	 * @param root
	 * @param nodes
	 */
	private void recursivelyBrowse(TagPath rootPath,TagProvider provider, BrowseFilter filter, Map<TagPath, List<TagPath>> nodeMap) {
		log.tracef("%s.recursivelyBrowse %s", CLSS, rootPath.toStringFull()); 
		CompletableFuture<Results<NodeDescription>> future  = provider.browseAsync(rootPath,filter);
		try {
			Results<NodeDescription> children = future.get();
			if (children != null && children.getReturnedSize()>0 ) {
				// Only add to the folder list if there are some children
				List<TagPath> nodes = nodeMap.get(rootPath);
				if (nodes == null) {
					nodes = new ArrayList<TagPath>();
					nodeMap.put(rootPath, nodes);
					visitOrder.add(rootPath);
					log.debugf("%s.recursivelyBrowse: added to visit order %s", CLSS,rootPath.toStringFull());
				}

				for (NodeDescription child:children.getResults()) {
					TagPath childPath = rootPath.getChildPath(child.getName());
					log.tracef("%s.recursivelyBrowse: added child %s", CLSS,childPath.toStringFull());
					nodes.add(childPath);
					recursivelyBrowse(childPath, provider,filter,nodeMap);
				}
			}
		}
		catch(InterruptedException ie) {
			log.warnf("%s.recursivelyBrowse: interrupted at child %s", CLSS,rootPath.toStringFull());
		}
		catch(ExecutionException eex) {
			log.warnf("%s.recursivelyBrowse: added child %s", CLSS,rootPath.toStringFull());
		}
	}
	// If the tag path has a source (provider), strip it off.
	// This is for use with commands that explicitly specify
	// the provider.
	private String stripProvider(String path) {
		int pos = path.indexOf("]");
		if(pos>0) path = path.substring(pos+1);
		return path;
	}
	
	// Dump whatever information we can glean from a TagPath
	protected String tagDefinitionToString(TagDefinition tagDef) {
		return String.format("%s (type=%s, nproperties=%d,noverrides=%d )",
				tagDef.getName(), tagDef.getDataType().toString(), tagDef
				.getProperties().size(), tagDef.getPropertyOverrides()
				.size());
	}

}
