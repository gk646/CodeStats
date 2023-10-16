/*
 * MIT License
 *
 * Copyright (c) 2023 gk646
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

package com.gk646.codestats.util;

import com.gk646.codestats.CodeStatsWindow;
import com.gk646.codestats.ui.LineChartPanel;

public final class TimePoint {
    public long timestamp;
    public int linesCode;
    public int totalLines;
    //Date for GENERIC, commit msg for COMMIT
    public String string;

    public TimePoint() {
    }

    public TimePoint(long timestamp, int linesCode, int totalLines, String string) {
        this.timestamp = timestamp;
        this.linesCode = linesCode;
        this.totalLines = totalLines;
        this.string = string;
    }

    public long getX() {
        return timestamp;
    }

    public int getY() {
        if (CodeStatsWindow.TIME_LINE.lineMode == LineChartPanel.LineCountMode.CODE_LINES) {
            return linesCode;
        } else {
            return totalLines;
        }
    }
}
