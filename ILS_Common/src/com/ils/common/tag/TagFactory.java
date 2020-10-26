/**
 *   (c) 2013-2015  ILS Automation. All rights reserved.
 */
package com.ils.common.tag;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ils.common.ILSProperties;
import com.ils.common.persistence.ToolkitProperties;
import com.ils.common.persistence.ToolkitRecordHandler;
import com.inductiveautomation.ignition.common.BasicDataset;
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue;
import com.inductiveautomation.ignition.common.model.values.BasicQuality;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.Quality;
import com.inductiveautomation.ignition.common.sqltags.BasicTagValue;
import com.inductiveautomation.ignition.common.sqltags.TagDefinition;
import com.inductiveautomation.ignition.common.sqltags.model.Tag;
import com.inductiveautomation.ignition.common.sqltags.model.TagManagerBase;
import com.inductiveautomation.ignition.common.sqltags.model.TagManagerBase.CollisionPolicy;
import com.inductiveautomation.ignition.common.sqltags.model.TagNode;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.TagProp;
import com.inductiveautomation.ignition.common.sqltags.model.types.AccessRightsType;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.sqltags.model.types.ExpressionType;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagType;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagValue;
import com.inductiveautomation.ignition.common.sqltags.parser.BasicTagPath;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;
import com.inductiveautomation.ignition.common.sqltags.tagpaths.SourceAlteredTagPath;
import com.inductiveautomation.ignition.common.sqltags.tags.TagDiff;
import com.inductiveautomation.ignition.common.sqltags.udt.ComplexTagDefinition;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.sqltags.SQLTagsManager;
import com.inductiveautomation.ignition.gateway.sqltags.TagProvider;

/**
 *  Provide for the creation, deleting and updating of SQLTags. We handle
 *  both "simple" tag providers and the built-in providers to the extent 
 *  possible.
 */
public class TagFactory  {
	private static final String TAG = "TagFactory";
	private final LoggerEx log;
	private final GatewayContext context;
	private final SimpleDateFormat dateFormat;
	private final SQLTagsManager tagManager;
	private final List<TagPath> visitOrder;
	
