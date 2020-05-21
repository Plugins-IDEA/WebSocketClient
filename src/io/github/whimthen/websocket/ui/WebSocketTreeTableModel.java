package io.github.whimthen.websocket.ui;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class WebSocketTreeTableModel extends DefaultTreeModel {

	public WebSocketTreeTableModel(TreeNode root) {
		super(root, true);
	}

}
