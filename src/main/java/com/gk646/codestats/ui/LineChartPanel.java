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

package com.gk646.codestats.ui;

import com.gk646.codestats.util.PointI;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class LineChartPanel extends JPanel {
    private final List<PointI> points = new ArrayList<>();
    private final int PADDING = 30;
    private final int TICK_SIZE = 5;
    private final JBColor GRID_COLOR = new JBColor(new Color(0.8F, 0.8F, 0.8F, 0.5F), new Color(0.8F, 0.8F, 0.8F, 0.5F));
    private final Color AXIS_COLOR = Color.BLACK;
    private BufferedImage offScreenImage;
    private boolean refreshGraphic = true;

    public LineChartPanel() {
        for (int i = 0; i < 10000; i++) {
            points.add(new PointI(Math.random() * 100, Math.random() * 100));
        }
        points.add(new PointI(10, 10));
        points.add(new PointI(40, 80));
        points.add(new PointI(70, 40));
        points.add(new PointI(100, 100));
    }

    public void refreshChart() {
        int width = getWidth();
        int height = getHeight();

        offScreenImage = UIUtil.createImage(this, width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D offScreenGraphics = offScreenImage.createGraphics();

        renderChart(offScreenGraphics);

        offScreenGraphics.dispose();
        repaint();
    }

    private void renderChart(Graphics2D g2d) {
        // Enable antialiasing for smoother lines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Calculate min and max values for both axes
        int minX = points.stream().mapToInt(PointI::getX).min().orElse(0);
        int maxX = points.stream().mapToInt(PointI::getX).max().orElse(0);
        int minY = points.stream().mapToInt(PointI::getY).min().orElse(0);
        int maxY = points.stream().mapToInt(PointI::getY).max().orElse(0);

        // 2. Determine scaling factors
        double scaleX = (double) (getWidth() - 2 * PADDING) / (maxX - minX);
        double scaleY = (double) (getHeight() - 2 * PADDING) / (maxY - minY);

        // Draw a grid background
        g2d.setColor(GRID_COLOR);
        for (int i = PADDING; i < getWidth() - PADDING; i += PADDING) {
            g2d.drawLine(i, PADDING, i, getHeight() - PADDING);
        }
        for (int i = PADDING; i < getHeight() - PADDING; i += PADDING) {
            g2d.drawLine(PADDING, i, getWidth() - PADDING, i);
        }

        for (int i = 0; i < points.size() - 1; i++) {
            int x1 = (int) ((points.get(i).x - minX) * scaleX + PADDING);
            int y1 = (int) (getHeight() - ((points.get(i).y - minY) * scaleY + PADDING));
            int x2 = (int) ((points.get(i + 1).x - minX) * scaleX + PADDING);
            int y2 = (int) (getHeight() - ((points.get(i + 1).y - minY) * scaleY + PADDING));

            g2d.drawLine(x1, y1, x2, y2);
            g2d.fillOval(x1 - 3, y1 - 3, 6, 6);
        }
        int xLast = (int) ((points.get(points.size() - 1).x - minX) * scaleX + PADDING);
        int yLast = (int) (getHeight() - ((points.get(points.size() - 1).y - minY) * scaleY + PADDING));
        g2d.fillOval(xLast - 3, yLast - 3, 6, 6);

        // 4. Adjust the ticks and labels on the axes
        // This is just an example. You can adjust the intervals and labels to your liking
        int intervalX = (maxX - minX) / 5;
        int intervalY = (maxY - minY) / 5;

        for (int i = 0; i <= 5; i++) {
            int xTick = (int) (minX + intervalX * i * scaleX + PADDING);
            int yTick = (int) (getHeight() - (minY + intervalY * i * scaleY + PADDING));
            g2d.drawLine(xTick, getHeight() - PADDING - TICK_SIZE, xTick, getHeight() - PADDING + TICK_SIZE);
            g2d.drawString(String.valueOf(minX + intervalX * i), xTick - 10, getHeight() - PADDING + 2 * TICK_SIZE);
            g2d.drawLine(PADDING - TICK_SIZE, yTick, PADDING + TICK_SIZE, yTick);
            g2d.drawString(String.valueOf(minY + intervalY * i), PADDING - 3 * TICK_SIZE, yTick + 3);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (refreshGraphic) {
            refreshChart();
            refreshGraphic = false;
        }

        // Draw the pre-rendered image
        if (offScreenImage != null) {
            g.drawImage(offScreenImage, 0, 0, this);
        }
    }
}
