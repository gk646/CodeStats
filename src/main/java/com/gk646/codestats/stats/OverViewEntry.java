package com.gk646.codestats.stats;

public record OverViewEntry(


        String name,
        int sizeSum,
        int sizeMax,
        int sizeAvg,
        int lines,
        int linesMin,
        int linesMax
) {}
