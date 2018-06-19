package com.ils.common.groups;

import com.inductiveautomation.factorysql.FactorySQLGatewayHook;
import com.inductiveautomation.factorysql.common.config.GroupConfig;
import com.inductiveautomation.ignition.common.model.ApplicationScope;
import com.inductiveautomation.ignition.common.project.ProjectVersion;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.XMLDeserializer;
import com.inductiveautomation.ignition.gateway.SRContext;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

/**
 * This class provides utility methods dealing with transaction groups.
 * These methods must all be invoked in Gateway scope.
 */
public class GatewayTransactionGroupUtility extends BaseTransactionGroupUtility {
	private final GatewayContext context;
	private final FactorySQLGatewayHook hook;
	
	public GatewayTransactionGroupUtility(long projectId) {
		context = SRContext.get();
		setProject(context.getProjectManager().getProject(projectId, ApplicationScope.GATEWAY, ProjectVersion.Staging));
		hook = (FactorySQLGatewayHook)context.getModule(FSQL_MODULE_ID);
		log.infof("GatewayTransactionGroupUtility.constructor: ExecutionManager is %s",hook.getGroupExecutionManager().getClass().getCanonicalName());
		listTransactionGroups(); // Populate the lookup maps
	}
	
	protected void addResource(String path,GroupConfig group) {}  // TODO
	protected void deleteResource(long resourceId) {}  // TODO:
	protected XMLDeserializer getDeserializer() { return this.context.createDeserializer(); }
}
