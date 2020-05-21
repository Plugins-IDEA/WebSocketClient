package io.github.whimthen.websocket.ui;

import com.intellij.icons.AllIcons;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.AutoScrollToSourceHandler;
import com.intellij.ui.CommonActionsPanel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTreeTable;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.xml.ui.StringColumnInfo;
import com.sun.java.swing.action.DelegateAction;
import com.sun.java.swing.action.OkAction;
import com.sun.java.swing.ui.CommonUI;
import com.sun.java.swing.ui.OkCancelButtonPanel;
import com.sun.java.swing.ui.OkCancelDialog;
import io.github.whimthen.websocket.service.HistoryService;
import io.github.whimthen.websocket.utils.ComponentUtil;
import io.github.whimthen.websocket.utils.ConnectionUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

import static com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH;
import static com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED;
import static java.awt.FlowLayout.LEADING;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

public class WebSocketWindowPanel extends SimpleToolWindowPanel {

	private final Project                   project;
	private       boolean                   isConnect       = false;
	private       boolean                   isPauseReceived = false;
	private       TreeModel                 treeModel;
	private final Storage                   settings;
	private final WebSocketTree             tree;
	private final AutoScrollToSourceHandler autoScrollHandler;
	private final JPanel                    contentPanel;
	private final DefaultActionGroup        actionGroup     = new DefaultActionGroup();
	private final JComponent                address;
	private final JTextField                message;
	private final JButton                   okButton;
	private final JButton                   clearButton;
	private final DateTimeFormatter         formatter       = DateTimeFormatter.ofPattern("yyyy-MM-hh HH:mm:ss:SSS");
	private final ValidationInfo            validationInfo;
	private final String                    addressToolTip  = "Enter the websocket address";
	private JBTreeTable jbTreeTable;

	public static WebSocketWindowPanel getInstance(@NotNull Project project) {
		return new WebSocketWindowPanel(project);
	}

	public WebSocketWindowPanel(@NotNull Project project) {
		super(false, true);
		this.project = project;
		this.settings = new Storage();
		this.contentPanel = new JPanel(new GridLayoutManager(
			2, 1, JBUI.emptyInsets(), 0, 0
		));
		if (HistoryService.getInstance().isEmpty()) {
			this.address = new ExpandableTextField();
			this.address.setMinimumSize(new Dimension(270, (int) this.address.getSize().getHeight()));
		} else {
			this.address = new ComboBox<String>();
			getComboBoxAddress().setEditable(true);
			getComboBoxAddress().setEditor(new BasicComboBoxEditor() {
				@Override
				protected JTextField createEditorComponent() {
					return new ExpandableTextField();
				}
			});
			ApplicationManager.getApplication().invokeLater(() -> {
				AtomicInteger urlLength = new AtomicInteger();
				HistoryService.getInstance().get().urls.forEach(url -> {
					getComboBoxAddress().addItem(url);
					int length = url.length();
					if (length > urlLength.intValue()) {
						urlLength.set(length);
					}
				});
				this.address.setSize(urlLength.intValue(), (int) this.address.getSize().getHeight());
			});
		}
		this.message = new ExpandableTextField();
		this.okButton = new JButton("Send");
		this.clearButton = new JButton("Clear");
		this.validationInfo = new ValidationInfo("The websocket address is invalid.", this.address);
		this.treeModel = new DefaultTreeModel(null);
		this.tree = new WebSocketTree(createCellRenderer(), new WebSocketTreeNode("{\"sdfdsfdsfdsfds\":  \"jkhkjhjknjks\"}"));
		WebSocketTreeTableNode rootNode = new WebSocketTreeTableNode(null);
		this.jbTreeTable = new JBTreeTable(new ListTreeTableModel(rootNode, new ColumnInfo[]{new TreeColumnInfo("Message"), new DateColumnInfo("Date")}));
//		TreeTable treeTable = new TreeTable(new ListTreeTableModel(rootNode, new ColumnInfo[]{new TreeColumnInfo("Message"), new DateColumnInfo("Date")}));
		rootNode.add(new WebSocketTreeTableNode(rootNode));
		this.autoScrollHandler = new AutoScrollToSourceHandler() {
			@Override
			protected boolean isAutoScrollMode() {
				return WebSocketWindowPanel.this.settings.isAutoScrollToSource();
			}

			@Override
			protected void setAutoScrollMode(boolean b) {
				WebSocketWindowPanel.this.settings.setAutoScrollToSource(b);
			}
		};
		setContent(getContentPanel());
	}

