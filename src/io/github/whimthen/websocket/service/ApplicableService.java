package io.github.whimthen.websocket.service;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;

public interface ApplicableService extends PersistentStateComponent<ApplicableService.State> {

    static ApplicableService getInstance() {
        return ServiceManager.getService(ApplicableService.class);
    }

    class State {
        public boolean isApplicable;
        public State() {}
        public State(boolean isApplicable) {
            this.isApplicable = isApplicable;
        }
    }

    void applicable();

    boolean isApplicable();

}
