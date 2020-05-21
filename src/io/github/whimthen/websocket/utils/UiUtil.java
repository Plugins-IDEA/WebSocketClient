package io.github.whimthen.websocket.utils;

import com.intellij.icons.AllIcons;
import com.intellij.ui.AnimatedIcon;

import javax.swing.Icon;

public class UiUtil {

	private static final Icon[] STEP_ICONS = new Icon[]{
		AllIcons.Process.Step_1,
		AllIcons.Process.Step_2,
		AllIcons.Process.Step_3,
		AllIcons.Process.Step_4,
		AllIcons.Process.Step_5,
		AllIcons.Process.Step_6,
	};

	public static Icon getLoadingIcon() {
		return new AnimatedIcon(150, STEP_ICONS);
	}

}
