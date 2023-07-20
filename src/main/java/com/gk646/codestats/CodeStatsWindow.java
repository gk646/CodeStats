package com.gk646.codestats;

import com.gk646.codestats.stats.StatEntry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class CodeStatsWindow implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull com.intellij.openapi.wm.ToolWindow toolWindow) {
        // Create a new JTabbedPane.
        JTabbedPane tabbedPane = new JBTabbedPane();

        // For each set of code stats, create a new JTable and add it to the JTabbedPane.
        List<StatEntry> codeStats = List.of(new StatEntry("first", 100, 10, 2, 2)); // Replace this with your method to calculate the code stats.

        for (StatEntry statEntry : codeStats) {
            // Create the data for the JTable.
            Object[][] data = {
                    {"Total Lines", statEntry.totalLines()},
                    {"Source Code Lines", statEntry.sourceCodeLines()},
                    {"Comment Lines", statEntry.commentLines()},
                    {"Blank Lines", statEntry.blankLines()}
            };
            String[] columnNames = {"Stat", "Value"};

            // Create the JTable.
            JTable table = new JBTable(new DefaultTableModel(data, columnNames));

            // Add the JTable to the JTabbedPane.
            tabbedPane.addTab(statEntry.name(), new JScrollPane(table));
        }


        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(tabbedPane, "CodeStats", true);

        toolWindow.getContentManager().addContent(content);
    }
}
