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

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.util.Comparator;

public class UIHelper {

    public static final DefaultTableCellRenderer OverViewTableCellRenderer = getIconRenderer();
    public static final DefaultTableCellRenderer SeparateTableCellRenderer = getSeparateTableCellRenderer();

    private UIHelper() {
    }

    public static TableCellRenderer getBoldRendere() {
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

    public static DefaultTableCellRenderer getIconRenderer() {
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

    public static DefaultTableCellRenderer getSeparateTableCellRenderer() {
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

    public static TableRowSorter<TableModel> getOverViewTableSorter(TableRowSorter<TableModel> sorter) {
        sorter.setComparator(1, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("x", ""))));
        sorter.setComparator(2, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("kb", ""))));
        sorter.setComparator(3, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("kb", ""))));
        sorter.setComparator(4, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("kb", ""))));
        sorter.setComparator(5, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("kb", ""))));

        sorter.setComparator(6, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(7, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(8, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(9, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(10, Comparator.comparingInt(a -> (Integer) a));
        return sorter;
    }

    public static TableRowSorter<TableModel> getSeparateTabTableSorter(TableRowSorter<TableModel> sorter) {
        sorter.setComparator(1, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(2, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(4, Comparator.comparingInt(a -> (Integer) a));
        sorter.setComparator(6, Comparator.comparingInt(a -> (Integer) a));

        sorter.setComparator(3, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("%", ""))));
        sorter.setComparator(5, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("%", ""))));
        sorter.setComparator(7, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("%", ""))));
        return sorter;
    }


    public static GridBagConstraints setMainTableConstraint(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        return gbc;
    }

    public static GridBagConstraints setFooterTableConstraint(GridBagConstraints gbc) {
        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }
}
