package io.github.whimthen.websocket.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class ConfirmDialog extends DialogWrapper {

	public ConfirmDialog() {
		super(true);
		init();
		setTitle("Close Confirm");
		setResizable(false);
	}

	@Nullable
	@Override
	protected JComponent createCenterPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Should be close this websocket?");
		label.setPreferredSize(new Dimension(100, 100));
		panel.add(label, BorderLayout.CENTER);
		return panel;
	}

}
