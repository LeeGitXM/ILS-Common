/**
 *   (c) 2016  ILS Automation. All rights reserved.
 *  
 */
package com.ils.common.designer;

import java.util.Enumeration;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.designer.IgnitionDesigner;
import com.inductiveautomation.ignition.designer.WorkspaceManager;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.navtree.model.AbstractNavTreeNode;
import com.inductiveautomation.ignition.designer.navtree.model.ProjectBrowserRoot;


/**
 *  The handler is used to enable selected workspaces and show NavTree nodes
 *  by name (path). This works only in the Designer scope.
 */
public class WorkspaceHandler {
	private final LoggerEx log;
	private final DesignerContext context;
	
	// "well-known" workspace keys
	public static final String DIAGRAM_WORKSPACE_KEY = "BlockDiagramWorkspace";
	public static final String SFC_WORKSPACE_NAME = "sfc-workspace";
	public static final String VISION_WORKSPACE_NAME = "windows";
	
	// Root node names that indicate correct branch
	public static final String ROOT = "ROOT";   // For DIAGRAM WORKSPACE

	/**
	 * The handler
	 */
	public WorkspaceHandler(DesignerContext ctx) {
		this.log = LogUtil.getLogger(getClass().getPackage().getName());
		this.context = ctx;
	}
	/**
	 * Show a named workspace in the designer. This should be executed in a
	 * background UI thread.
	 * 
	 * @param key standard key for the workspace
	 */
	public void showWorkspace(String key) {
		IgnitionDesigner designerGUI = (IgnitionDesigner)context.getFrame();
		WorkspaceManager wmgr = designerGUI.getWorkspace();
		/*
		 * Here's how to get a list of all the registered workspaces
		int count = wmgr.getWorkspaceCount();
		for(int index=0;index<count;index++) {
			ResourceWorkspace wksp = wmgr.getWorkspace(index);
			log.infof("showWorkspace: key = %s", wksp.getKey());
		}
		*/
		wmgr.setSelectedWorkspace(key);
	}
	/**
	 * Select a node in the NavTree for the global project. This action opens
	 * its associated panel. THis is most often used with an SFC chart.
	 * @param path to the NavTree node associated with the desired tab. 
	 */
	public void showGlobalTab(String path) {
		ProjectBrowserRoot project = context.getProjectBrowserRoot();
		AbstractNavTreeNode root = null;
		AbstractNavTreeNode node = null;
		// Get the "ROOT" node before we traverse the hierarchy.
		root = project.findChild("Project");
		if( root!=null ) node = findChildInTree(root,"ROOT");

		// The specified path is slash-delimited.
		String[] pathArray = path.split("/");

		int index = 0;
		while( index<pathArray.length ) {
			node = findChildInTree(node,pathArray[index]);
			if( node!=null ) {
				node.expand();
				try {
					Thread.sleep(100); 
				}
				catch(InterruptedException ignore) {}
			}
			else{
				log.warnf("receiveNotification: Unable to find node (%s) on browser path",pathArray[index]);
				break;
			}
			index++;
		}

		if( node!=null ) {
			node.onDoubleClick();    // Opens the diagram
		}
		else {
			log.warnf("receiveNotification: Unable to open browser path (%s)",path);
		}
	}
	/**
	 * Select a node in the NavTree for a project. This action opens
	 * its associated panel.
	 * 
	 * @param path to the NavTree node associated with the desired tab. 
	 */
	public void showProjectTab(String path,String rootName) {
		ProjectBrowserRoot project = context.getProjectBrowserRoot();
		AbstractNavTreeNode root = null;
		AbstractNavTreeNode node = null;
		// Get the "ROOT" node before we traverse the hierarchy.
		root = project.findChild("Project");
		if( root!=null ) node = findChildInTree(root,"ROOT");

		// The specified path is slash-delimited.
		String[] pathArray = path.split("/");

		int index = 0;
		while( index<pathArray.length ) {
			node = findChildInTree(node,pathArray[index]);
			if( node!=null ) {
				node.expand();
				try {
					Thread.sleep(100); 
				}
				catch(InterruptedException ignore) {}
			}
			else{
				log.warnf("receiveNotification: Unable to find node (%s) on browser path",pathArray[index]);
				break;
			}
			index++;
		}

		if( node!=null ) {
			node.onDoubleClick();    // Opens the diagram
		}
		else {
			log.warnf("receiveNotification: Unable to open browser path (%s)",path);
		}
	}



	/**
	 * We have not been successful with the findChild method .. so we've taken it on ourselves.
	 * @param root
	 * @param name
	 * @return
	 */
	private AbstractNavTreeNode findChildInTree(AbstractNavTreeNode root,String name) {
		AbstractNavTreeNode match = null;
		if( root!=null ) {
			@SuppressWarnings("unchecked")
			Enumeration<AbstractNavTreeNode> nodeWalker = root.children();
			AbstractNavTreeNode child = null;
			
			while( nodeWalker.hasMoreElements() ) {
				child = nodeWalker.nextElement();
				log.tracef("findChildInTree: testing %s vs %s",name,child.getName());
				if( child.getName().equalsIgnoreCase(name)) {
					match = child;
					break;
				}
			}
		}
		return match;
	}
	/**
	 * Search the menu tree for a particular item.
	 * @param bar
	 * @param path
	 * @return
	 */
	private JMenuItem menuItemForPath(JMenuBar bar,String[] path) {
		int index = 0;
		int pathSegments = path.length;
		JMenuItem item = null;
		while( index<pathSegments ) {
			String segmentName = path[index];
			
			if( index==0 ) {
				int count = bar.getMenuCount();
				JMenu menu = null;
				while(index<count) {
					if(segmentName.equals(bar.getMenu(index).getText()) ) {
						item = bar.getMenu(index);
						break;
					}
					index++;
				}
			}
			else {
				MenuElement[] subElements = item.getSubElements();
				int count = subElements.length;
				int jndex = 0;
				while(jndex<count) {
					MenuElement me = subElements[jndex];
					if( me instanceof JMenuItem ) {
						JMenuItem mi = (JMenuItem) me;
						if(segmentName.equals(mi.getText()) ) {
							item = mi;
							break;
						}
					}
					jndex++;
				}
			}
			if( item==null) break;
			index++;
		}
		return item;
	}
}
