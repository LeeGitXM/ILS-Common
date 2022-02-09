package com.ils.common.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import com.inductiveautomation.ignition.client.tags.tree.TagRenderer;
import com.inductiveautomation.ignition.designer.model.DesignerContext;
import com.inductiveautomation.ignition.designer.tags.tree.TagTreeModelImpl;

import net.miginfocom.swing.MigLayout;

/**
 * Display a tag browser in tree form. Clients may subscribe
 * as selection listener. This is only valid in Designer scope.
 * @See BLT TagSelectorPanel
 */
class TagTreeSelectionPanel extends JPanel {
	private static final long serialVersionUID = 387758126934539795L;
	private final DesignerContext context;
	private final JTree tagTree;
	private final TagRenderer tagRenderer;
	private final TreeSelectionModel tagTreeSelectionModel;
	public static final Dimension TREE_SIZE = new Dimension(600,500);
	
	
	// The constructor
	public TagTreeSelectionPanel(DesignerContext ctx) {
		super(new BorderLayout(20, 30));
		this.context = ctx;
		
		JPanel mainPanel = new JPanel(new MigLayout("", "[right]"));
		
		this.tagRenderer = new TagRenderer();
		//setLayout(new BorderLayout());
		tagTree = new JTree();
		tagTree.setOpaque(true);
		tagTree.setCellRenderer(tagRenderer);
		TagTreeModelImpl ttmodel = new TagTreeModelImpl(context);
		ttmodel.reload();
		tagTree.setModel(ttmodel);
		tagTreeSelectionModel = tagTree.getSelectionModel();
		tagTreeSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tagTree.setBackground(getBackground());
		tagRenderer.setBackground(Color.cyan);
		JScrollPane treePane = new JScrollPane(tagTree);
		treePane.setPreferredSize(TREE_SIZE);
		mainPanel.add(treePane,BorderLayout.CENTER);
		add(mainPanel,BorderLayout.CENTER);
	}
	public JTree getTagTree() { return this.tagTree; }
	public void addTreeSelectionListener(TreeSelectionListener listener) { tagTree.addTreeSelectionListener(listener); }
	public void removeTreeSelectionListener(TreeSelectionListener listener) { tagTree.removeTreeSelectionListener(listener); }
}
