package com.gk646.codestats.stats;

public class StatEntry {
    String name;
    int totalLines;
    int sourceCodeLines;
    int commentLines;
    int blankLines;
    StatEntry(String name) {
        this.name = name;
    }
}