	public WebSocketTree.WebSocketTreeCellRenderer createCellRenderer() {
		return new WebSocketTree.WebSocketTreeCellRenderer() {
			@Override
			public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.customizeRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
				if (!(value instanceof DefaultMutableTreeNode)) return;
				value = ((DefaultMutableTreeNode) value).getUserObject();

				if (value instanceof CharSequence) {
					if ("".equals(value)) {
						getTextRenderer().setIcon(AllIcons.Actions.Lightning);
						getTextRenderer().append(" " + value, SimpleTextAttributes.GRAY_ATTRIBUTES);
					} else {
						getTextRenderer().setIcon(AllIcons.Json.Object);
//						getTextRenderer().append(value.toString());
						EditorFactory factory  = EditorFactory.getInstance();
						Document      document = factory.createDocument(value.toString());
						Editor        editor   = factory.createEditor(document, null, JsonFileType.INSTANCE, false);
						getTextRenderer().add(editor.getComponent());
//						getDateTimeRenderer().append(getNow(), SimpleTextAttributes.GRAY_ATTRIBUTES);
//						getDateTimeRenderer().setForeground(UIUtil.getFocusedFillColor());
					}
				}
			}
		};
	}

	public void addTreeNode(@NotNull WebSocketTreeNode treeNode) {
//		this.tree.add(treeNode);
	}

	private JPanel getContentPanel() {
		JBPanel<SimpleToolWindowPanel> panel = new JBPanel<>(new GridLayoutManager(
			1, 2, JBUI.emptyInsets(), 0, 0
		));
		JBPanel<SimpleToolWindowPanel> toolbar = new JBPanel<>(new BorderLayout());
		toolbar.setBorder(JBUI.Borders.customLine(JBUI.CurrentTheme.DefaultTabs.borderColor(),
												  0, 0, 0, 1));
		initToolbar(toolbar);

		tree.setRootVisible(true);
		tree.setToggleClickCount(3);
		tree.getSelectionModel().setSelectionMode(SINGLE_TREE_SELECTION);
		EditSourceOnDoubleClickHandler.install(tree, () -> tree.expandPath(tree.getSelectionPath()));
		autoScrollHandler.install(tree);

		panel.add(toolbar, fillColumnConstraintsHSizePolicyFixed(0));

		initContentPanel(contentPanel);
		panel.add(contentPanel, fillColumnConstraints(1));

		return panel;
	}

	private void initContentPanel(JPanel contentPanel) {
		contentPanel.setBackground(JBColor.WHITE);
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));
		inputPanel.setBorder(JBUI.Borders.customLine(JBUI.CurrentTheme.DefaultTabs.borderColor(),
													 0, 0, 1, 0));

		// 第一行, 链接地址, 消息等
		{
			JLabel addressLabel = new JLabel(" Address:");
			inputPanel.add(addressLabel);
			listenUrlChanged();
			addressLabel.setLabelFor(this.address);
			this.address.setToolTipText(addressToolTip);
			inputPanel.add(this.address);
			JLabel messageLabel = new JLabel(" Message:");
			inputPanel.add(messageLabel);
			messageLabel.setLabelFor(this.message);
			listenMsgChanged();
			this.message.setToolTipText("Enter the websocket messages");
			inputPanel.add(this.message);
			inputPanel.add(this.okButton);
			inputPanel.add(this.clearButton);
		}

		contentPanel.add(inputPanel, fillRowConstraintsVSizePolicyFixed(0));

		// ScrollPanel DataTree
		JBScrollPane scrollPane = new JBScrollPane(this.jbTreeTable, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(JBUI.Borders.empty());
		contentPanel.add(scrollPane, fillRowConstraints(1));
	}

	private void listenMsgChanged() {
		ValidationInfo okButtonValidationInfo = new ValidationInfo("Please input the message then click send", this.message);
		ComponentValidator validator = new ComponentValidator(project);
		validator.withValidator(() -> {
			String addressText = this.message.getText();
			if (StringUtils.isBlank(addressText)) {
				return okButtonValidationInfo;
			}
			return null;
		}).installOn(this.message);
		this.message.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(@NotNull DocumentEvent e) {
				if (e.getDocument().getLength() > 0) {
					validator.updateInfo(null);
				}
			}
		});
		this.okButton.addActionListener(event -> {
			String message = this.message.getText();
			if (StringUtils.isBlank(message)) {
				this.message.requestFocus(true);
				validator.updateInfo(okButtonValidationInfo);
				return;
			}
			validator.updateInfo(null);
		});
		this.clearButton.addActionListener(event -> {
			this.message.setText("");
			validator.updateInfo(null);
		});
	}

	private void listenUrlChanged() {
		ComponentValidator validator = new ComponentValidator(project);
		validator.withValidator(() -> {
			String addressText = getAddressTextField().getText();
			if (StringUtils.isNotBlank(addressText) && ConnectionUtil.validateAddress(addressText)) {
				return this.validationInfo;
			}
			return null;
		}).installOn(this.address).andStartOnFocusLost();

		getAddressDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(@NotNull DocumentEvent e) {
				if (e.getDocument().getLength() > 0)
					WebSocketWindowPanel.this.address.setToolTipText("");
				else
					WebSocketWindowPanel.this.address.setToolTipText(addressToolTip);
				validator.andStartOnFocusLost();
			}
		});
	}

	private void initToolbar(JPanel toolbar) {
		actionGroup.add(ComponentUtil.getConnectAction());
		actionGroup.add(ComponentUtil.getDisConnectAction());
		actionGroup.add(ComponentUtil.getPauseAction());
		actionGroup.addSeparator();
		actionGroup.add(ComponentUtil.getClearAction());
		actionGroup.add(ComponentUtil.getAddTabAction());
		actionGroup.addSeparator();
		actionGroup.add(ComponentUtil.getExpandAction());
		actionGroup.add(ComponentUtil.getCollapseAction());

		ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_TITLE, actionGroup, false);
		actionToolbar.setTargetComponent(toolbar);
		toolbar.add(actionToolbar.getComponent());
	}

	private GridConstraints fillRowConstraints(int row) {
		GridConstraints constraints = new GridConstraints();
		constraints.setRow(row);
		constraints.setFill(FILL_BOTH);
		return constraints;
	}

	private GridConstraints fillColumnConstraints(int column) {
		GridConstraints constraints = new GridConstraints();
		constraints.setColumn(column);
		constraints.setFill(FILL_BOTH);
		return constraints;
	}

	@NotNull
	private GridConstraints fillColumnConstraintsHSizePolicyFixed(int column) {
		GridConstraints constraints = new GridConstraints();
		constraints.setColumn(column);
		constraints.setFill(FILL_BOTH);
		constraints.setHSizePolicy(SIZEPOLICY_FIXED);
		return constraints;
	}

	private GridConstraints fillRowConstraintsHSizePolicyFixed(int row) {
		GridConstraints constraints = new GridConstraints();
		constraints.setRow(row);
		constraints.setFill(FILL_BOTH);
		constraints.setHSizePolicy(SIZEPOLICY_FIXED);
		return constraints;
	}

	private GridConstraints fillRowConstraintsVSizePolicyFixed(int row) {
		GridConstraints constraints = new GridConstraints();
		constraints.setRow(row);
		constraints.setFill(FILL_BOTH);
		constraints.setVSizePolicy(SIZEPOLICY_FIXED);
		constraints.setHSizePolicy(SIZEPOLICY_FIXED);
		return constraints;
	}

	public JPanel getPanel() {
		return this.contentPanel;
	}

	public ExpandableTextField getTextFieldAddress() {
		return (ExpandableTextField) address;
	}

	@SuppressWarnings("unchecked")
	public ComboBox<String> getComboBoxAddress() {
		return (ComboBox<String>) address;
	}

	public JTextField getAddressComboBoxTextFiled() {
		return (JTextField) getComboBoxAddress().getEditor().getEditorComponent();
	}

	public JTextField getAddressTextField() {
		JTextField textField = null;
		if (HistoryService.getInstance().isEmpty()) {
			textField = getTextFieldAddress();
		} else {
			textField = getAddressComboBoxTextFiled();
		}
		return textField;
	}

	public JComponent getAddress() {
		JComponent address = null;
		if (HistoryService.getInstance().isEmpty()) {
			address = getTextFieldAddress();
		} else {
			address = getComboBoxAddress();
		}
		return address;
	}

	public javax.swing.text.Document getAddressDocument() {
		javax.swing.text.Document document = null;
		if (HistoryService.getInstance().isEmpty()) {
			document = getTextFieldAddress().getDocument();
		} else {
			document = getAddressComboBoxTextFiled().getDocument();
		}
		return document;
	}

	public boolean isConnect() {
		return isConnect;
	}

	public void setConnect(boolean connect) {
		isConnect = connect;
	}

	public boolean isPauseReceived() {
		return isPauseReceived;
	}

	public void setPauseReceived(boolean pauseReceived) {
		isPauseReceived = pauseReceived;
	}

	public TreeModel getTreeModel() {
		return treeModel;
	}

	public WebSocketWindowPanel setTreeModel(TreeModel treeModel) {
		this.treeModel = treeModel;
		return this;
	}

	public Storage getSettings() {
		return settings;
	}

	public WebSocketTree getTree() {
		return tree;
	}

	public AutoScrollToSourceHandler getAutoScrollHandler() {
		return autoScrollHandler;
	}

	public DefaultActionGroup getActionGroup() {
		return actionGroup;
	}

	static class Storage {
		private boolean isAutoScrollToSource = false;

		public boolean isAutoScrollToSource() {
			return isAutoScrollToSource;
		}

		public void setAutoScrollToSource(boolean autoScrollToSource) {
			isAutoScrollToSource = autoScrollToSource;
		}
	}

}
