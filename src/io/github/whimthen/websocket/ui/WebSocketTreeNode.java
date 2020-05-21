package io.github.whimthen.websocket.ui;

import javax.swing.tree.DefaultMutableTreeNode;

public class WebSocketTreeNode extends DefaultMutableTreeNode {

	protected boolean isChecked = true;
	private boolean isEnabled = true;

	public WebSocketTreeNode() {
	}

	public WebSocketTreeNode(Object userObject) {
		super(userObject);
	}

	public boolean isChecked() {
		return isChecked;
	}

	public WebSocketTreeNode setChecked(boolean checked) {
		isChecked = checked;
		return this;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public WebSocketTreeNode setEnabled(boolean enabled) {
		isEnabled = enabled;
		return this;
	}

}
