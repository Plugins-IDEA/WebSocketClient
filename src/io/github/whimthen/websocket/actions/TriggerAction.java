package io.github.whimthen.websocket.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import io.github.whimthen.websocket.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TriggerAction extends AnAction {

	@Override
	public void actionPerformed(@NotNull AnActionEvent event) {
		Project project = event.getProject();
		if (Objects.isNull(project)) {
			return;
		}
		ToolWindow debugger = ComponentUtil.getToolWindow(project);
		if (Objects.isNull(debugger)) {
			return;
		}
		if (debugger.isVisible()) {
			debugger.hide();
		} else {
			debugger.show();
		}
	}

}
