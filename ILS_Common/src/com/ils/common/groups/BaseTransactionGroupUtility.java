package com.ils.common.groups;

import java.util.HashMap;
import java.util.Map;

import org.python.core.PyList;

import com.inductiveautomation.ignition.common.model.BaseContext;
import com.inductiveautomation.ignition.common.project.Project;
import com.inductiveautomation.ignition.common.project.ProjectResource;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 * This class presents several static utility methods dealing with transaction
 * groups.These methods must all be invoked in Gateway scope.
 */
public abstract class BaseTransactionGroupUtility {
	protected final static String TRANSACTION_GROUP = "group";
	protected LoggerEx log;
	protected final Map<String, TransactionGroup> groupsByPath; // Lookup by path name
	protected final Map<Long, TransactionGroup> groupsById;     // Lookup by resource Id
	protected Project project = null;

	public BaseTransactionGroupUtility() {
		this.groupsByPath = new HashMap<>();
		this.groupsById = new HashMap<>();
		this.log = LogUtil.getLogger(getClass().getPackage().getName());
	}

	protected abstract void deserialize(ProjectResource pr);
	
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
		return list;
	}
	

	
	/**
	 * Create a new transaction group based on the specified source, but modified 
	 * for a different processing unit.
	 * @param source path to the source transaction group.
	 * @param unit name of the processing unit. 
	 */
	public void createTransactionGroupForUnit(String source,String unit) {
		listTransactionGroups(); // Populate the lookup maps
		TransactionGroup master = groupsByPath.get(source);
		if( master!=null ) {
			ProjectResource pr = project.getResource(master.getResourceId());
			deserialize(pr);
			
		}
	}
	
	public void deleteTransactionGroup(String path) {
		
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
