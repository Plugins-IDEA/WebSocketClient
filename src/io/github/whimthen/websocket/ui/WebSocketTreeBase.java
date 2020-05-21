package io.github.whimthen.websocket.ui;

import com.intellij.internal.inspector.UiDropperAction;
import com.intellij.json.JsonFileType;
import com.intellij.json.json5.Json5FileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.unscramble.AnalyzeStacktraceUtil;
import com.intellij.util.EventDispatcher;
import com.intellij.util.ui.ColorsIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.PlatformColors;
import com.intellij.util.ui.ThreeStateCheckBox;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WebSocketTreeBase extends Tree {
	private final WebSocketTreeHelper                    myHelper;
	private final EventDispatcher<WebSocketTreeListener> myEventDispatcher;

	public WebSocketTreeBase() {
		this(new WebSocketTreeBase.WebSocketTreeCellRendererBase(), null);
	}

	public WebSocketTreeBase(WebSocketTreeBase.WebSocketTreeCellRendererBase cellRenderer, WebSocketTreeNode root) {
		this(cellRenderer, root, WebSocketTreeHelper.DEFAULT_POLICY);
	}

	public WebSocketTreeBase(WebSocketTreeBase.WebSocketTreeCellRendererBase cellRenderer, WebSocketTreeNode root, WebSocketTreeBase.CheckPolicy checkPolicy) {
		this.myEventDispatcher = EventDispatcher.create(WebSocketTreeListener.class);
		this.myHelper = new WebSocketTreeHelper(checkPolicy, this.myEventDispatcher);
		if (root != null) {
			this.setModel(new DefaultTreeModel(root));
			this.setSelectionRow(0);
		}

		this.myEventDispatcher.addListener(new WebSocketTreeListener() {
			@Override
			public void mouseDoubleClicked(@NotNull WebSocketTreeNode node) {
				WebSocketTreeBase.this.onDoubleClick(node);
			}

			@Override
			public void nodeStateChanged(@NotNull WebSocketTreeNode node) {
				WebSocketTreeBase.this.onNodeStateChanged(node);
			}

			@Override
			public void beforeNodeStateChanged(@NotNull WebSocketTreeNode node) {
				WebSocketTreeBase.this.nodeStateWillChange(node);
			}

			@Override
			public void onSelected(@NotNull WebSocketTreeNode node) {
				WebSocketTreeBase.this.onNodeSelected(node);
			}
		});
		this.myHelper.initTree(this, this, cellRenderer);
	}

	protected void onNodeSelected(WebSocketTreeNode node) {
	}

	protected void onDoubleClick(WebSocketTreeNode node) {
	}

	protected void onNodeStateChanged(WebSocketTreeNode node) {
	}

	protected void nodeStateWillChange(WebSocketTreeNode node) {
	}

	public void setNodeState(@NotNull WebSocketTreeNode node, boolean checked) {
		this.myHelper.setNodeState(this, node, checked);
	}

	public void addCheckboxTreeListener(@NotNull WebSocketTreeListener listener) {
		this.myEventDispatcher.addListener(listener);
	}

	public <T> T[] getCheckedNodes(Class<? extends T> nodeType, @Nullable NodeFilter<? super T> filter) {
		return WebSocketTreeHelper.getCheckedNodes(nodeType, filter, this.getModel());
	}

	public static class WebSocketTreeCellRendererBase extends JPanel implements TreeCellRenderer {
		private final ColoredTreeCellRenderer myTextRenderer;
		private final ColoredTreeCellRenderer myDateTimeRender;
		//		public final  ThreeStateCheckBox      myCheckbox;
		private final boolean                 myUsePartialStatusForParentNodes;
		protected     boolean                 myIgnoreInheritance;
		private final DateTimeFormatter       formatter = DateTimeFormatter.ofPattern("yyyy-MM-hh HH:mm:ss:SSS");

		public WebSocketTreeCellRendererBase(boolean opaque) {
			this(opaque, true);
		}

		public WebSocketTreeCellRendererBase(boolean opaque, boolean usePartialStatusForParentNodes) {
			super(new BorderLayout());
			this.myUsePartialStatusForParentNodes = usePartialStatusForParentNodes;
//			this.myCheckbox = new ThreeStateCheckBox();
//			this.myCheckbox.setSelected(false);
//			this.myCheckbox.setThirdStateEnabled(false);
			this.myTextRenderer = new ColoredTreeCellRenderer() {
				@Override
				public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				}
			};
			this.myTextRenderer.setOpaque(opaque);
			this.myDateTimeRender = new ColoredTreeCellRenderer() {
				@Override
				public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				}
			};
			this.myDateTimeRender.setOpaque(false);

			EditorFactory factory = EditorFactory.getInstance();
			Document document = factory.createDocument("{" +
														   "\"datas\": {" +
														   "\"isLogin\": false" +
														   "}," +
														   "\"resMsg\": {" +
														   "\"code\": 1003," +
														   "\"method\": \"getUserInfo\"," +
														   "\"message\": \"授权失效，需要重新登录\"" +
														   "}" +
														   "}");
			document.setReadOnly(true);
			Editor editor = factory.createEditor(document, null, Json5FileType.INSTANCE, true);
			editor.setBorder(JBUI.Borders.empty());
			final EditorSettings settings = editor.getSettings();
			settings.setWhitespacesShown(true);
			settings.setLineMarkerAreaShown(false);
			settings.setIndentGuidesShown(false);
			settings.setLineNumbersShown(false);
			settings.setFoldingOutlineShown(false);
			settings.setRightMarginShown(false);
			settings.setVirtualSpace(false);
			settings.setWheelFontChangeEnabled(false);
			settings.setUseSoftWraps(false);
			settings.setAdditionalColumnsCount(0);
			settings.setAdditionalLinesCount(1);
			final EditorColorsScheme colorsScheme = editor.getColorsScheme();
			colorsScheme.setColor(EditorColors.CARET_ROW_COLOR, null);
			editor.getContentComponent().setFocusable(true);

			this.add(editor.getComponent(), "Center");
			this.add(this.myDateTimeRender, "East");
			this.myDateTimeRender.append(getNow());
		}

		private String getNow() {
			return LocalDateTime.now().format(formatter);
		}

		public WebSocketTreeCellRendererBase() {
			this(true);
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			this.invalidate();
//			if (value instanceof WebSocketTreeNode) {
//				WebSocketTreeNode        node  = (WebSocketTreeNode) value;
//				ThreeStateCheckBox.State state = this.getNodeStatus(node);
//				this.myCheckbox.setVisible(true);
//				this.myCheckbox.setEnabled(node.isEnabled());
//				this.myCheckbox.setSelected(state != ThreeStateCheckBox.State.NOT_SELECTED);
//				this.myCheckbox.setState(state);
//				this.myCheckbox.setOpaque(false);
//				this.myCheckbox.setBackground((Color)null);
//				this.setBackground(null);
//				if (UIUtil.isUnderWin10LookAndFeel()) {
//					Object hoverValue = this.getClientProperty("JCheckBox.rollOver.rectangle");
//					this.myCheckbox.getModel().setRollover(hoverValue == value);
//					Object pressedValue = this.getClientProperty("JCheckBox.pressed.rectangle");
//					this.myCheckbox.getModel().setPressed(pressedValue == value);
//				}
//			} else {
//				this.myCheckbox.setVisible(false);
//			}
			this.myTextRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
//			this.myDateTimeRender.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
			this.customizeRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
			this.revalidate();
			return this;
		}

		private ThreeStateCheckBox.State getNodeStatus(WebSocketTreeNode node) {
			if (this.myIgnoreInheritance) {
				return node.isChecked() ? ThreeStateCheckBox.State.SELECTED : ThreeStateCheckBox.State.NOT_SELECTED;
			} else {
				boolean checked = node.isChecked();
				if (node.getChildCount() != 0 && this.myUsePartialStatusForParentNodes) {
					ThreeStateCheckBox.State result = null;

					for (int i = 0; i < node.getChildCount(); ++i) {
						TreeNode                 child       = node.getChildAt(i);
						ThreeStateCheckBox.State childStatus = child instanceof WebSocketTreeNode ? this.getNodeStatus((WebSocketTreeNode) child) : (checked ? ThreeStateCheckBox.State.SELECTED : ThreeStateCheckBox.State.NOT_SELECTED);
						if (childStatus == ThreeStateCheckBox.State.DONT_CARE) {
							return ThreeStateCheckBox.State.DONT_CARE;
						}

						if (result == null) {
							result = childStatus;
						} else if (result != childStatus) {
							return ThreeStateCheckBox.State.DONT_CARE;
						}
					}

					return result == null ? ThreeStateCheckBox.State.NOT_SELECTED : result;
				} else {
					return checked ? ThreeStateCheckBox.State.SELECTED : ThreeStateCheckBox.State.NOT_SELECTED;
				}
			}
		}

		public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			if (value instanceof WebSocketTreeNode) {
				this.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
			}
		}

		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		}

		public ColoredTreeCellRenderer getTextRenderer() {
			return this.myTextRenderer;
		}

		public ColoredTreeCellRenderer getDateTimeRenderer() {
			return this.myDateTimeRender;
		}

