package com.gk646.codestats.tabs;

import com.gk646.codestats.stats.StatEntry;

public class FileTypeTab {
    String tabName;
    StatEntry statEntry;

    FileTypeTab(String tabName, StatEntry statEntry) {
        this.tabName = tabName;
        this.statEntry = statEntry;
    }
}
