package io.github.whimthen.websocket.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import io.github.whimthen.websocket.service.WebSocketService;
import org.jetbrains.annotations.NotNull;

public class AddTabAction extends AnAction {

	public static final String ID = "WebSocket.AddTabAction";

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		WebSocketService.getInstance().createTab(e.getProject());
	}

}
