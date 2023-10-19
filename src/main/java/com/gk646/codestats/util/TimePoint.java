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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A simple class imitating {@link java.awt.Point} but with the ability to return different values based on outside parameters such as {@link com.gk646.codestats.ui.LineChartPanel.TimePointMode}.
 */

public final class TimePoint {
    public static final int MILLISEC_PER_DAY = 86400000;
    /**
     * The current timestamp as per {@link System#currentTimeMillis()}
     */
    public long timestamp;
    public int linesCode;
    public int totalLines;
    /**
     * The {@link LocalDate} as string when created with {@link com.gk646.codestats.ui.LineChartPanel.TimePointMode#GENERIC} and up to 25 character of the commit message with {@link com.gk646.codestats.ui.LineChartPanel.TimePointMode#COMMIT}
     */
    public String info;

    /**
     * Empty constructor for the {@link com.intellij.openapi.components.PersistentStateComponent} interface
     */
    public TimePoint() {
    }

    public TimePoint(long timestamp, int linesCode, int totalLines, String info) {
        this.timestamp = timestamp;
        this.linesCode = linesCode;
        this.totalLines = totalLines;
        this.info = info;
    }

    /**
     * To test the scaling and visuals of the line chart
     *
     * @param numPoints
     * @return
     */
    public static @NotNull List<TimePoint> generateMockTimePoints(int numPoints) {
        List<TimePoint> mockPoints = new ArrayList<>();


        double a = -1;
        double b = 25000.0 / numPoints;
        double c = 0;

        for (int i = 0; i < numPoints; i++) {
            TimePoint point = new TimePoint();

            long randomMillisOffset = (long) (numPoints - i) * TimePoint.MILLISEC_PER_DAY / 2;
            point.timestamp = ZonedDateTime.now().toInstant().toEpochMilli() - randomMillisOffset;

            point.linesCode = (int) (a * Math.pow(i, 2) + b * i + c);
            point.totalLines = point.linesCode;

            LocalDate date = Instant.ofEpochMilli(point.timestamp).atZone(ZoneId.systemDefault()).toLocalDate();
            point.info = date.toString();

            mockPoints.add(point);
        }

        return mockPoints;
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

    @Contract(pure = true)
    @Override
    public @NotNull String toString() {
        if (CodeStatsWindow.TIME_LINE.lineMode == LineChartPanel.LineCountMode.TOTAL_LINES) {
            return info + " | Total Lines:" + totalLines;
        } else {
            return info + " | Code Lines:" + linesCode;
        }
    }
}
