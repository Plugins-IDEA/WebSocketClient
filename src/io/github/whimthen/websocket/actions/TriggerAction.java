package io.github.whimthen.websocket.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import io.github.whimthen.websocket.service.ApplicableService;
import io.github.whimthen.websocket.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.intellij.openapi.wm.RegisterToolWindowTask.closable;

public class TriggerAction extends AnAction {

	@Override
	public void actionPerformed(@NotNull AnActionEvent event) {
		Project project = event.getProject();
		if (Objects.isNull(project)) {
			return;
		}
        ApplicableService.getInstance().applicable();
        ToolWindow client = ComponentUtil.getToolWindow(project);
		if (Objects.isNull(client)) {
            client = ToolWindowManager.getInstance(project).registerToolWindow(closable("", AllIcons.Actions.GroupByPrefix, ToolWindowAnchor.BOTTOM));
//			return;
		}
        if (Objects.isNull(client)) {
            return;
        }
		if (client.isVisible()) {
			client.hide();
		} else {
			client.show();
		}
	}

}
