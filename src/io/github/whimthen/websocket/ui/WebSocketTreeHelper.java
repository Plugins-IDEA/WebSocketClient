package io.github.whimthen.websocket.ui;

import com.intellij.openapi.util.Key;
import com.intellij.ui.CheckedTreeNode;
import com.intellij.ui.ClickListener;
import com.intellij.ui.ComponentUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.speedSearch.SpeedSearchSupply;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ArrayUtil;
import com.intellij.util.EventDispatcher;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;

public class WebSocketTreeHelper {

	private static final Key<Runnable>                         TREE_LISTENERS_REMOVER = Key.create("TREE_LISTENERS_REMOVER");
	public static final  WebSocketTreeBase.CheckPolicy         DEFAULT_POLICY         = new WebSocketTreeBase.CheckPolicy(true, true, false, true);
	private final        WebSocketTreeBase.CheckPolicy         myCheckPolicy;
	private final        EventDispatcher<WebSocketTreeListener> myEventDispatcher;

	public WebSocketTreeHelper(@NotNull WebSocketTreeBase.CheckPolicy checkPolicy, @NotNull EventDispatcher<WebSocketTreeListener> dispatcher) {
		this.myCheckPolicy = checkPolicy;
		this.myEventDispatcher = dispatcher;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] getCheckedNodes(Class<? extends T> nodeType, Tree.NodeFilter<? super T> filter, TreeModel model) {
		final ArrayList<T> nodes = new ArrayList<>();
		Object             root  = model.getRoot();
		if (!(root instanceof WebSocketTreeNode)) {
			throw new IllegalStateException("The root must be instance of the " + CheckedTreeNode.class.getName() + ": " + root.getClass().getName());
		} else {
			((new Object() {
				public void collect(WebSocketTreeNode node) {
					if (node.isLeaf()) {
						Object userObject = node.getUserObject();
						if (node.isChecked() && userObject != null && nodeType.isAssignableFrom(userObject.getClass())) {
							if (filter != null && !filter.accept((T) userObject)) {
								return;
							}
							nodes.add((T) userObject);
						}
					} else {
						for(int i = 0; i < node.getChildCount(); ++i) {
							TreeNode child = node.getChildAt(i);
							if (child instanceof WebSocketTreeNode) {
								this.collect((WebSocketTreeNode) child);
							}
						}
					}
				}
			})).collect((WebSocketTreeNode) root);
			T[] result = ArrayUtil.newArray(nodeType, nodes.size());
			nodes.toArray(result);
			return result;
		}
	}

	public void initTree(@NotNull Tree tree, JComponent mainComponent, WebSocketTreeBase.WebSocketTreeCellRendererBase cellRenderer) {
		removeTreeListeners(mainComponent);
		tree.setCellRenderer(cellRenderer);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		TreeUtil.installActions(tree);
		KeyListener   keyListener   = this.setupKeyListener(tree, mainComponent);
		ClickListener clickListener = this.setupMouseListener(tree, mainComponent, cellRenderer);
		ComponentUtil.putClientProperty(mainComponent, TREE_LISTENERS_REMOVER, () -> {
			mainComponent.removeKeyListener(keyListener);
			clickListener.uninstall(mainComponent);
		});
	}

	private ClickListener setupMouseListener(Tree tree, JComponent mainComponent, WebSocketTreeBase.WebSocketTreeCellRendererBase cellRenderer) {
		ClickListener listener = new ClickListener() {
			@Override
			public boolean onClick(@NotNull MouseEvent e, int clickCount) {
				int row = tree.getRowForLocation(e.getX(), e.getY());
				if (row >= 0) {
					Object o = tree.getPathForRow(row).getLastPathComponent();
					if (o instanceof WebSocketTreeNode) {
						Rectangle rowBounds = tree.getRowBounds(row);
						cellRenderer.setBounds(rowBounds);
						cellRenderer.validate();
//						Rectangle checkBounds = cellRenderer.myCheckbox.getBounds();
//						checkBounds.setLocation(rowBounds.getLocation());
//						if (checkBounds.height == 0) {
//							checkBounds.height = checkBounds.width = rowBounds.height;
//						}

						WebSocketTreeNode node = (WebSocketTreeNode) o;
						/*if (checkBounds.contains(e.getPoint()) && cellRenderer.myCheckbox.isVisible()) {
							if (node.isEnabled()) {
								WebSocketTreeHelper.this.toggleNode(tree, node);
								tree.setSelectionRow(row);
								return true;
							}
						} else*/
//						cellRenderer.getDateTimeRenderer().setForeground(JBColor.WHITE);
						if (clickCount == 1) {
							WebSocketTreeHelper.this.myEventDispatcher.getMulticaster().onSelected(node);
							return true;
						} else if (clickCount > 1 && clickCount % 2 == 0) {
							WebSocketTreeHelper.this.myEventDispatcher.getMulticaster().mouseDoubleClicked(node);
							return true;
						}
					}
				}
				return false;
			}
		};
		listener.installOn(mainComponent);
		return listener;
	}

