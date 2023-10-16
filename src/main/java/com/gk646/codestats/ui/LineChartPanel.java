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

import com.gk646.codestats.settings.Save;
import com.gk646.codestats.util.TimePoint;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.List;

public final class LineChartPanel extends JPanel {
    public final Timer resizeTimer;
    private final int PADDING = 30;
    private final int TICK_SIZE = 5;
    private final int TOOLTIP_MARGIN = 15;
    private final JBColor GRID_COLOR = JBColor.WHITE;
    private final Color AXIS_COLOR = JBColor.BLACK;
    public TimePointMode pointMode = TimePointMode.GENERIC;
    public LineCountMode lineMode = LineCountMode.CODE_LINES;
    public boolean refreshGraphic = true;
    private BufferedImage offScreenImage;

    public LineChartPanel(JTabbedPane pane) {
        resizeTimer = new Timer(50, (ActionEvent e) -> {
            Component selectedComponent = pane.getSelectedComponent();
            if (selectedComponent == this) {
                this.refreshGraphic = true;
                repaint();
            }
        });
        resizeTimer.setRepeats(false);
        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setReshowDelay(200);
        ToolTipManager.sharedInstance().setDismissDelay(10000);
    }

    public void refreshChart() {
        int width = getWidth();
        int height = getHeight();

        offScreenImage = UIUtil.createImage(this, width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D offScreenGraphics = offScreenImage.createGraphics();

        if (pointMode == TimePointMode.GENERIC) {
            renderChart(offScreenGraphics, Save.getInstance().genericTimePoints);
        } else {
            renderChart(offScreenGraphics, Save.getInstance().commitTimePoints);
        }

        offScreenGraphics.dispose();
        repaint();
    }

    private void renderChart(Graphics2D g2d, List<TimePoint> points) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long minX = points.stream().mapToLong(TimePoint::getY).min().orElse(0);
        long maxX = points.stream().mapToLong(TimePoint::getX).max().orElse(0);
        int minY = 0;
        int maxY = points.stream().mapToInt(TimePoint::getY).max().orElse(0);

        double scaleX = (double) (getWidth() - 2 * PADDING) / (maxX - minX);
        double scaleY = (double) (getHeight() - 2 * PADDING) / (maxY - minY);

        g2d.setColor(GRID_COLOR.brighter());
        g2d.setFont(UIManager.getFont("Label.font").deriveFont((float) 12));
        long intervalX = (maxX - minX) / 5;
        int intervalY = (maxY - minY) / 5;

        for (int i = 0; i <= 5; i++) {
            int xTick = (int) ((minX + intervalX * i - minX) * scaleX + PADDING);
            int yTick = (int) (getHeight() - ((minY + intervalY * i - minY) * scaleY + PADDING));

            g2d.drawLine(xTick, PADDING, xTick, getHeight() - PADDING);
            g2d.drawLine(PADDING, yTick, getWidth() - PADDING, yTick);

            g2d.setColor(AXIS_COLOR);
            g2d.drawLine(xTick, getHeight() - PADDING - TICK_SIZE, xTick, getHeight() - PADDING + TICK_SIZE);
            g2d.drawLine(PADDING - TICK_SIZE, yTick, PADDING + TICK_SIZE, yTick);

            g2d.setColor(GRID_COLOR);
            g2d.drawString(String.valueOf(minX + intervalX * i), xTick - 10, getHeight() - PADDING + 20);
            g2d.drawString(String.valueOf(minY + intervalY * i), PADDING - 25, yTick + 5);
        }

        BasicStroke lineStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(lineStroke);
        GradientPaint lineGradient = new GradientPaint(0, 0, Color.BLUE, getWidth(), getHeight(), Color.CYAN);
        g2d.setPaint(lineGradient);

        for (int i = 0; i < points.size() - 1; i++) {
            int x1 = (int) ((points.get(i).getX() - minX) * scaleX + PADDING);
            int y1 = (int) (getHeight() - ((points.get(i).getY() - minY) * scaleY + PADDING));
            int x2 = (int) ((points.get(i + 1).getX() - minX) * scaleX + PADDING);
            int y2 = (int) (getHeight() - ((points.get(i + 1).getY() - minY) * scaleY + PADDING));

            g2d.drawLine(x1, y1, x2, y2);
            g2d.setColor(JBColor.ORANGE);
            g2d.fillOval(x1 - 5, y1 - 5, 10, 10);
        }

        int xLast = (int) ((points.get(points.size() - 1).getX() - minX) * scaleX + PADDING);
        int yLast = (int) (getHeight() - ((points.get(points.size() - 1).getY() - minY) * scaleY + PADDING));
        g2d.fillOval(xLast - 5, yLast - 5, 10, 10);

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean found = false;
                for (TimePoint point : points) {
                    int x = (int) ((point.getX() - minX) * scaleX + PADDING);
                    int y = (int) (getHeight() - ((point.getY() - minY) * scaleY + PADDING));
                    if (e.getX() >= x - TOOLTIP_MARGIN && e.getX() <= x + TOOLTIP_MARGIN && e.getY() >= y - TOOLTIP_MARGIN && e.getY() <= y + TOOLTIP_MARGIN) {
                        setToolTipText("X: " + point.getX() + ", Y: " + point.getY());
                        g2d.setColor(JBColor.RED);
                        g2d.fillOval(x - 7, y - 7, 14, 14);  // Highlighted point
                        ToolTipManager.sharedInstance().mouseMoved(e);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    setToolTipText(null);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (refreshGraphic) {
            refreshChart();
            refreshGraphic = false;
        }

        if (offScreenImage != null) {
            g.drawImage(offScreenImage, 0, 0, this);
        }
    }

    public enum TimePointMode {COMMIT, GENERIC}

    public enum LineCountMode {TOTAL_LINES, CODE_LINES}
}
