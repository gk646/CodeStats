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

import com.gk646.codestats.CodeStatsWindow;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

/**
 * Utility class for UI elements
 */
public final class UIHelper {

    public static final DefaultTableCellRenderer OverViewTableCellRenderer = getIconRenderer();
    public static final DefaultTableCellRenderer SeparateTableCellRenderer = getSeparateTableCellRenderer();

    private UIHelper() {
    }

    @Contract(" -> new")
    public static @NotNull TableCellRenderer getBoldRenderer() {
        //TODO return only a reference
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                if (column == 0) {
                    ((JComponent) c).setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, JBColor.WHITE));
                }
                return c;
            }
        };
    }

    @Contract(" -> new")
    public static @NotNull DefaultTableCellRenderer getIconRenderer() {

        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                switch (column) {
                    case 0 -> label.setIcon(AllIcons.Actions.InlayRenameInNoCodeFiles);
                    case 2, 3, 4, 5 -> label.setIcon(AllIcons.Actions.MenuSaveall);
                    case 6, 7, 8, 9, 10 -> label.setIcon(AllIcons.Toolwindows.ToolWindowMessages);
                    default -> label.setIcon(null);
                }
                return label;
            }
        };
    }

    @Contract(" -> new")
    public static @NotNull DefaultTableCellRenderer getSeparateTableCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 0) {
                    label.setIcon(AllIcons.Actions.InlayRenameInNoCodeFiles);
                } else {
                    label.setIcon(AllIcons.Toolwindows.ToolWindowMessages);
                }
                return label;
            }
        };
    }

    @Contract("_ -> param1")
    public static @NotNull TableRowSorter<TableModel> getOverViewTableSorter(@NotNull TableRowSorter<TableModel> sorter) {
        sorter.setComparator(1, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("x", ""))));

        sorter.setComparator(2, Comparator.comparingLong(a -> Long.parseLong(((String) a).replace("kb", "").replace(".", ""))));
        sorter.setComparator(3, Comparator.comparingLong(a -> Long.parseLong(((String) a).replace("kb", "").replace(".", ""))));
        sorter.setComparator(4, Comparator.comparingLong(a -> Long.parseLong(((String) a).replace("kb", "").replace(".", ""))));
        sorter.setComparator(5, Comparator.comparingLong(a -> Long.parseLong(((String) a).replace("kb", "").replace(".", ""))));

        sorter.setComparator(6, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(7, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(8, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(9, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(10, Comparator.comparingInt(a -> (Integer) a));
        return sorter;
    }

    @Contract("_ -> param1")
    public static @NotNull TableRowSorter<TableModel> getSeparateTabTableSorter(@NotNull TableRowSorter<TableModel> sorter) {
        sorter.setComparator(1, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(2, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(4, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(6, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(5, Comparator.comparingInt(a -> (Integer) a));

        sorter.setComparator(3, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("%", ""))));
        sorter.setComparator(7, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("%", ""))));
        return sorter;
    }

    @Contract("_ -> param1")
    public static @NotNull GridBagConstraints setMainTableConstraint(@NotNull GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        return gbc;
    }

    @Contract("_ -> param1")
    public static @NotNull GridBagConstraints setFooterTableConstraint(@NotNull GridBagConstraints gbc) {
        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    public static @NotNull ComboBox<String> getCharsetMenu() {
        var comboBox = new ComboBox<String>();
        comboBox.addItem(String.valueOf(StandardCharsets.UTF_8));
        comboBox.addItem(String.valueOf(StandardCharsets.US_ASCII));
        comboBox.addItem(String.valueOf(StandardCharsets.UTF_16));
        comboBox.addItem(String.valueOf(StandardCharsets.ISO_8859_1));
        comboBox.addItem(String.valueOf(StandardCharsets.UTF_16BE));

        return comboBox;
    }

    public static @NotNull ActionButton createButton(String text, String description, @NotNull Icon icon, Runnable action) {
        var anAction = new AnAction(text, description, icon) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                action.run();
            }
        };
        return new ActionButton(anAction, anAction.getTemplatePresentation(), text, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);
    }

    public static void createMainUI(@NotNull GridBagConstraints gbc, ActionButton refreshButton, ActionButton settingsButton, @NotNull JPanel mainPanel) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = JBUI.insets(2, 5, 0, 2);
        mainPanel.add(refreshButton, gbc);

        gbc.gridx = 1;
        gbc.insets = JBUI.insets(2, 1, 0, 2);
        mainPanel.add(settingsButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = JBUI.emptyInsets();
        mainPanel.add(CodeStatsWindow.TABBED_PANE, gbc);

        //To react to resize events for the TIME_LINE
        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                CodeStatsWindow.TIME_LINE.resizeTimer.restart();
            }
        });

        CodeStatsWindow.TABBED_PANE.addChangeListener(e -> {
            if (CodeStatsWindow.TABBED_PANE.getSelectedIndex() == 1) {
                CodeStatsWindow.TIME_LINE.refreshGraphic = true;
            }
        });
    }

    public static void addTimeLineButtons(@NotNull LineChartPanel lineChartPanel, @NotNull JBRadioButton commitPoints, @NotNull JBRadioButton genericPoints, @NotNull JBRadioButton codeLines, @NotNull JBRadioButton totalLines) {
        lineChartPanel.setLayout(null);
        codeLines.setSelected(true);
        genericPoints.setSelected(true);

        codeLines.addActionListener(e -> {
            lineChartPanel.lineMode = LineChartPanel.LineCountMode.CODE_LINES;
            lineChartPanel.refreshChart();
        });

        totalLines.addActionListener(e -> {
            lineChartPanel.lineMode = LineChartPanel.LineCountMode.TOTAL_LINES;
            lineChartPanel.refreshChart();
        });

        commitPoints.addActionListener(e -> {
            lineChartPanel.pointMode = LineChartPanel.TimePointMode.COMMIT;
            lineChartPanel.refreshChart();
        });

        genericPoints.addActionListener(e -> {
            lineChartPanel.pointMode = LineChartPanel.TimePointMode.GENERIC;
            lineChartPanel.refreshChart();
        });


        ButtonGroup lineModeGroup = new ButtonGroup();
        lineModeGroup.add(codeLines);
        lineModeGroup.add(totalLines);

        ButtonGroup pointModeGroup = new ButtonGroup();
        pointModeGroup.add(commitPoints);
        pointModeGroup.add(genericPoints);


        JLabel lineTypeLabel = new JLabel("Y-Axis:");
        lineTypeLabel.setBounds(10, 1, 70, 20);
        lineChartPanel.add(lineTypeLabel);

        codeLines.setBounds(70, 1, 107, 20);
        totalLines.setBounds(180, 1, 115, 20);
        lineChartPanel.add(codeLines);
        lineChartPanel.add(totalLines);

        JLabel pointTypeLabel = new JLabel("Data Points:");
        pointTypeLabel.setBounds(340, 1, 110, 20);
        lineChartPanel.add(pointTypeLabel);

        commitPoints.setBounds(440, 1, 80, 20);
        genericPoints.setBounds(522, 1, 90, 20);
        lineChartPanel.add(commitPoints);
        lineChartPanel.add(genericPoints);
    }
}
