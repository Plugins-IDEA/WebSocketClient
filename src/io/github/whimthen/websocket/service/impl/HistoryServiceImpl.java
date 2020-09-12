package io.github.whimthen.websocket.service.impl;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import io.github.whimthen.websocket.service.HistoryService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

@State(name = "io.github.whimthen.websocket.History", storages = {
	@Storage(value = "io.github.whimthen.websocket.xml")
})
public class HistoryServiceImpl implements HistoryService {

	private State state;

	@Nullable
	@Override
	public State getState() {
		return this.state;
	}

	@Override
	public void loadState(@NotNull State state) {
		this.state = state;
	}

	@Override
	public State get() {
		State state = Objects.isNull(this.state) ? new State() : this.state;
		if (Objects.isNull(state.urls))
			state.urls = new ArrayList<>();
		return state;
	}

	@Override
	public void save(String wsUrl) {
		if (get().urls.contains(wsUrl))
			return;
		loadState(get().add(wsUrl));
	}

	@Override
	public boolean isEmpty() {
		State state = getState();
		return Objects.isNull(state) || Objects.isNull(state.urls) || state.urls.isEmpty();
	}

}
