package com.ils.common.groups;

import java.util.List;
import java.util.Map;

import com.inductiveautomation.factorypmi.application.FPMIApp;
import com.inductiveautomation.factorysql.common.config.GroupConfig;
import com.inductiveautomation.factorysql.designer.FSQLDesignerModuleHook;
import com.inductiveautomation.factorysql.designer.model.controllers.FSQLProjectController;
import com.inductiveautomation.factorysql.designer.model.controllers.GroupConfigController;
import com.inductiveautomation.factorysql.designer.model.controllers.RootGroupController;
import com.inductiveautomation.ignition.client.model.ClientContext;
import com.inductiveautomation.ignition.common.metaproperties.MetaProperty;
import com.inductiveautomation.ignition.common.project.ProjectResource;
import com.inductiveautomation.ignition.common.xmlserialization.SerializationException;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.DeserializationContext;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.XMLDeserializer;

/**
 * This class provides utility methods dealing with transaction groups.
 * These methods must all be invoked in Client/Designer scope.
 */
public class DesignerTransactionGroupUtility extends BaseTransactionGroupUtility
{
	private final static String FSQL_MODULE_ID = "fsql";
	private final ClientContext context;
	private final FSQLDesignerModuleHook hook;
	
	public DesignerTransactionGroupUtility() {
		@SuppressWarnings("deprecation")
		FPMIApp app = FPMIApp.getInstance();
		context = app.getAdapterContext();
		hook = (FSQLDesignerModuleHook)context.getModule(FSQL_MODULE_ID);
		RootGroupController controller = (RootGroupController)hook.getGroupController();
		FSQLProjectController pc = controller.getProjectController();
		//pc.deleteResource, addResource, commitResource()
		GroupConfigController gcc = pc.getGroupController();

		log.infof("constructor: controller %s", hook.getGroupController().getClass().getCanonicalName());
		log.infof("constructor: controller %s", hook.getLiveValueManager().getClass().getCanonicalName());
		setProject(context.getProject());
	}
	public void deserialize(ProjectResource res) {
		XMLDeserializer deserializer = context.createDeserializer();
		try {
			byte[] bytes = res.getData();	
			DeserializationContext dc = deserializer.deserialize(bytes);
			List<Object> rootObjects = dc.getRootObjects();
			log.infof("deserialize: config= %s",hook.getConfigObjectForResourceId(res.getResourceId()).getClass().getCanonicalName());
			//FSQLDesignerModuleHook.DesignTimeGroupConfig dtgc = hook.getConfigObjectForResourceId(res.getResourceId());
			for( Object obj:rootObjects ) {
				if( obj instanceof GroupConfig ) {
					GroupConfig config = (GroupConfig)obj;
					log.infof("deserialize: got %s",config.getName());

					// config.setPath(); new group = GroupTypeRegistry.getInstance().createInstance(Long.valueOf(this.project.getId()), Long.valueOf(resource.getResourceId()), config);
					Map<String,MetaProperty> propertyMap = config.getProperties().getProperties();
					for(String key:propertyMap.keySet()) {
						MetaProperty prop = propertyMap.get(key);
						log.infof(" %s = %s",key,prop.getValue().toString());
					}
				}
			}
			//IlsSfcCommonUtils.printResource(data);					
			//GZIPInputStream xmlInput = new GZIPInputStream(new ByteArrayInputStream(chartResourceData));

			//log.debugf("loadModels: found resource %s (%d)",path,res.getResourceId());
		}
		catch(SerializationException se) {
			log.errorf("deserialize: Exception reading %d (%s)",res.getResourceId(),se.getLocalizedMessage());
		}
		
	}

	/*
	public void run() {
		// Now we need to serialize the entire chart.
		long resourceId = chartInfo.getResourceId();
		ByteArrayOutputStream out = new ByteArrayOutputStream(xml.length());
		try {
			GZIPOutputStream gzip = new GZIPOutputStream(out);
			gzip.write(xml.getBytes());
			gzip.close();
			byte[] bytes = out.toByteArray();
			if( context.requestLock(resourceId) ) {
				try {
					ProjectResource resource = context.getGlobalProject().getProject().getResource(resourceId);
					if( resource!=null ) {
						resource.setData(bytes);
						context.updateLock(resourceId);
						
						// Now save the resource as we have edited it.
						Project diff = context.getGlobalProject().getProject().getEmptyCopy();
						DesignerProjectContext globalContext = context.getGlobalProject();
						Project global = globalContext.getProject();
						ProjectResource res = global.getResource(resourceId);
						diff.putResource(res, false);    // Mark as clean
						global.applyDiff(diff,false);
						global.clearAllFlags();          // Don't know what this does ...
						try {
							DTGatewayInterface.getInstance().saveProject(IgnitionDesigner.getFrame(), global, true, "Committing ...");  // Publish
							DTGatewayInterface.getInstance().publishGlobalProject(IgnitionDesigner.getFrame());
						}
						catch(GatewayException ge) {
							log.errorf("%s.commitEdit: Unable to save project update (%s)",CLSS,ge.getLocalizedMessage());
						}		
					}
					else {
						log.errorf("%s.commitEdit: No resource (id=%d)",CLSS,resourceId); 
					}
				}
				finally {
					context.releaseLock(resourceId);
				}
			}
			else {
				log.errorf("%s.commitEdit: Unable to fetch lock for resource (%d)",CLSS,resourceId); 
			}
		}
		catch(IOException ioe) {
			log.errorf("%s.commitEdit: Unable to serialize element (%s)",CLSS,ioe.getLocalizedMessage()); 
		}
	}
	*/
}
