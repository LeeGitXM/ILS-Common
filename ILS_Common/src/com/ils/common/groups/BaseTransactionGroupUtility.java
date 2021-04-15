/**
 *   (c) 2016-2021  ILS Automation. All rights reserved.
 *   @See: sql-bridge-common-api.jar, sql-bridge-designer-api.jar
 */

package com.ils.common.groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.python.core.PyList;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;
import com.inductiveautomation.factorysql.common.config.CommonGroupProperties;
import com.inductiveautomation.factorysql.common.config.CommonItemProperties;
import com.inductiveautomation.factorysql.common.config.GroupConfig;
import com.inductiveautomation.factorysql.common.config.ItemConfig;
import com.inductiveautomation.ignition.common.metaproperties.MetaProperty;
import com.inductiveautomation.ignition.common.project.Project;
import com.inductiveautomation.ignition.common.project.resource.ProjectResource;
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceId;
import com.inductiveautomation.ignition.common.xmlserialization.SerializationException;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.DeserializationContext;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.XMLDeserializer;

/**
 * This class presents several static utility methods dealing with transaction groups.
 * The methods that are abstracted can be overwritten for either gateway or designer scopes.
 */
public abstract class BaseTransactionGroupUtility {
	private final static String CLSS = "BaseTransactionGroupUtility";
	protected final static String FSQL_MODULE_ID = "com.inductiveautomation.sqlbridge";
	protected final static String TRANSACTION_GROUP_PATH = "Site/Units";
	protected final static String TRANSACTION_GROUP = "transaction-groups"; // Was "group"
	// Property keys for the python map
	protected final static String KEY_DATASOURCE= "datasource";
	protected final static String KEY_NAME= "name";
	protected final static String KEY_PATTERN = "pattern";
	protected final static String KEY_PATH= "path";         // Transaction group path  
	protected final static String KEY_TABLE= "table";
	protected final static String KEY_TAGPATH = "tagpath";  // Root of tagpath
	protected final static String KEY_TSTAMP= "tstamp";
	protected final static String KEY_UNIT= "unit";
	
	protected final static String KEY_CONFIGURED_ITEMS = "CONFIGURED_ITEMS";
	protected final static String KEY_DRIVING_TAG_PATH = "DRIVING_TAG_PATH";
	protected final static String KEY_TARGET_NAME      = "TARGET_NAME";
	protected final static String KEY_TARGET_DATA_TYPE = "TARGET_DATA_TYPE";
	
	protected static List<String> exclusions;
	static {
		exclusions = new ArrayList<>();
		exclusions.add("EFT");
	}
	
	// Defaults
	protected final static String DEFAULT_TSTAMP= "tstamp";
	
	protected ILSLogger log;
	protected final Map<String, TransactionGroup> groupsByPath;              // Lookup by path name
	protected final Map<ProjectResourceId, TransactionGroup> groupsById;     // Lookup by resource Id
	protected Project project = null;

	/**
	 * Constructor: Each subclass must independently call listTransactionGroups()
	 *              after the project is established.
	 */
	public BaseTransactionGroupUtility() {
		this.groupsByPath = new HashMap<>();
		this.groupsById = new HashMap<>();
		this.log = LogMaker.getLogger(getClass().getPackage().getName());
	}

	protected abstract void addResource(String path,GroupConfig group);
	protected abstract void deleteResource(ProjectResourceId resourceId);
	protected abstract XMLDeserializer getDeserializer();
	protected void setProject(Project proj) {
		this.project = proj;
	}

	/**
	 * Create a list of transaction groups associated with a specified project.
	 * Calling this method has the side effect of populating the lookup maps.
	 * @return a list of group folder paths
	 */
	public PyList listTransactionGroups() {
		PyList list = new PyList();
		for (ProjectResource res : project.getResources()) {
			//log.infof("%s.listTransactionGroups: found project resource: %s.",CLSS,res.getResourceType().getTypeId());
			if( !res.isFolder() && TRANSACTION_GROUP.equalsIgnoreCase(res.getResourceType().getTypeId()) ) {
				String name = res.getResourceName();
				//log.infof("%s.listTransactionGroups: found transaction group: %s.",CLSS,name);
				ProjectResourceId resId = res.getResourceId();
				String path = resId.getFolderPath();
				TransactionGroup tg = new TransactionGroup(name,resId);
				groupsByPath.put(path, tg);
				groupsById.put(resId, tg);
				list.add(path);
			}
		}
		list.sort();
		return list;
	}
	
