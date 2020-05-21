package io.github.whimthen.websocket.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.AnimatedIcon;
import io.github.whimthen.websocket.WebSocketToolWindow;
import io.github.whimthen.websocket.service.HistoryService;
import io.github.whimthen.websocket.utils.ComponentUtil;
import io.github.whimthen.websocket.utils.ConnectionUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTextField;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectAction extends AnAction {

	public static final String ID = "WebSocket.ConnectAction";
	private ComponentValidator validator;

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		JTextField address = WebSocketToolWindow.getPanel().getAddressTextField();
		String addressText = address.getText();
		Project    project = e.getProject();
		ValidationInfo validationInfo = null;
		if (StringUtils.isBlank(addressText)) {
			validationInfo = new ValidationInfo("The websocket address can not be empty!", address);
		} else if (ConnectionUtil.validateAddress(addressText)) {
			validationInfo = new ValidationInfo("The websocket address is invalid.", address);
		}

		if (Objects.nonNull(validationInfo)) {
			focusAndTip(address, project, validationInfo);
			return;
		} else {
			getValidator(Objects.requireNonNull(project)).updateInfo(null);
		}

		// 保存wsUrl
		HistoryService.getInstance().save(addressText);

		AnAction connect = ComponentUtil.getConnectAction();
		AnAction reconnect = ComponentUtil.getReConnectAction();
		Presentation presentation = e.getPresentation();
		Icon connectIcon = presentation.getIcon();
		// connecting
		presentation.setIcon(new AnimatedIcon.Default());
		// connected
		address.setEditable(false);
		presentation.setIcon(connectIcon);
		WebSocketToolWindow.getActionGroup().replaceAction(connect, reconnect);
		WebSocketToolWindow.getPanel().setConnect(true);
		ComponentUtil.getDisConnectAction().update(e);
		ComponentUtil.getPauseAction().update(e);
		WebSocketToolWindow.selectedContent.setIcon(AllIcons.Debugger.ThreadStates.Socket);
		if (StringUtils.isNotBlank(addressText)) {
			Pattern pattern = ConnectionUtil.getAddressPattern();
			Matcher matcher = pattern.matcher(addressText);
			if (matcher.find()) {
				addressText = matcher.group(2);
				if (addressText.contains("/")) {
					addressText = addressText.substring(0, addressText.indexOf("/"));
				}
			}
			WebSocketToolWindow.selectedContent.setDisplayName(addressText);
		}
	}

	private void focusAndTip(JComponent address, Project project, ValidationInfo validationInfo) {
		if (WebSocketToolWindow.getPanel().getAddressTextField().hasFocus()) {
			addressTip(project, validationInfo);
		} else {
			address.requestFocus(true);
			ApplicationManager.getApplication().invokeLater(() -> {
				addressTip(project, validationInfo);
			});
		}
	}

	private void addressTip(Project project, ValidationInfo validationInfo) {
		ComponentValidator validator = getValidator(project);
		validator.updateInfo(null);
		validator.updateInfo(validationInfo);
	}

	private ComponentValidator getValidator(@NotNull Project project) {
		if (Objects.nonNull(this.validator)) {
			return this.validator;
		}
		synchronized (this) {
			if (Objects.nonNull(this.validator)) {
				return this.validator;
			}
			this.validator = new ComponentValidator(project);
			return this.validator;
		}
	}

}
