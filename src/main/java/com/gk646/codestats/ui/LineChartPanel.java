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
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
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
import java.util.stream.Collectors;

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
        ToolTipManager.sharedInstance().setInitialDelay(50);
        ToolTipManager.sharedInstance().setReshowDelay(75);
        ToolTipManager.sharedInstance().setDismissDelay(10000);



    }

    public void refreshChart() {
        int width = getWidth();
        int height = getHeight();

        offScreenImage = UIUtil.createImage(this, width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D offScreenGraphics = offScreenImage.createGraphics();

        if (pointMode == TimePointMode.GENERIC) {
            renderChart(offScreenGraphics, TimePoint.generateMockTimePoints(100));
        } else {
            renderChart(offScreenGraphics, Save.getInstance().commitTimePoints);
        }

        offScreenGraphics.dispose();
        repaint();
    }

    private void renderChart(Graphics2D g2d, List<TimePoint> points) {
        g2d.setFont(new Font(EditorColorsManager.getInstance().getGlobalScheme().getEditorFontName(), Font.PLAIN, 14));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        long minX = points.stream().mapToLong(TimePoint::getX).min().orElse(0);
        long maxX = points.stream().mapToLong(TimePoint::getX).max().orElse(0);
        int minY = 0;
        int maxY = points.stream().mapToInt(TimePoint::getY).max().orElse(0);

        double scaleX = (double) (getWidth() - 2 * PADDING) / (maxX - minX);
        double scaleY = (double) (getHeight() - 2 * PADDING) / (maxY - minY);

        long intervalY = (maxY - minY) / 5;


        List<LocalDate> dates = points.stream()
                .map(p -> Instant.ofEpochMilli(p.getX()).atZone(ZoneId.systemDefault()).toLocalDate())
                .toList();

        LocalDate today = LocalDate.now();
        LocalDate defaultMinDate = today.minusDays(7);
        LocalDate minDate = dates.stream().min(LocalDate::compareTo).orElse(defaultMinDate);
        LocalDate maxDate = dates.stream().max(LocalDate::compareTo).orElse(today);
        long daysBetween = ChronoUnit.DAYS.between(minDate, maxDate);

        long dateIncrement;
        if (daysBetween <= 2) {
            dateIncrement = 1;
        } else if (daysBetween <= 7) {
            dateIncrement = 2;
        } else if (daysBetween <= 30) {
            dateIncrement = 7;
        } else {
            dateIncrement = 30;
        }

        for (int i = 0; i <= 5; i++) {
            if(i < dates.size()) {
                LocalDate date = minDate.plusDays(i * dateIncrement);
                if(!date.isAfter(maxDate)) {
                    long dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    int xTick = (int) ((dateMillis - minX) * scaleX + PADDING);

                    g2d.setColor(GRID_COLOR.brighter());
                    g2d.drawLine(xTick, PADDING, xTick, getHeight() - PADDING);

                    g2d.setColor(AXIS_COLOR);
                    g2d.drawLine(xTick, getHeight() - PADDING - TICK_SIZE, xTick, getHeight() - PADDING + TICK_SIZE);

                    String dateString = date.toString();
                    Rectangle2D stringBounds = g2d.getFontMetrics().getStringBounds(dateString, g2d);
                    if(i == 0){
                        g2d.drawString(dateString, (int) (xTick + stringBounds.getWidth()), getHeight() - PADDING + 20);
                    }else {
                        g2d.drawString(dateString, (int) (xTick - stringBounds.getWidth() / 2), getHeight() - PADDING + 20);
                    }
                }
            }

            int yTick = (int) (getHeight() - ((minY + intervalY * i - minY) * scaleY + PADDING));

            g2d.setColor(GRID_COLOR.brighter());
            g2d.drawLine(PADDING, yTick, getWidth() - PADDING, yTick);

            g2d.setColor(AXIS_COLOR);
            g2d.drawLine(PADDING - TICK_SIZE, yTick, PADDING + TICK_SIZE, yTick);
            g2d.drawString(String.valueOf(minY + intervalY * i), PADDING - 25, yTick + 5);
        }

        BasicStroke lineStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(lineStroke);
        GradientPaint lineGradient = new GradientPaint(0, 0, JBColor.BLUE, getWidth(), getHeight(), JBColor.CYAN);
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
                        setToolTipText(point.toString());
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
