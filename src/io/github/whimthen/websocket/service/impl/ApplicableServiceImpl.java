package io.github.whimthen.websocket.service.impl;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import io.github.whimthen.websocket.service.ApplicableService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "io.github.whimthen.websocket.Applicable", storages = {
    @Storage(value = "io.github.whimthen.websocket.xml")
})
public class ApplicableServiceImpl implements ApplicableService {

    private State state;

    @Override
    public void applicable() {
        if (this.state == null) {
            this.state = new State(true);
        } else if (!this.state.isApplicable) {
            this.state.isApplicable = true;
        }
    }

    @Override
    public boolean isApplicable() {
        return this.state != null && this.state.isApplicable;
    }

    @Nullable
    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

}
