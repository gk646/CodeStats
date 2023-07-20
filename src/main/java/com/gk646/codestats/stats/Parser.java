package com.gk646.codestats.stats;

import com.gk646.codestats.settings.Save;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.table.JBTable;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Parser {
    public ProjectFileIndex projectIndex;
    HashMap<String, OverViewEntry> overView = new HashMap<>(6);
    ArrayList<HashMap<String, StatEntry>> tabs = new ArrayList<>(6);

    public Parser(ProjectFileIndex pfi) {
        this.projectIndex = pfi;
        for (var ext : Save.getInstance().separateTabsTypes.split(";")) {
            overView.put(ext, new OverViewEntry());
        }
    }

    public void update(JTabbedPane pane) {
        iterateFiles();
        rebuildTabbedPane(pane);
    }

    private void rebuildTabbedPane(JTabbedPane pane) {
        Object[][] data = new Object[overView.size()][];
        int i = 0;
        for (var pair : overView.entrySet()) {
            var entry = pair.getValue();
            data[i] = new Object[]{pair.getKey(), entry.count, entry.linesCode};
        }
        String[] columnNames = {"Extension", "Count", "Lines"};
        JTable table = new JBTable(new DefaultTableModel(data, columnNames));
        pane.addTab("OverView", new JScrollPane(table));
        /*
        for (StatEntry statEntry : tabs) {
            Object[][] data = {
                    {"Total Lines", statEntry.totalLines()},
                    {"Source Code Lines", statEntry.sourceCodeLines()},
                    {"Comment Lines", statEntry.commentLines()},
                    {"Blank Lines", statEntry.blankLines()}
            };
            String[] columnNames = {"Stat", "Value"};

            JTable table = new JBTable(new DefaultTableModel(data, columnNames));

            pane.addTab(statEntry.name(), new JScrollPane(table));
        }*/
    }

    private void parseFile(VirtualFile file) {
        try {
            System.out.println(file.getExtension());
            var overview = overView.get(file.getExtension());
            overview.count++;

            byte[] bytes = file.contentsToByteArray();
            String content = new String(bytes, file.getCharset());

            String[] lines = content.split("\n");
            for (String line : lines) {
                overview.lines++;
                if (line.trim().isEmpty()) {
                } else if (line.trim().startsWith("//")) {
                    System.out.println("Single line comment found");
                } else if (line.trim().startsWith("/*") && line.trim().endsWith("*/")) {
                    System.out.println("Block comment found");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void iterateFiles() {
        final List<String> excludedTypes = Save.getInstance().excludedDirectories;
        projectIndex.iterateContent(file -> {
            System.out.println(file.getName());
            parseFile(file);
            return true;
        }, file -> file.isDirectory() && !excludedTypes.contains(file.getExtension()));
    }
}
