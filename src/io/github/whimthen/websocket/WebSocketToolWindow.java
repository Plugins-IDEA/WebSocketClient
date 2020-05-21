package io.github.whimthen.websocket;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.messages.MessageDialog;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import io.github.whimthen.websocket.service.WebSocketService;
import io.github.whimthen.websocket.ui.ConfirmDialog;
import io.github.whimthen.websocket.ui.WebSocketWindowPanel;
import io.github.whimthen.websocket.utils.ComponentUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;

public class WebSocketToolWindow implements ToolWindowFactory {

	public static Content selectedContent;

	public static @NotNull DefaultActionGroup getActionGroup() {
		return getPanel().getActionGroup();
	}

	public static @NotNull WebSocketWindowPanel getPanel() {
		return (WebSocketWindowPanel) selectedContent.getComponent();
	}

	@Override
	public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
		WebSocketService instance = WebSocketService.getInstance();
		selectedContent = instance.initWindow(project);
		toolWindow.getContentManager().addContent(selectedContent);
		toolWindow.getContentManager().setSelectedContent(selectedContent);
		toolWindow.addContentManagerListener(new WebSocketContentManagerListener(toolWindow));
	}

	public static class WebSocketContentManagerListener implements ContentManagerListener {

		private final ToolWindow toolWindow;

		public WebSocketContentManagerListener(@NotNull ToolWindow toolWindow) {
			this.toolWindow = toolWindow;
		}

		@Override
		public void contentAdded(@NotNull ContentManagerEvent event) {
			selectedContent.setCloseable(true);
			toolWindow.getContentManager().setSelectedContent(event.getContent());
		}

		@Override
		public void contentRemoved(@NotNull ContentManagerEvent event) {
			if (toolWindow.getContentManager().getContentCount() == 1) {
				toolWindow.getContentManager().getContent(0).setCloseable(false);
			}
			event.getContent().release();
		}

		@Override
		public void contentRemoveQuery(@NotNull ContentManagerEvent event) {
			if (getPanel().isConnect()) {
				int yesNo = Messages.showYesNoDialog("The connection is about to be closed. Are you sure you want to close the tab?",
													 "Close Confirm",
													 Messages.getQuestionIcon());
				if (yesNo != 0) {
					event.consume();
				}
			}
		}

		@Override
		public void selectionChanged(@NotNull ContentManagerEvent event) {
			selectedContent = event.getContent();
		}
	}

}