	/**
	 * Create a new transaction group based on the specified source, but modified 
	 * for a different processing unit. Save as a new project resource. Delete
	 * any existing resource of the new name.
	 * @param source path to the source transaction group.
	 * @param properties are desired options for the new transaction group 
	 */
	public void createTransactionGroupForUnit(String source,Map<String,String> map) {
		TransactionGroup master = groupsByPath.get(source);
		if( master!=null ) {
			Optional<ProjectResource> optional = project.getResource(master.getResourceId());
			ProjectResource pr = optional.get();
			String path = map.get(KEY_PATH);
			if( path==null) {
				log.warnf("%s.createTransactionGroupForUnit: destination required",CLSS);
				return;
			}
			String name = map.get(KEY_NAME);
			if( name==null) {
				log.warnf("%s.createTransactionGroupForUnit: name required",CLSS);
				return;
			}
			
			GroupConfig group = deserialize(pr);  // The is the transaction group
			if( group!=null ) {
				log.infof("%s.createTransactionGroupForUnit: Deserialized %s (now %s)",CLSS,group.getName(),path);
				group.setName(name);
				modifyPropertiesForTarget(group,map);
				deleteTransactionGroup(path);  // In case it exists
				addResource(path,group);
			}
			else {
				log.warnf("%s.createTransactionGroupForUnit: No configuration found for path %s",CLSS,source);
			}
		}
	}
	
	public void deleteTransactionGroup(String path) {
		ProjectResourceId groupId = resourceIdForPath(path);
		if( groupId!=null) deleteResource(groupId);
	}
	
	public GroupConfig deserialize(ProjectResource res) {
		XMLDeserializer deserializer = getDeserializer();
		try {
			byte[] bytes = res.getData();	
			DeserializationContext dc = deserializer.deserialize(bytes);
			List<Object> rootObjects = dc.getRootObjects();
			// We are only deserializing one resource, so there's only one root object.
			for( Object obj:rootObjects ) {
				if( obj instanceof GroupConfig ) {
					return (GroupConfig)obj;
				}
			}
		}
		catch(SerializationException se) {
			log.errorf("%s.deserialize: Exception reading %d (%s)",CLSS,res.getResourceId(),se.getLocalizedMessage());
		}
		return null;
	}
	
	public void modifyPropertiesForTarget(GroupConfig group,Map<String,String>map) {
		
		Map<String,MetaProperty> propertyMap = group.getProperties().getProperties();
		for(String key:propertyMap.keySet()) {
			MetaProperty prop = propertyMap.get(key);
			modifyPropertyForTarget(prop,map);
		}
	}	
	private void modifyConfiguredItemsForTarget(ItemConfig[] items,Map<String,String>map ) {
		for( ItemConfig item:items) {
			Map<String,MetaProperty> propertyMap = item.getProperties().getProperties();
			for(String key:propertyMap.keySet()) {
				MetaProperty prop = propertyMap.get(key);
				modifyPropertyForTarget(prop,map);
			}
		}
	}
	
	// We have seen DRIVING_TAG_PATH return a null name, and so protect for it and the rest.
	private void modifyPropertyForTarget(MetaProperty prop,Map<String,String>map ) {
		String key = prop.getName();
		//log.infof("%s.modifyPropertyForTarget: %s %s",CLSS,prop.getName(),prop.getValue().toString());
		if( CommonGroupProperties.CONFIGURED_ITEMS.getName()!=null && key.equals(CommonGroupProperties.CONFIGURED_ITEMS.getName())) {
			modifyConfiguredItemsForTarget((ItemConfig[])prop.getValue(),map);
		}
		else if( CommonGroupProperties.DATA_SOURCE.getName()!=null          && key.equals(CommonGroupProperties.DATA_SOURCE.getName())) {
			prop.setValue(map.get(KEY_DATASOURCE));
		}
		else if( CommonItemProperties.DRIVING_TAG_PATH.getName()!=null       && key.equals(CommonItemProperties.DRIVING_TAG_PATH.getName())) {
			prop.setValue(modifyTagPath(prop.getValue().toString(),map));
		}
		else if( CommonGroupProperties.EXECUTION_ENABLED.getName()!=null     && key.equals(CommonGroupProperties.EXECUTION_ENABLED.getName())) {}
		else if( CommonGroupProperties.GROUP_EXECUTION_FLAGS.getName()!=null && key.equals(CommonGroupProperties.GROUP_EXECUTION_FLAGS.getName())) {}
		else if( key.equals("NAME")) {
			prop.setValue(modifyTagPath(prop.getValue().toString(),map));
		} 
		else if( CommonGroupProperties.TABLE_NAME.getName()!=null && key.equals(CommonGroupProperties.TABLE_NAME.getName())) {
			prop.setValue(map.get(KEY_TABLE));
		}
		else if( CommonItemProperties.TARGET_DATA_TYPE.getName()!=null  && key.equals(CommonItemProperties.TARGET_DATA_TYPE.getName())) {}
		else if( CommonItemProperties.TARGET_NAME.getName()!=null       && key.equals(CommonItemProperties.TARGET_NAME.getName())) {}
		else if( CommonItemProperties.TARGET_TYPE.getName()!=null       && key.equals(CommonItemProperties.TARGET_TYPE.getName())) {}
		else if( CommonGroupProperties.TIMESTAMP_COLUMN.getName()!=null && key.equals(CommonGroupProperties.TIMESTAMP_COLUMN.getName())) {}
		else if( CommonGroupProperties.TRIGGER_INACTIVE_COMPARE.getName()!=null && key.equals(CommonGroupProperties.TRIGGER_INACTIVE_COMPARE.getName())) {}
		else if( CommonGroupProperties.TRIGGER_MODE.getName()!=null     && key.equals(CommonGroupProperties.TRIGGER_MODE.getName())) {}
		else if( CommonGroupProperties.TRIGGER_PATH.getName()!=null     && key.equals(CommonGroupProperties.TRIGGER_PATH.getName())) {
			prop.setValue(modifyTagPath(prop.getValue().toString(),map));
		}
		else if( CommonGroupProperties.UPDATE_RATE.getName()!=null      && key.equals(CommonGroupProperties.UPDATE_RATE.getName())) {}
		else if( CommonGroupProperties.UPDATE_UNITS.getName()!=null     && key.equals(CommonGroupProperties.UPDATE_UNITS.getName())) {}
		else {
			log.infof("%s.modifyPropertyForTarget: UNRECOGNIZED:  %s = %s",CLSS,prop.getName(),prop.getValue().toString());
		}
	}
	

