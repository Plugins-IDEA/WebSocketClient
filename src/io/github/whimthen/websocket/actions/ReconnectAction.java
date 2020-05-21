package io.github.whimthen.websocket.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.whimthen.websocket.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;

public class ReconnectAction extends AnAction {

	public static final String ID = "WebSocket.ReconnectAction";

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		ComponentUtil.getDisConnectAction().actionPerformed(e);
		ComponentUtil.getConnectAction().actionPerformed(e);
	}

}
