package io.github.whimthen.websocket.ui;

import org.jetbrains.annotations.NotNull;

import java.util.EventListener;

public interface WebSocketTreeListener extends EventListener {

	default void mouseDoubleClicked(@NotNull WebSocketTreeNode node) {
	}

	default void nodeStateChanged(@NotNull WebSocketTreeNode node) {
	}

	default void beforeNodeStateChanged(@NotNull WebSocketTreeNode node) {
	}

	default void onSelected(@NotNull WebSocketTreeNode node) {

	}

}