//		private static class ComponentTreeCellRenderer extends ColoredTreeCellRenderer {
//			private final Component myInitialSelection;
//
//			ComponentTreeCellRenderer(Component initialSelection) {
//				this.myInitialSelection = initialSelection;
//				this.setFont(JBUI.Fonts.label(11.0F));
//				this.setBorder(JBUI.Borders.empty(0, 3));
//			}
//
//			public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//				Color foreground = UIUtil.getTreeForeground(selected, hasFocus);
//				Color background = selected ? UIUtil.getTreeSelectionBackground(hasFocus) : null;
//				if (value instanceof WebSocketTreeNode) {
//					WebSocketTreeNode componentNode = (WebSocketTreeNode) value;
//					Component component = componentNode.getComponent();
//					Class<?> clazz0 = component.getClass();
//					Class<?> clazz = clazz0.isAnonymousClass() ? clazz0.getSuperclass() : clazz0;
//					String name = component.getName();
//					if (!selected) {
//						if (!component.isVisible()) {
//							foreground = JBColor.GRAY;
//						} else if (component.getWidth() != 0 && component.getHeight() != 0) {
//							if (component.getPreferredSize() != null && (component.getSize().width < component.getPreferredSize().width || component.getSize().height < component.getPreferredSize().height)) {
//								foreground = PlatformColors.BLUE;
//							}
//						} else {
//							foreground = new JBColor(new Color(128, 10, 0), JBColor.BLUE);
//						}
//
//						if (this.myInitialSelection == componentNode.getComponent()) {
//							background = new Color(31, 128, 8, 58);
//						}
//					}
//
//					this.append(clazz.getSimpleName());
//					if (StringUtil.isNotEmpty(name)) {
//						this.append(" \"" + name + "\"");
//					}
//
//					this.append("UiDropperAction.RectangleRenderer.toString(component.getBounds())", SimpleTextAttributes.GRAYED_ATTRIBUTES);
//					if (component.isOpaque()) {
//						this.append(", opaque", SimpleTextAttributes.GRAYED_ATTRIBUTES);
//					}
//
//					if (component.isDoubleBuffered()) {
//						this.append(", double-buffered", SimpleTextAttributes.GRAYED_ATTRIBUTES);
//					}
//
//					componentNode.setText(this.toString());
//					this.setIcon(JBUI.scale(new ColorsIcon(11, new Color[]{component.getBackground(), component.getForeground()})));
//				}
//
//				this.setForeground((Color)foreground);
//				this.setBackground(background);
//				SpeedSearchUtil.applySpeedSearchHighlighting(tree, this, false, selected);
//			}
//		}
	}

	public static class CheckPolicy {
		final boolean checkChildrenWithCheckedParent;
		final boolean uncheckChildrenWithUncheckedParent;
		final boolean checkParentWithCheckedChild;
		final boolean uncheckParentWithUncheckedChild;

		public CheckPolicy(boolean checkChildrenWithCheckedParent, boolean uncheckChildrenWithUncheckedParent, boolean checkParentWithCheckedChild, boolean uncheckParentWithUncheckedChild) {
			this.checkChildrenWithCheckedParent = checkChildrenWithCheckedParent;
			this.uncheckChildrenWithUncheckedParent = uncheckChildrenWithUncheckedParent;
			this.checkParentWithCheckedChild = checkParentWithCheckedChild;
			this.uncheckParentWithUncheckedChild = uncheckParentWithUncheckedChild;
		}
	}

}
