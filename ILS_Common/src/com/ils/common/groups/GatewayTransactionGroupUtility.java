package com.ils.common.groups;

import java.util.List;

import com.inductiveautomation.ignition.common.model.ApplicationScope;
import com.inductiveautomation.ignition.common.project.ProjectResource;
import com.inductiveautomation.ignition.common.project.ProjectVersion;
import com.inductiveautomation.ignition.common.xmlserialization.SerializationException;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.DeserializationContext;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.XMLDeserializer;
import com.inductiveautomation.ignition.gateway.SRContext;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

/**
 * This class provides utility methods dealing with transaction groups.
 * These methods must all be invoked in Gateway scope.
 */
public class GatewayTransactionGroupUtility extends BaseTransactionGroupUtility {
	private final GatewayContext context;
	
	public GatewayTransactionGroupUtility(long projectId) {
		context = SRContext.get();
		setProject(context.getProjectManager().getProject(projectId, ApplicationScope.GATEWAY, ProjectVersion.Staging));
	}
	
	public void deserialize(ProjectResource res) {
		XMLDeserializer deserializer = context.createDeserializer();
		try {
			byte[] bytes = res.getData();	
			DeserializationContext dc = deserializer.deserialize(bytes);
			List<Object> rootObjects = dc.getRootObjects();
			for( Object obj:rootObjects ) {
				log.infof("deserialize: got %s",obj.getClass().getCanonicalName());
			}
			//IlsSfcCommonUtils.printResource(data);					
			//GZIPInputStream xmlInput = new GZIPInputStream(new ByteArrayInputStream(chartResourceData));

			//log.debugf("loadModels: found resource %s (%d)",path,res.getResourceId());
		}
		catch(SerializationException se) {
			log.errorf("deserialize: Exception reading %d (%s)",res.getResourceId(),se.getLocalizedMessage());
		}
	}
}