	/**
	 * Given the path to a transaction group, return a map of its tag paths
	 * keyed by the control parameter name.
	 * @param path
	 * @return
	 */
	public Map<String,String> propertiesForGroup(String path) {
		Map<String,String> result = new HashMap<>();
		TransactionGroup tg = groupsByPath.get(path);
		if( tg!=null ) {
			Optional<ProjectResource> optional = project.getResource(tg.getResourceId());
			ProjectResource pr = optional.get();
			GroupConfig group = deserialize(pr);  // The is the transaction group
			if( group!=null ) {
				if( group.getProperties()!=null ) {
					Map<String,MetaProperty> props = group.getProperties().getProperties();
					ItemConfig[] items = (ItemConfig[])props.get(KEY_CONFIGURED_ITEMS).getValue();
					//log.infof("%s.propertiesForGroup: %d configured items",CLSS,items.length);
					for( ItemConfig item:items ) {
						Map<String,MetaProperty> iprops = item.getProperties().getProperties();
						if( iprops.get(KEY_TARGET_NAME)==null ) continue;
						String name = iprops.get(KEY_TARGET_NAME).getValue().toString();
						//log.infof("%s.propertiesForGroup: %s %s %s",CLSS,iprops.get(KEY_TARGET_NAME).getValue(),iprops.get(KEY_TARGET_DATA_TYPE).getValue(),iprops.get(KEY_DRIVING_TAG_PATH).getValue());
						if(exclusions.contains(name)) continue;
						if(!iprops.get(KEY_TARGET_DATA_TYPE).getValue().toString().equals("Float4") && !iprops.get(KEY_TARGET_DATA_TYPE).getValue().toString().equals("Float8")) continue;
						String tagpath = iprops.get(KEY_DRIVING_TAG_PATH).getValue().toString();
						result.put(name,tagpath);
						log.infof("%s.propertiesForGroup: using %s %s %s",CLSS,iprops.get(KEY_TARGET_NAME).getValue(),iprops.get(KEY_TARGET_DATA_TYPE).getValue(),iprops.get(KEY_DRIVING_TAG_PATH).getValue());
					}
				}
				else {
					log.warnf("%s.propertiesForGroup: group %s has no properties",CLSS,group.getName());
				}
			}
			else {
				log.warnf("%s.propertiesForGroup: No configuration found for path %s",CLSS,path);
			}
		}
		else {
			log.warnf("%s.propertiesForGroup: No group found for path %s",CLSS,path);
		}
		return result;
	}
	
	// Modify the original tag path to incorporate a new root pattern
	private String modifyTagPath(String inPath,Map<String,String> map) {
		String tagpath = inPath;
		// Remove the provider, if it exists
		if( tagpath.startsWith("/")) tagpath = tagpath.substring(1);
		int pos = tagpath.indexOf("]");
		if( pos>0 ) tagpath = tagpath.substring(pos+1);
		String pattern = map.get(KEY_PATTERN);
		String path = map.get(KEY_TAGPATH);
		String newPath = tagpath.replace(pattern, path);
		//log.infof("%s.modifyTagPath: %s -> %s",CLSS,inPath,newPath);
		return newPath;
	}
	
	/**
	 * Given the resource path, return the corresponding resource Id.
	 * If there is no match, return NULL.
	 * @param path in the resource nav tree
	 * @return resourceId
	 */
	private ProjectResourceId resourceIdForPath(String path) {
		ProjectResourceId result = null;
		TransactionGroup group = groupsByPath.get(path);
		if( group!=null) result = group.getResourceId();
		return result;
	}

	private class TransactionGroup {
		private final String name;
		private final ProjectResourceId resourceId;

		public TransactionGroup(String nam,ProjectResourceId resId) {
			this.name = nam;
			this.resourceId = resId;
		}

		public String getName() {return name;}
		public ProjectResourceId getResourceId() { return resourceId; }
	}
}