	private void removeTreeListeners(JComponent mainComponent) {
		Runnable remover = ComponentUtil.getClientProperty(mainComponent, TREE_LISTENERS_REMOVER);
		if (remover != null) {
			remover.run();
		}
	}

	public static boolean isToggleEvent(KeyEvent e, JComponent mainComponent) {
		return e.getKeyCode() == 32 && SpeedSearchSupply.getSupply(mainComponent) == null;
	}

	private void toggleNode(Tree tree, WebSocketTreeNode node) {
		this.setNodeState(tree, node, !node.isChecked());
	}

	public void setNodeState(Tree tree, WebSocketTreeNode node, boolean checked) {
		this.changeNodeState(node, checked);
		this.adjustParentsAndChildren(node, checked);
		tree.repaint();
		TreeModel model = tree.getModel();
		model.valueForPathChanged(new TreePath(node.getPath()), node.getUserObject());
	}

	private void changeNodeState(WebSocketTreeNode node, boolean checked) {
		if (node.isChecked() != checked) {
			this.myEventDispatcher.getMulticaster().beforeNodeStateChanged(node);
			node.setChecked(checked);
			this.myEventDispatcher.getMulticaster().nodeStateChanged(node);
		}
	}

	private void adjustParentsAndChildren(WebSocketTreeNode node, boolean checked) {
		TreeNode parent;
		if (!checked) {
			if (this.myCheckPolicy.uncheckParentWithUncheckedChild) {
				for(parent = node.getParent(); parent != null; parent = parent.getParent()) {
					if (parent instanceof WebSocketTreeNode) {
						this.changeNodeState((WebSocketTreeNode)parent, false);
					}
				}
			}

			if (this.myCheckPolicy.uncheckChildrenWithUncheckedParent) {
				this.uncheckChildren(node);
			}
		} else {
			if (this.myCheckPolicy.checkChildrenWithCheckedParent) {
				this.checkChildren(node);
			}

			if (this.myCheckPolicy.checkParentWithCheckedChild) {
				for(parent = node.getParent(); parent != null; parent = parent.getParent()) {
					if (parent instanceof WebSocketTreeNode) {
						this.changeNodeState((WebSocketTreeNode)parent, true);
					}
				}
			}
		}
	}

	private void uncheckChildren(WebSocketTreeNode node) {
		Enumeration children = node.children();

		while(children.hasMoreElements()) {
			Object o = children.nextElement();
			if (o instanceof WebSocketTreeNode) {
				WebSocketTreeNode child = (WebSocketTreeNode)o;
				this.changeNodeState(child, false);
				this.uncheckChildren(child);
			}
		}
	}


	private void checkChildren(WebSocketTreeNode node) {
		Enumeration children = node.children();

		while(children.hasMoreElements()) {
			Object o = children.nextElement();
			if (o instanceof WebSocketTreeNode) {
				WebSocketTreeNode child = (WebSocketTreeNode)o;
				this.changeNodeState(child, true);
				this.checkChildren(child);
			}
		}
	}

	private KeyListener setupKeyListener(final Tree tree, final JComponent mainComponent) {
		KeyListener listener = new KeyAdapter() {
			@Override
			public void keyPressed(@NotNull KeyEvent e) {
				if (WebSocketTreeHelper.isToggleEvent(e, mainComponent)) {
					TreePath treePath = tree.getLeadSelectionPath();
					if (treePath == null) {
						return;
					}

					Object o = treePath.getLastPathComponent();
					if (!(o instanceof WebSocketTreeNode)) {
						return;
					}

					WebSocketTreeNode firstNode = (WebSocketTreeNode) o;
					if (!firstNode.isEnabled()) {
						return;
					}

					WebSocketTreeHelper.this.toggleNode(tree, firstNode);
					boolean checked = firstNode.isChecked();
					TreePath[] selectionPaths = tree.getSelectionPaths();

					for(int i = 0; selectionPaths != null && i < selectionPaths.length; ++i) {
						TreePath selectionPath = selectionPaths[i];
						Object o1 = selectionPath.getLastPathComponent();
						if (o1 instanceof WebSocketTreeNode) {
							WebSocketTreeNode node = (WebSocketTreeNode)o1;
							WebSocketTreeHelper.this.setNodeState(tree, node, checked);
						}
					}

					e.consume();
				}
			}
		};
		mainComponent.addKeyListener(listener);
		return listener;
	}

}