	/**
	 * Constructor.
	 */
	public TagFactory(GatewayContext ctxt) {
		this.context = ctxt;
		this.dateFormat = new SimpleDateFormat(ILSProperties.TIMESTAMP_FORMAT);
		this.tagManager = context.getTagManager();
		this.visitOrder = new ArrayList<>();   // For tag replication
		log = LogUtil.getLogger(getClass().getPackage().getName());
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
		log.infof("%s.createExpression: [%s] %s (%s) = %s",TAG,providerName,tagPath,type,expr);
		TagPath tp = null;
		try {
			tp = TagPathParser.parse(providerName,tagPath);
		}
		catch(IOException ioe) {
			log.warnf("%s.createExpression: Exception parsing tag %s (%s)",TAG,tagPath,ioe.getLocalizedMessage());
			return;
		}
		
		DataType dataType = dataTypeFromString(type);
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null ) {
			try {
				// Guarantee that parent paths exist
				createParents(tp);
				List<TagNode> toAdd = new ArrayList<>();
				
				TagDefinition node = new TagDefinition(tp.getItemName(),TagType.DB);
				node.setDataType(dataType);
				node.setAttribute(TagProp.ExpressionType, new BasicTagValue(ExpressionType.Expression));
				node.setAttribute(TagProp.Expression, new BasicTagValue(expr));
				node.setEnabled(true);
				node.setAccessRights(AccessRightsType.Read_Write);
				toAdd.add(node);
				context.getTagManager().addTags(tp.getParentPath(), toAdd, TagManagerBase.CollisionPolicy.Overwrite);
			}
			catch(Exception ex) {
				log.warnf("%s.createExpression: Exception creating tag %s (%s)",TAG,tagPath,ex.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.createExpression: Provider %s does not exist",TAG,providerName);
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
		log.infof("%s.createExpressionWithHistory: [%s]%s (%s) = %s (%s)",TAG,providerName,tagPath,type,expr,historyProvider);
		tagPath = stripProvider(tagPath);
		TagPath tp = null;
		try {
			tp = TagPathParser.parse(providerName,tagPath);
		}
		catch(IOException ioe) {
			log.warnf("%s.createExpressionWithHistory: Exception parsing tag %s (%s)",TAG,tagPath,ioe.getLocalizedMessage());
			return;
		}
		
		DataType dataType = dataTypeFromString(type);
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null ) {
			try {
				// Guarantee that parent paths exist
				createParents(tp);
				List<TagNode> toAdd = new ArrayList<>();
			
				TagDefinition node = new TagDefinition(tp.getItemName(),TagType.DB);
				node.setDataType(dataType);
				node.setEnabled(true);
				node.setAttribute(TagProp.ExpressionType, new BasicTagValue(ExpressionType.Expression));
				node.setAttribute(TagProp.Expression, new BasicTagValue(expr));
				node.setAccessRights(AccessRightsType.Read_Write); 
				node.setAttribute(TagProp.HistoryEnabled, new BasicTagValue(Boolean.TRUE));
				node.setAttribute(TagProp.PrimaryHistoryProvider, new BasicTagValue(historyProvider));
				node.setAttribute(TagProp.HistoricalScanclass, new BasicTagValue("Default"));
				node.setAttribute(TagProp.ScanClass, new BasicTagValue("Default"));
				toAdd.add(node);
				context.getTagManager().addTags(tp.getParentPath(), toAdd, TagManagerBase.CollisionPolicy.Overwrite);
			}
			catch(Exception ex) {
				log.warnf("%s.createExpressionWithHistory: Exception creating tag %s (%s)",TAG,tagPath,ex.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.createExpressionWithHistory: Provider %s does not exist",TAG,providerName);
		}
	}

	private void createParents(TagPath path) {
		
		int segcount = path.getPathLength();
		int seg = 1;
		while(seg<segcount) {
			TagPath tp = BasicTagPath.subPath(path,0, seg);
			log.debugf("%s.createParents: Subpath = %s",TAG,tp.toStringFull());
			TagDefinition tag = new TagDefinition(tp.getItemName(),TagType.Folder);
			try {
				List<TagNode> toAdd = new ArrayList<>();
				toAdd.add(tag);
				context.getTagManager().addTags(tp.getParentPath(), toAdd, TagManagerBase.CollisionPolicy.Ignore);
			}
			catch(Exception ex) {
				log.warnf("%s.createParents: Exception creating tag folder %s (%s)",TAG,tp,ex.getLocalizedMessage());
			}
			seg++;
		}
	}
	/**
	 * The TagPath attribute "source" actually refers to the provider name. A full tag path includes
	 * the provider in brackets, a partial path does not. 
	 * @param providerName
	 * @param tagPath
	 * @param type String version of datatype
	 */
	public void createTag(String providerName, String tagPath, String type) {
		tagPath = stripProvider(tagPath);
		log.debugf("%s.createTag [%s]%s (%s)",TAG,providerName,tagPath,type);
		TagPath tp = null;
		try {
			tp = TagPathParser.parse(providerName,tagPath);
		}
		catch(IOException ioe) {
			log.warnf("%s.createTag: Exception parsing tag %s (%s)",TAG,tagPath,ioe.getLocalizedMessage());
			return;
		}
		// NOTE: Our experience here is that if the simple data provider, then the tags 
		//       show up in the designer SQLTagsBrowser. This is not the case when defining directly
		//       through the tag manager. Here the calls appear to succeed, but the tags do not show up.
		// In the cases where we need historical timestamps,  we use the simple tag provider.
		DataType dataType = dataTypeFromString(type);
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null ) {
			try {
				// Guarantee that parent paths exist
				createParents(tp);
				List<TagNode> toAdd = new ArrayList<>();

				TagDefinition node = new TagDefinition(tp.getItemName(),TagType.DB);
				node.setDataType(dataType);
				node.setEnabled(true);
				node.setAccessRights(AccessRightsType.Read_Write);    // Or Custom?
				toAdd.add(node);
				context.getTagManager().addTags(tp.getParentPath(), toAdd, TagManagerBase.CollisionPolicy.Overwrite);
			}
			catch(Exception ex) {
				log.warnf("%s.createTag: Exception creating tag %s (%s)",TAG,tagPath,ex.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.createTag: Provider %s does not exist",TAG,providerName);
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
		log.infof("%s.createTagWithHistory [%s]%s (%s) on %s",TAG,providerName,tagPath,type,historyProvider);
		TagPath tp = null;
		try {
			tp = TagPathParser.parse(providerName,tagPath);
		}
		catch(IOException ioe) {
			log.warnf("%s.createTagWithHistory: Exception parsing tag %s (%s)",TAG,tagPath,ioe.getLocalizedMessage());
			return;
		}
		DataType dataType = dataTypeFromString(type);
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null ) {
			try {
				// Guarantee that parent paths exist
				createParents(tp);
				List<TagNode> toAdd = new ArrayList<>();
				
				TagDefinition node = new TagDefinition(tp.getItemName(),TagType.DB);
				node.setDataType(dataType);
				node.setEnabled(true);
				node.setAccessRights(AccessRightsType.Read_Write);    // Or Custom?
				node.setAttribute(TagProp.HistoryEnabled, new BasicTagValue(Boolean.TRUE));
				node.setAttribute(TagProp.PrimaryHistoryProvider, new BasicTagValue(historyProvider));
				node.setAttribute(TagProp.HistoricalScanclass, new BasicTagValue("Default"));
				node.setAttribute(TagProp.ScanClass, new BasicTagValue("Default"));
				toAdd.add(node);
				context.getTagManager().addTags(tp.getParentPath(), toAdd, TagManagerBase.CollisionPolicy.Overwrite);
			}
			catch(Exception ex) {
				log.warnf("%s.createTagWithHistory: Exception creating tag %s (%s)",TAG,tagPath,ex.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.createTagWithHistory: Provider %s does not exist",TAG,providerName);
		}
	}
	public void deleteTag(String providerName, String tagPath) {
		tagPath = stripProvider(tagPath);
		log.debugf("%s.deleteTag [%s]%s",TAG,providerName,tagPath);
		TagPath tp = null;
		try {
			tp = TagPathParser.parse(providerName,tagPath);
		}
		catch(IOException ioe) {
			log.warnf("%s: deleteTag: Exception parsing tag %s (%s)",TAG,tagPath,ioe.getLocalizedMessage());
			return;
		}

		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null  ) {
			List<TagPath> tags = new ArrayList<TagPath>();
			tags.add(tp);
			try {
				context.getTagManager().removeTags(tags);
			}
			catch(Exception ex) {
				log.warnf("%s: deleteTag: Exception deleting tag %s (%s)",TAG,tagPath,ex.getLocalizedMessage());
			}
		}
		else {
			log.warnf("%s.deleteTag: Provider %s does not exist",TAG,providerName);
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
		Map<TagPath, List<TagPath>> nodeMap = findNodesUnderProvider(sourceProvider);
		copyTagsToNewProvider(sourceProvider,destProvider,nodeMap);
		log.infof("%s.replicate: ========== Complete =================", TAG);
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
		log.debugf("%s: updateExpression %s to %s",TAG,tagPath,expr);
		if(providerName==null || tagPath==null) return;

		TagPath tp = null;
		Tag tag    = null; 
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		if( provider != null  ) {
			try {
				tp = TagPathParser.parse(providerName,tagPath);
				tag = provider.getTag(tp);
			}
			catch(IOException ioe) {
				log.warnf(TAG+"%s: updateExpression: Exception parsing tag name (%s)",TAG,ioe.getLocalizedMessage());
				return;
			}
		}
		else {
			log.warn(TAG+"updateExpression: Provider "+providerName+" does not exist");
			return;
		}

		if( provider!=null ) { 
			List<TagPath> tags = new ArrayList<TagPath>();
			tags.add(tp);
			Quality q = new BasicQuality("ILS",Quality.Level.Good);
			QualifiedValue qv = new BasicQualifiedValue( expr,q);
			TagValue tv = new BasicTagValue(qv);
			TagDiff diff = new TagDiff();
			diff.setAttribute(TagProp.Expression,tv);
			try {
				context.getTagManager().editTags(tags, diff);
			}
			catch(Exception ex) {
				log.warnf("%s: updateTag: Exception updating %s (%s)",TAG,tp.toStringFull(),ex.getLocalizedMessage());
			}
		}
	}

	// =================================== Private Helper Methods ===================================
	private void clearTagsUnderProvider(String provider) {
		Map<TagPath, List<TagPath>> nodeMap = findNodesUnderProvider(provider);
		List<TagPath> pathsToDelete = new ArrayList<>();
		for (List<TagPath> node: nodeMap.values()) {
			for (TagPath path : node) {
				if (path != null) {
					pathsToDelete.add(path);
				}
			}
		}
		try {
			tagManager.removeTags(pathsToDelete);
		} 
		catch (Exception ex) {
			log.warnf("%s.clearTagsUnderProvider: Unsuccessful (%s)", TAG,ex.getMessage());
		}


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
		for (TagPath parent : visitOrder) {
			List<TagPath> paths = nodeMap.get(parent);
			if( paths!=null )  {
				List<TagNode> toAdd = new ArrayList<>();
				for (TagPath path : paths) {

					Tag tag = sourceProvider.getTag(path);
					TagDefinition node = null;
					if(tag != null && tag.getType() != TagType.UDT_DEF) {
						TagType ttype = tag.getType();
						if (ttype.isComplex()) {
							// Make a folder
							node = new TagDefinition(tag.getName(), TagType.Folder);
						} 
						else if(ttype.equals(TagType.OPC)) {
							node = new TagDefinition(tag);
							node.setType(TagType.DB);
							if( node.getValue() == null ) {
								Object value = "";
								if( node.getDataType()==DataType.Boolean ) value = Boolean.FALSE;
								else if( node.getDataType()==DataType.Float4 ||
										node.getDataType()==DataType.Float8) value = new Double(0.0);
								else if( node.getDataType()==DataType.Int1 ||
										node.getDataType()==DataType.Int2 ||
										node.getDataType()==DataType.Int4 ||
										node.getDataType()==DataType.Int8) value = new Integer(0);
								else if( node.getDataType()==DataType.DataSet) value = new BasicDataset();
								else if( node.getDataType()==DataType.DateTime) value = new Date();
								TagValue tv = new BasicTagValue(value);
								node.setValue(tv);
							}
						}
						else {
							node = new TagDefinition(tag);
						}
					}
					else if( tag!=null) {
						// Tag definition
						node = new ComplexTagDefinition(tag);
					}

					TagValue tv = null;
					// For a simple provider we add the node one-by-one, otherwise accumulate in list
					if( node!=null ) {
						// Configure the target history database. We copy attributes. Is this necessary?
						tv = tag.getAttribute(TagProp.HistoryEnabled);
						node.setAttribute(TagProp.HistoryEnabled,tv );
						tv = tag.getAttribute(TagProp.PrimaryHistoryProvider);
						node.setAttribute(TagProp.PrimaryHistoryProvider,tv );
						
						// In the case of a SQL Query Expression, we may want to set the database
						tv = tag.getAttribute(TagProp.SQLBindingDatasource);
						if( tv!=null && tv.getValue()!=null) {
							String database = tv.getValue().toString();
							if(fromProductionToIsolation && productionDatabase.equalsIgnoreCase(database) ) {
								tv = new BasicTagValue(isolationDatabase);
								node.setAttribute(TagProp.SQLBindingDatasource,tv );
							}
							else if(fromIsolationToProduction && isolationDatabase.equalsIgnoreCase(database) ) {
								tv = new BasicTagValue(productionDatabase);
								node.setAttribute(TagProp.SQLBindingDatasource,tv);
							}
						}		 
						toAdd.add(node);
					}

					if (log.isTraceEnabled()) {
						log.tracef("%s.copyTagstoNewProvider: tag=%s", TAG, path);
					}
				}

				try {
					if( !toAdd.isEmpty()) {
						parent = new SourceAlteredTagPath(parent, destination);
						tagManager.addTags(parent, toAdd, CollisionPolicy.Overwrite);
						log.debugf("%s.copyTagstoNewProvider: copied folder=%s",
								TAG, parent.toStringFull());
					}
				} 
				catch (Exception ex) {
					log.warnf("%s.copyTagstoNewProvider: %s unsuccessful (%s)",
							TAG, parent.toString(), ex.getMessage());
				}
			}
			else {
				// This is normal if we've cleared folders first
				log.debugf("%s.copyTagstoNewProvider: No nodes found under parent=%s",
						TAG, parent.toStringFull());
			}
		}
	}

	/**
	 * Convert a string data type into a data type object
	 */
	private DataType dataTypeFromString( String type ) {
		DataType result = DataType.valueOf(type);
		return result;
	}

	private Map<TagPath, List<TagPath>> findNodesUnderProvider(String providerName) {
		Map<TagPath, List<TagPath>> nodeMap = new HashMap<>();
		TagProvider provider = context.getTagManager().getTagProvider(providerName);
		TagPath root = new BasicTagPath(providerName);
		recursivelyBrowse(root, provider, nodeMap);
		return nodeMap;
	}
	/**
	 * Add to the map. Map is keyed by folder. For each folder we create a list
	 * of nodes. This allows us to add as a group later on ...
	 * 
	 * @param root
	 * @param nodes
	 */
	private void recursivelyBrowse(TagPath rootPath,TagProvider provider, Map<TagPath, List<TagPath>> nodeMap) {
		log.tracef("%s.recursivelyBrowse %s", TAG, rootPath.toStringFull()); 
		List<Tag> children = provider.browse(rootPath);
		if (children != null && !children.isEmpty() ) {
			// Only add to the folder list if there are some children
			List<TagPath> nodes = nodeMap.get(rootPath);
			if (nodes == null) {
				nodes = new ArrayList<TagPath>();
				nodeMap.put(rootPath, nodes);
				visitOrder.add(rootPath);
				log.debugf("%s.recursivelyBrowse: added to visit order %s", TAG,rootPath.toStringFull());
			}

			for (Tag child:children) {
				TagPath childPath = rootPath.getChildPath(child.getName());
				log.tracef("%s.recursivelyBrowse: added child %s", TAG,childPath.toStringFull());
				nodes.add(childPath);
				recursivelyBrowse(childPath, provider,nodeMap);
			}
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
