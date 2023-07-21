package com.gk646.codestats.tabs;

import com.intellij.icons.AllIcons;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

public class IconCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        switch (column) {
            case 0 -> label.setIcon(AllIcons.Actions.InlayRenameInNoCodeFiles);
            case 2, 3, 4, 5 -> label.setIcon(AllIcons.Actions.Annotate);
            case 6, 7, 8, 9 -> label.setIcon(AllIcons.Toolwindows.ToolWindowMessages);
            default -> label.setIcon(null);
        }
        return label;
    }
}
