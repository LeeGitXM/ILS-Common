package com.ils.common.groups;

import com.inductiveautomation.factorypmi.application.FPMIApp;
import com.inductiveautomation.factorysql.common.config.GroupConfig;
import com.inductiveautomation.factorysql.designer.FSQLDesignerModuleHook;
import com.inductiveautomation.ignition.client.model.ClientContext;
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceId;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.XMLDeserializer;

/**
 * This class provides utility methods dealing with transaction groups.
 * These methods must all be invoked in Designer scope.
 */
public class DesignerTransactionGroupUtility extends BaseTransactionGroupUtility {
	private final static String CLSS = "DesignerTransactionGroupUtility";
	private final ClientContext context;
	private FSQLDesignerModuleHook hook;
	
	public DesignerTransactionGroupUtility() {
		@SuppressWarnings("deprecation")
		FPMIApp app = FPMIApp.getInstance();
		context = app.getAdapterContext();
		hook = (FSQLDesignerModuleHook)context.getModule(FSQL_MODULE_ID);
		setProject(context.getProject());
		listTransactionGroups(); // Populate the lookup maps
	}
	/**
	 * The hook's addResource wants the enclosing folder as the path.
	 * We don't know the new resourceId.
	 * @param path full path to the resource.
	 * @param gc the group
	 */
	public void addResource(String path,GroupConfig gc) {
		String folder = "";
		int pos = path.lastIndexOf("/");
		if( pos>0 ) folder = path.substring(0,pos);
		log.infof("%s.addResource: path: %s name:%s.",CLSS,folder,gc.getName());
		hook.addResource(path,gc.getName(),gc);
		listTransactionGroups(); // Re-populate the lookup maps
	}
	
	public XMLDeserializer getDeserializer() { return this.context.createDeserializer(); }
	
	public void deleteResource(ProjectResourceId resourceId) { 
		hook.deleteResource(resourceId);
	}
}
