/**
 *   (c) 2021  ILS Automation. All rights reserved. 
 */
package com.ils.module.gateway.meta;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Application;
import org.apache.wicket.model.Model;

import com.ils.common.ILSProperties;
import com.ils.logging.common.CommonProperties;
import com.ils.module.gateway.ILSGatewayHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.web.components.RecordEditForm;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;
/**
 * Note: A RecordEditForm is a ConfigPanel.
 */
public class HelpParameterEditPage extends RecordEditForm {
	private static final long serialVersionUID = 9167269039342984188L;
	
	// This gets added as the panel for the help category
	public static IConfigTab MENU_ENTRY = com.inductiveautomation.ignition.gateway.web.models.DefaultConfigTab.builder()
			.category(ILSGatewayHook.helpCategory)
			.name("settings")
			.i18n("browser.path")
			.page(HelpParameterEditPage.class)
			.terms(new String[] {"path"})
			.build();

	public HelpParameterEditPage() {
		super(null, null, Model.of("Browser Execution Path (Windows only)"),
				((GatewayContext) Application.get()).getPersistenceInterface()
						.find(HelpRecord.META, 0L));
	}

	@Override
	public String[] getMenuPath() {
		return new String[] { ILSProperties.ROOT,CommonProperties.HELP_CONFIGURATION };
	}
	@Override
	public Pair<String,String> getMenuLocation() {
		return MENU_ENTRY.getMenuLocation();
	}
	
	

}
