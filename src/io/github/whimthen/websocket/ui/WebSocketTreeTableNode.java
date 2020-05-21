package io.github.whimthen.websocket.ui;

import com.intellij.dupLocator.resultUI.BasicTreeNode;
import com.intellij.dupLocator.resultUI.DuplicatesModel;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class WebSocketTreeTableNode extends DefaultMutableTreeNode {

	public WebSocketTreeTableNode(TreeNode parent) {
		super(parent, true);
	}

}
