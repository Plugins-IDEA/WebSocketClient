package io.github.whimthen.websocket.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import io.github.whimthen.websocket.WsToolWindow;
import io.github.whimthen.websocket.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;

public class PauseReceiveAction extends AnAction {

	public static final String ID = "WebSocket.PauseReceiveAction";

	@Override
	public void update(@NotNull AnActionEvent e) {
		super.update(e);
		Presentation presentation = e.getPresentation();
		presentation.setEnabled(WsToolWindow.getPanel().isConnect());
		if (WsToolWindow.getPanel().isPauseReceived()) {
			AnAction pause   = ComponentUtil.getPauseAction();
			AnAction receive = ComponentUtil.getReceiveAction();
			WsToolWindow.getActionGroup().replaceAction(receive, pause);
		}
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		togglePaused();
	}

	public void togglePaused() {
		AnAction pause   = ComponentUtil.getPauseAction();
		AnAction receive = ComponentUtil.getReceiveAction();
		boolean  paused = WsToolWindow.getPanel().isPauseReceived();
		WsToolWindow.getPanel().setPauseReceived(!paused);
		if (paused) {
			WsToolWindow.getActionGroup().replaceAction(receive, pause);
		} else {
			WsToolWindow.getActionGroup().replaceAction(pause, receive);
		}
	}

}
