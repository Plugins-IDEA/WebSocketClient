package io.github.whimthen.websocket.ui;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.openapi.editor.EditorLinePainter;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsUtil;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ViewportLayout;
import java.awt.Component;

/**
 * @author whimthen
 * @version 1.0.0
 * @since 1.0.0
 */
public class WsListCellRender extends DefaultListCellRenderer {

	public static WsListCellRender getInstance() {
		return new WsListCellRender();
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		setAlignmentX(10f);

		MessageItem item = (MessageItem) value;

		Document document = EditorFactory.getInstance().createDocument(item.getMessage());
		Editor viewer = EditorFactory.getInstance().createViewer(document);
		setIcon(item.getIcon());
		setText(item.toString());
		return viewer.getComponent();
	}

}
