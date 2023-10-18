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
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class LineChartPanel extends JPanel {
    private static final int PADDING_LEFT = 45;
    private static final int PADDING_RIGHT = 5;
    private static final int PADDING_TOP = 30;
    private static final int PADDING_BOTTOM = 25;
    private static final int TICK_SIZE = 5;
    private static final int TOOLTIP_MARGIN = 15;
    private static final JBColor GRID_COLOR = JBColor.WHITE;
    private static final JBColor AXIS_COLOR = JBColor.BLACK;
    private static final Font AXIS_FONT = new Font(EditorColorsManager.getInstance().getGlobalScheme().getEditorFontName(), Font.PLAIN, 12);
    public final Timer resizeTimer;
    private final JButton exampleButton;
    private final JComboBox<String> exampleDropdown;
    private final ChartMouseMotionListener mouseListener = new ChartMouseMotionListener();
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
        ToolTipManager.sharedInstance().setInitialDelay(5);
        ToolTipManager.sharedInstance().setReshowDelay(10);
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        this.addMouseMotionListener(mouseListener);

        // setLayout(new BorderLayout());

        exampleButton = new JButton("Example");
        exampleDropdown = new ComboBox<>(new String[]{"Option 1", "Option 2", "Option 3"});

        this.add(exampleButton, BorderLayout.NORTH);
        this.add(exampleDropdown, BorderLayout.NORTH);
    }

    public void refreshChart() {
        offScreenImage = UIUtil.createImage(this, getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

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
        g2d.setFont(AXIS_FONT);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long minX = Long.MAX_VALUE;
        long maxX = Long.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (TimePoint p : points) {
            long x = p.getX();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;

            int y = p.getY();
            if (y > maxY) maxY = y;
        }
        var maxDate = Instant.ofEpochMilli(maxX).atZone(ZoneId.systemDefault()).toLocalDate();
        var minDate = Instant.ofEpochMilli(minX).atZone(ZoneId.systemDefault()).toLocalDate();

        double scaleX = (double) (getWidth() - PADDING_LEFT - PADDING_RIGHT) / (maxX - minX);
        double scaleY = (double) (getHeight() - PADDING_TOP - PADDING_BOTTOM) / maxY;

        drawAxis(g2d, minDate, maxDate, minX, scaleX, scaleY, maxY / 5);
        drawTimePoints(g2d, points, minX, scaleX, scaleY);


        mouseListener.update(minX, scaleX, scaleY, points, getWidth(), getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (refreshGraphic) {
            var time = System.nanoTime();
            refreshChart();
            System.out.println((System.nanoTime() - time) / 1000);
            refreshGraphic = false;
        }

        if (offScreenImage != null) {
            g.drawImage(offScreenImage, 0, 0, this);
        }
    }

    private void drawAxis(Graphics2D g2d, LocalDate minDate, LocalDate maxDate, long minX, double scaleX, double scaleY, long intervalY) {
        long daysBetween = ChronoUnit.DAYS.between(minDate, maxDate);
        long idealIncrement = daysBetween / 6;
        long dateIncrement;
        if (idealIncrement <= 1) {
            dateIncrement = 1;
        } else if (idealIncrement <= 3) {
            dateIncrement = 2;
        } else if (idealIncrement <= 10) {
            dateIncrement = 7;
        } else if (idealIncrement <= 15) {
            dateIncrement = 15;
        } else {
            dateIncrement = 30;
        }

        var fm = g2d.getFontMetrics();
        for (int i = 0; i < 6; i++) {
            LocalDate date = minDate.plusDays(i * dateIncrement);
            if (!date.isAfter(maxDate)) {
                long dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                if (dateMillis < minX) {
                    dateMillis = minX;
                }
                //Vertical Lines and X-Axis Labels
                int xTick = (int) ((dateMillis - minX) * scaleX + PADDING_LEFT);

                g2d.setColor(GRID_COLOR.brighter());
                g2d.drawLine(xTick, PADDING_TOP, xTick, getHeight() - PADDING_BOTTOM);


                g2d.setColor(AXIS_COLOR);
                g2d.drawLine(xTick, getHeight() - PADDING_BOTTOM - TICK_SIZE, xTick, getHeight() - PADDING_BOTTOM + TICK_SIZE);

                String dateString = date.toString();
                Rectangle2D stringBounds = g2d.getFontMetrics().getStringBounds(dateString, g2d);
                if (i == 0) {
                    g2d.drawString(dateString, xTick, getHeight() - PADDING_BOTTOM + 20);
                } else {
                    g2d.drawString(dateString, (int) (xTick - stringBounds.getWidth() / 2), getHeight() - PADDING_BOTTOM + 20);
                }
            }
            //Horizontal Lines and Y-Axis labels
            int yTick = (int) (getHeight() - PADDING_BOTTOM - (intervalY * i) * scaleY);

            g2d.setColor(GRID_COLOR.brighter());
            g2d.drawLine(PADDING_LEFT, yTick, getWidth() - PADDING_RIGHT, yTick);


            g2d.setColor(AXIS_COLOR);
            g2d.drawLine(PADDING_LEFT - TICK_SIZE, yTick, PADDING_LEFT + TICK_SIZE, yTick);
            var s = String.valueOf(intervalY * i);
            g2d.drawString(s, PADDING_LEFT - 7 - fm.stringWidth(s), yTick + 5);
        }
    }

    private void drawTimePoints(Graphics2D g2d, List<TimePoint> points, long minX, double scaleX, double scaleY) {
        BasicStroke lineStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(lineStroke);

        g2d.setColor(JBColor.ORANGE);
        for (int i = 0; i < points.size() - 1; i++) {
            int x1 = (int) ((points.get(i).getX() - minX) * scaleX + PADDING_LEFT);
            int y1 = (int) (getHeight() - ((points.get(i).getY()) * scaleY + PADDING_BOTTOM));
            int x2 = (int) ((points.get(i + 1).getX() - minX) * scaleX + PADDING_LEFT);
            int y2 = (int) (getHeight() - ((points.get(i + 1).getY()) * scaleY + PADDING_BOTTOM));

            g2d.drawLine(x1, y1, x2, y2);
            g2d.fillOval(x1 - 5, y1 - 5, 10, 10);
        }
        int xLast = (int) ((points.get(points.size() - 1).getX() - minX) * scaleX + PADDING_LEFT);
        int yLast = (int) (getHeight() - ((points.get(points.size() - 1).getY()) * scaleY + PADDING_BOTTOM));
        g2d.fillOval(xLast - 5, yLast - 5, 10, 10);
    }

    public enum TimePointMode {COMMIT, GENERIC}

    public enum LineCountMode {TOTAL_LINES, CODE_LINES}

    private class ChartMouseMotionListener extends MouseMotionAdapter {
        int width;
        int height;
        private long minX;
        private double scaleX;
        private double scaleY;
        private List<TimePoint> points;

        public ChartMouseMotionListener() {

        }

        public void update(long minX, double scaleX, double scaleY, List<TimePoint> points, int width, int height) {
            this.width = width;
            this.height = height;
            this.minX = minX;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.points = points;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            boolean found = false;
            for (TimePoint point : points) {
                int x = (int) ((point.getX() - minX) * scaleX + PADDING_LEFT);
                int y = (int) (height - ((point.getY()) * scaleY + PADDING_BOTTOM));
                if (e.getX() >= x - TOOLTIP_MARGIN && e.getX() <= x + TOOLTIP_MARGIN && e.getY() >= y - TOOLTIP_MARGIN && e.getY() <= y + TOOLTIP_MARGIN) {
                    setToolTipText(point.toString());
                    ToolTipManager.sharedInstance().mouseMoved(e);
                    found = true;
                    break;
                }
            }

            if (!found) {
                setToolTipText(null);
            }
        }
    }
}

