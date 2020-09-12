package io.github.whimthen.websocket.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.AutoScrollToSourceHandler;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import io.github.whimthen.websocket.service.HistoryService;
import io.github.whimthen.websocket.utils.ComponentUtil;
import io.github.whimthen.websocket.utils.ConnectionUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import sun.swing.DefaultLookup;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH;
import static com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

public class WsWindowPanel extends SimpleToolWindowPanel {

    private final Project project;
    private boolean isConnect = false;
    private boolean isPauseReceived = false;
    private final Storage settings;
    private final AutoScrollToSourceHandler autoScrollHandler;
    private final JPanel contentPanel;
    private final DefaultActionGroup actionGroup = new DefaultActionGroup();
    private final JComponent address;
    private final JTextField message;
    private final JButton sendBtn;
    private final JButton resetBtn;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-hh HH:mm:ss:SSS");
    private final ValidationInfo validationInfo;
    private final String addressToolTip = "Enter the websocket address";
    private final JBList<MessageItem> list;
    private final ToolbarDecorator decorator;
    private final CollectionListModel<MessageItem> listModel;

    public static WsWindowPanel getInstance(@NotNull Project project) {
        return new WsWindowPanel(project);
    }

    public WsWindowPanel(@NotNull Project project) {
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
        this.sendBtn = new JButton("Send");
        this.resetBtn = new JButton("Reset");
        this.validationInfo = new ValidationInfo("The websocket address is invalid.", this.address);
        MessageItem item = MessageItem.newInstance().setIcon(AllIcons.Gutter.Colors).setMessage("ssssssss").setDatetime("2020-09-12");
        this.listModel = new CollectionListModel<>(item);
        this.list = new JBList<>(listModel);
        this.list.getEmptyText().setText("Nothing to show, Please to connect ws");
//        this.list.installCellRenderer(this::cellRender);
        new JBList.StripedListCellRenderer();
        this.list.setCellRenderer(WsListCellRender.getInstance());
        this.list.setDataProvider(d -> {
            return MessageItem.newInstance().setIcon(AllIcons.Gutter.Colors).setMessage("ssssssss").setDatetime("2020-09-12");
        });
        this.decorator = ToolbarDecorator.createDecorator(list);
        this.decorator.setAddAction(a -> {
            this.list.add(new JBLabel(a.toString()));
        });
        this.autoScrollHandler = new AutoScrollToSourceHandler() {
            @Override
            protected boolean isAutoScrollMode() {
                return WsWindowPanel.this.settings.isAutoScrollToSource();
            }

            @Override
            protected void setAutoScrollMode(boolean b) {
                WsWindowPanel.this.settings.setAutoScrollToSource(b);
            }
        };
        setContent(getContentPanel());
    }

    private JComponent cellRender(MessageItem item) {
        return new JBLabel(item.toString());
    }

    private JPanel getContentPanel() {
        JBPanel<SimpleToolWindowPanel> panel = new JBPanel<>(new GridLayoutManager(
                1, 2, JBUI.emptyInsets(), 0, 0
        ));
        JBPanel<SimpleToolWindowPanel> toolbar = new JBPanel<>(new BorderLayout());
        toolbar.setBorder(JBUI.Borders.customLine(JBUI.CurrentTheme.DefaultTabs.borderColor(),
                0, 0, 0, 1));
        initToolbar(toolbar);

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
            inputPanel.add(this.sendBtn);
            inputPanel.add(this.resetBtn);
        }

        contentPanel.add(inputPanel, fillRowConstraintsVSizePolicyFixed(0));

        // ScrollPanel DataTree
        JBScrollPane scrollPane = new JBScrollPane(this.list, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(JBUI.Borders.empty());
        contentPanel.add(scrollPane, fillRowConstraints(1));
    }

    private void listenMsgChanged() {
        ValidationInfo okButtonValidationInfo = new ValidationInfo("Please input the message then to send", this.message);
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
        this.sendBtn.addActionListener(event -> {
            String message = this.message.getText();
            MessageItem item = MessageItem.newInstance().setIcon(AllIcons.Gutter.Colors).setMessage(message).setDatetime("2020-09-12");
            this.listModel.add(item);
//            if (StringUtils.isBlank(message)) {
//                this.message.requestFocus(true);
//                validator.updateInfo(okButtonValidationInfo);
//                return;
//            }
//            validator.updateInfo(null);
        });
        this.resetBtn.addActionListener(event -> {
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
                    WsWindowPanel.this.address.setToolTipText("");
                else
                    WsWindowPanel.this.address.setToolTipText(addressToolTip);
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

    public Storage getSettings() {
        return settings;
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
