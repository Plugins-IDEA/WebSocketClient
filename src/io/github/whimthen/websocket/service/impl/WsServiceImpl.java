package io.github.whimthen.websocket.service.impl;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import io.github.whimthen.websocket.service.WsService;
import io.github.whimthen.websocket.ui.WsWindowPanel;
import io.github.whimthen.websocket.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;

public class WsServiceImpl implements WsService {

	public Content initWindow(@NotNull Project project) {
		Content content = this.newContent(project);
		content.setCloseable(false);
		return content;
	}

	@Override
	public void createTab(@NotNull Project project) {
		ToolWindow           toolWindow  = ComponentUtil.getToolWindow(project);
		toolWindow.getContentManager().addContent(this.newContent(project));
	}

	@Override
	public Content newContent(@NotNull Project project) {
		ContentFactory factory = ContentFactory.SERVICE.getInstance();
		Content        content = factory.createContent(WsWindowPanel.getInstance(project), "Untitled", false);
		content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
		content.setIcon(AllIcons.Toolwindows.ToolWindowModuleDependencies);
		return content;
	}

}
