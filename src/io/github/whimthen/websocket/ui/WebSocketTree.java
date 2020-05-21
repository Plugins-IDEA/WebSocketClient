package io.github.whimthen.websocket.ui;

import com.intellij.ui.TreeSpeedSearch;

public class WebSocketTree extends WebSocketTreeBase {

	public WebSocketTree() {}

	public WebSocketTree(WebSocketTreeCellRenderer cellRenderer, WebSocketTreeNode root) {
		super(cellRenderer, root);
		this.installSpeedSearch();
	}

	public WebSocketTree(WebSocketTreeCellRenderer cellRenderer, WebSocketTreeNode root, WebSocketTreeBase.CheckPolicy checkPolicy) {
		super(cellRenderer, root, checkPolicy);
		this.installSpeedSearch();
	}

	protected void installSpeedSearch() {
		new TreeSpeedSearch(this);
	}

	public abstract static class WebSocketTreeCellRenderer extends WebSocketTreeCellRendererBase {
		protected WebSocketTreeCellRenderer() {
		}

		protected WebSocketTreeCellRenderer(boolean opaque) {
			super(opaque);
		}

		protected WebSocketTreeCellRenderer(boolean opaque, boolean usePartialStatusForParentNodes) {
			super(opaque, usePartialStatusForParentNodes);
		}
	}

}
