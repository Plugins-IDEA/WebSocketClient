package io.github.whimthen.websocket.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.whimthen.websocket.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;

public class ReceiveAction extends AnAction {

	public static final String ID = "WebSocket.ReceiveAction";

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		ComponentUtil.getPauseAction().togglePaused();
	}

}
