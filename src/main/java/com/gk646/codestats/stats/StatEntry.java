package com.gk646.codestats.stats;

public record StatEntry(
        String name,
        int totalLines,
        int sourceCodeLines,
        int commentLines,
        int blankLines
) {}

