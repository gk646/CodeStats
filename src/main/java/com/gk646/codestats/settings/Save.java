package com.gk646.codestats.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.Nullable;

@State(name = "CodeStatsSettings", storages = {@Storage("CodeStatsSettings.xml")})
public class Save implements PersistentStateComponent<Save.State> {
    private State myState = new State();

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(State state) {
        myState = state;
    }

    public static class State {
        public String excludedFileTypes;
        public String separateTabFileTypes;
    }
}
