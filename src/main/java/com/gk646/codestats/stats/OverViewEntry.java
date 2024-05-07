/*
 * MIT License
 *
 * Copyright (c) 2024 gk646
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gk646.codestats.stats;

/**
 * Describes an entry into the OverView tab of the {@link com.gk646.codestats.CodeStatsWindow#TABBED_PANE}
 */
public final class OverViewEntry {
    int count;
    long sizeMin = Integer.MAX_VALUE;
    long sizeSum;
    long sizeMax;
    int lines;
    int linesMin = Integer.MAX_VALUE;
    int linesMax;
    int linesCode;

    /**
     * As new files are parsed its stats are added to the overview tab at the correct position.
     * E.g. a new .java file has been processed and its stats are added to the OverView tab:
     * @param size fileSize in bytes
     * @param totalLines
     * @param sourceCodeLines
     */
    public void addValues(long size, int totalLines, int sourceCodeLines) {
        count++;

        sizeSum += size;
        if (size < sizeMin) {
            sizeMin = size;
        }
        if (size > sizeMax) {
            sizeMax = size;
        }

        lines += totalLines;
        if (totalLines < linesMin) {
            linesMin = totalLines;
        }
        if (totalLines > linesMax) {
            linesMax = totalLines;
        }
        linesCode += sourceCodeLines;
    }
}
