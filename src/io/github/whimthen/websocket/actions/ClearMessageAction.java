package io.github.whimthen.websocket.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.playback.commands.DelayCommand;
import com.intellij.remoteServer.util.DelayedRunner;
import kotlinx.coroutines.EventLoopImplBase;
import org.jetbrains.annotations.NotNull;

public class ClearMessageAction extends AnAction {

	public static final String ID = "WebSocket.ClearMessageAction";

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		// TODO: insert action logic here
	}

}
