package io.github.whimthen.websocket.utils;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.Gray;
import io.github.whimthen.websocket.actions.AddTabAction;
import io.github.whimthen.websocket.actions.ClearMessageAction;
import io.github.whimthen.websocket.actions.CollapseAllAction;
import io.github.whimthen.websocket.actions.ConnectAction;
import io.github.whimthen.websocket.actions.DisconnectAction;
import io.github.whimthen.websocket.actions.ExpandAllAction;
import io.github.whimthen.websocket.actions.PauseReceiveAction;
import io.github.whimthen.websocket.actions.ReceiveAction;
import io.github.whimthen.websocket.actions.ReconnectAction;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class ComponentUtil {

	public static final String TOOL_WINDOW_ID = "WebSocket";

	public static ToolWindow getToolWindow(@NotNull Project project) {
		return ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID);
	}

	public static AnAction getConnectAction() {
		return ActionManager.getInstance().getAction(ConnectAction.ID);
	}

	public static AnAction getReConnectAction() {
		return ActionManager.getInstance().getAction(ReconnectAction.ID);
	}

	public static AnAction getDisConnectAction() {
		return ActionManager.getInstance().getAction(DisconnectAction.ID);
	}

	public static AnAction getCollapseAction() {
		return ActionManager.getInstance().getAction(CollapseAllAction.ID);
	}

	public static AnAction getExpandAction() {
		return ActionManager.getInstance().getAction(ExpandAllAction.ID);
	}

	public static PauseReceiveAction getPauseAction() {
		return (PauseReceiveAction) ActionManager.getInstance().getAction(PauseReceiveAction.ID);
	}

	public static AnAction getAddTabAction() {
		return ActionManager.getInstance().getAction(AddTabAction.ID);
	}

	public static AnAction getClearAction() {
		return ActionManager.getInstance().getAction(ClearMessageAction.ID);
	}

	public static ReceiveAction getReceiveAction() {
		return (ReceiveAction) ActionManager.getInstance().getAction(ReceiveAction.ID);
	}

	public static Color getBorderColor() {
		return Gray._50;
	}

}
