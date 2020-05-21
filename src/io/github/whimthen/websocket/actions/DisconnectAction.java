package io.github.whimthen.websocket.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.whimthen.websocket.WebSocketToolWindow;
import io.github.whimthen.websocket.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;

public class DisconnectAction extends AnAction {

	public static final String ID = "WebSocket.DisconnectAction";

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		e.getPresentation().setEnabled(WebSocketToolWindow.getPanel().isConnect());
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		// disconnected
		WebSocketToolWindow.getPanel().getAddressTextField().setEditable(true);
		WebSocketToolWindow.getActionGroup().replaceAction(ComponentUtil.getReConnectAction(), ComponentUtil.getConnectAction());
		WebSocketToolWindow.getPanel().setConnect(false);
		WebSocketToolWindow.selectedContent.setIcon(AllIcons.Toolwindows.ToolWindowModuleDependencies);
		update(e);
		ComponentUtil.getPauseAction().update(e);
		WebSocketToolWindow.getPanel().setPauseReceived(false);
	}

}
