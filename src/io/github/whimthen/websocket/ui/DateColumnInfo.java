package io.github.whimthen.websocket.ui;

import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class DateColumnInfo extends ColumnInfo<String, String> {

	public DateColumnInfo(@Nls(capitalization = Nls.Capitalization.Title) String name) {
		super(name);
	}

	@Nullable
	@Override
	public String valueOf(String o) {
		return null;
	}

}
