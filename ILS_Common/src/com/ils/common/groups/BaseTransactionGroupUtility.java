/**
 *   (c) 2016-2018  ILS Automation. All rights reserved.
 *   @See: sql-bridge-common-api.jar, sql-bridge-designer-api.jar
 */

package com.ils.common.groups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyString;

import com.inductiveautomation.factorysql.common.config.CommonGroupProperties;
import com.inductiveautomation.factorysql.common.config.CommonItemProperties;
import com.inductiveautomation.factorysql.common.config.GroupConfig;
import com.inductiveautomation.factorysql.common.config.ItemConfig;
import com.inductiveautomation.ignition.common.metaproperties.MetaProperty;
import com.inductiveautomation.ignition.common.project.Project;
import com.inductiveautomation.ignition.common.project.ProjectResource;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.common.xmlserialization.SerializationException;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.DeserializationContext;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.XMLDeserializer;

/**
 * This class presents several static utility methods dealing with transaction groups.
 * The methods that are abstracted can be overwritten for either gateway or designer scopes.
 */
public abstract class BaseTransactionGroupUtility {
	private final static String CLSS = "BaseTransactionGroupUtility";
	protected final static String FSQL_MODULE_ID = "fsql";
	protected final static String TRANSACTION_GROUP = "group";
	// Property keys for the python map
	protected final static String KEY_DATASOURCE= "datasource";
	protected final static String KEY_ORIGINAL= "original";
	protected final static String KEY_PATH= "path";    
	protected final static String KEY_TABLE= "table";
	protected final static String KEY_TSTAMP= "tstamp";
	protected final static String KEY_UNIT= "unit";
	
	// Defaults
	protected final static String DEFAULT_TSTAMP= "tstamp";
	
	protected LoggerEx log;
	protected final Map<String, TransactionGroup> groupsByPath; // Lookup by path name
	protected final Map<Long, TransactionGroup> groupsById;     // Lookup by resource Id
	protected Project project = null;

	public BaseTransactionGroupUtility() {
		this.groupsByPath = new HashMap<>();
		this.groupsById = new HashMap<>();
		this.log = LogUtil.getLogger(getClass().getPackage().getName());
	}

	protected abstract void addResource(String path,GroupConfig group);
	protected abstract void deleteResource(long resourceId);
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
			if (res.getResourceType().equals(TRANSACTION_GROUP)) {
				String name = res.getName();
				long resId = res.getResourceId();
				String path = project.getFolderPath(resId);
				TransactionGroup tg = new TransactionGroup(name,resId,path);
				groupsByPath.put(path, tg);
				groupsById.put(new Long(resId), tg);
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
		listTransactionGroups(); // Populate the lookup maps
		TransactionGroup master = groupsByPath.get(source);
		if( master!=null ) {
			ProjectResource pr = project.getResource(master.getResourceId());
			String path = map.get(KEY_PATH);
			if( path==null) {
				log.warnf("%s.createTransactionGroupForUnit: destination required",CLSS);
				return;
			}
			GroupConfig group = deserialize(pr);  // The is the transaction group
			if( group!=null ) {
				log.infof("deserializing: got %s (now %s)",group.getName(),path);
				group.setName(path);
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
		Long groupId = resourceIdForPath(path);
		if( groupId!=null) deleteResource(groupId.longValue());
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
			log.errorf("deserialize: Exception reading %d (%s)",res.getResourceId(),se.getLocalizedMessage());
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
	
	private void modifyPropertyForTarget(MetaProperty prop,Map<String,String>map ) {
		String key = prop.getName();
		String oldName = map.get(KEY_ORIGINAL);
		String newName = map.get(KEY_UNIT);
		if( key.equals(CommonGroupProperties.CONFIGURED_ITEMS.getName())) {
			modifyConfiguredItemsForTarget((ItemConfig[])prop.getValue(),map);
		}
		else if( key.equals(CommonGroupProperties.DATA_SOURCE.getName())) {
			prop.setValue(map.get(KEY_DATASOURCE));
		}
		else if( key.equals(CommonItemProperties.DRIVING_TAG_PATH.getName())) {
			prop.setValue(prop.getValue().toString().replaceAll(oldName, newName));
		}
		else if( key.equals(CommonGroupProperties.EXECUTION_ENABLED.getName())) {}
		else if( key.equals(CommonGroupProperties.GROUP_EXECUTION_FLAGS.getName())) {}
		else if( key.equals("NAME")) {}  // Already handled
		else if( key.equals(CommonGroupProperties.TABLE_NAME.getName())) {
			prop.setValue(map.get(KEY_TABLE));
		}
		else if( key.equals(CommonItemProperties.TARGET_DATA_TYPE.getName())) {}
		else if( key.equals(CommonItemProperties.TARGET_NAME.getName())) {}
		else if( key.equals(CommonItemProperties.TARGET_TYPE.getName())) {}
		else if( key.equals(CommonGroupProperties.TIMESTAMP_COLUMN.getName())) {}
		else if( key.equals(CommonGroupProperties.TRIGGER_INACTIVE_COMPARE.getName())) {}
		else if( key.equals(CommonGroupProperties.TRIGGER_MODE.getName())) {}
		else if( key.equals(CommonGroupProperties.TRIGGER_PATH.getName())) {
			prop.setValue(prop.getValue().toString().replaceAll(oldName, newName));
		}
		else if( key.equals(CommonGroupProperties.UPDATE_RATE.getName())) {}
		else if( key.equals(CommonGroupProperties.UPDATE_UNITS.getName())) {}
		else {
			log.infof("UNRECOGNIZED:  %s = %s",prop.getName(),prop.getValue().toString());
		}
	}
	

	
	/**
	 * Given the resource path, return the corresponding resource Id.
	 * If there is no match, return NULL.
	 * @param path in the resource nav tree
	 * @return resourceId
	 */
	private Long resourceIdForPath(String path) {
		Long result = null;
		listTransactionGroups(); // Populate the lookup maps
		TransactionGroup group = groupsByPath.get(path);
		if( group!=null) result = new Long(group.getResourceId());
		return result;
	}

	private class TransactionGroup {
		private final String name;
		private final long resourceId;
		private final String path;

		public TransactionGroup(String nam,long resId,String folderPath) {
			this.name = nam;
			this.resourceId = resId;
			this.path = folderPath;
		}

		public String getName() {return name;}
		public long getResourceId() { return resourceId; }
	}
}
