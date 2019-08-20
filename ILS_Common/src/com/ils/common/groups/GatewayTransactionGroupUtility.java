package com.ils.common.groups;


import java.util.Optional;

import com.inductiveautomation.factorysql.FactorySQLGatewayHook;
import com.inductiveautomation.factorysql.common.config.GroupConfig;
import com.inductiveautomation.ignition.common.model.ApplicationScope;
import com.inductiveautomation.ignition.common.project.RuntimeProject;
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceId;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.XMLDeserializer;
import com.inductiveautomation.ignition.gateway.IgnitionGateway;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

/**
 * This class provides utility methods dealing with transaction groups.
 * These methods must all be invoked in Gateway scope.
 */
public class GatewayTransactionGroupUtility extends BaseTransactionGroupUtility {
	private final GatewayContext context;
	private final FactorySQLGatewayHook hook;
	
	/**
	 * Constructor:
	 * @param name project name
	 */
	public GatewayTransactionGroupUtility(String name) {
		context = IgnitionGateway.get();
		Optional<RuntimeProject> optional = context.getProjectManager().getProject(name, ApplicationScope.GATEWAY);
		RuntimeProject project = optional.get();
		setProject(project);
		hook = (FactorySQLGatewayHook)context.getModule(FSQL_MODULE_ID);
		log.infof("GatewayTransactionGroupUtility.constructor: ExecutionManager is %s",hook.getGroupExecutionManager().getClass().getCanonicalName());
		listTransactionGroups(); // Populate the lookup maps
	}
	
	protected void addResource(String path,GroupConfig group) {}  // TODO
	protected void deleteResource(ProjectResourceId resourceId) {}  // TODO:
	protected XMLDeserializer getDeserializer() { return this.context.createDeserializer(); }
}
