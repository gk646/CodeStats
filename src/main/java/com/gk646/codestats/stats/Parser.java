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

package com.gk646.codestats.stats;

import com.gk646.codestats.CodeStatsWindow;
import com.gk646.codestats.settings.Save;
import com.gk646.codestats.ui.UIHelper;
import com.gk646.codestats.util.BoolContainer;
import com.gk646.codestats.util.ParsingUtil;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.stream.Stream;

@SuppressWarnings("DialogTitleCapitalization")
public final class Parser {
    final HashSet<String> excludedTypes = new HashSet<>(16, 1);
    final HashSet<String> excludedDirs = new HashSet<>(16, 1);
    final HashSet<String> separateTabs = new HashSet<>(16, 1);
    final HashSet<String> whiteListTypes = new HashSet<>(16, 1);
    final HashMap<String, OverViewEntry> overView = new HashMap<>(10);
    final HashMap<String, ArrayList<StatEntry>> tabs = new HashMap<>(6);
    final Path projectPath;

    public Parser(String path) {
        this.projectPath = Path.of(path);
        updateState();
    }

    public void updateState() {
        var save = Save.getInstance();
        overView.clear();
        separateTabs.clear();
        excludedTypes.clear();
        whiteListTypes.clear();

        Collections.addAll(excludedTypes, save.excludedFileTypes.split(";"));
        Collections.addAll(separateTabs, save.separateTabsTypes.split(";"));

        if (!save.includedFileTypes.isEmpty()) {
            Collections.addAll(whiteListTypes, save.includedFileTypes.split(";"));
        }

        for (var dir : save.excludedDirectories) {
            excludedDirs.add(Path.of(dir).toString());
        }

        if (save.exclude_idea) {
            excludedDirs.add(projectPath + File.separator + ".idea");
        }
        if (save.exclude_npm) {
            excludedDirs.add(projectPath + File.separator + "node_modules");
        }
        if (save.exclude_compiler) {
            excludedDirs.add(projectPath + File.separator + "out");
            excludedDirs.add(projectPath + File.separator + "cmake-build-debug");
            excludedDirs.add(projectPath + File.separator + "cmake-build-release");
        }
        if (save.exclude_git) {
            excludedDirs.add(projectPath + File.separator + ".git");
        }
    }

    public void updatePane() {
        Task.Backgroundable task = new Task.Backgroundable(CodeStatsWindow.project, "Updating Code Stats", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                var time = System.currentTimeMillis();
                resetCache();
                iterateFiles();
                ApplicationManager.getApplication().invokeLater(() -> {
                    rebuildTabbedPane();
                    var duration = System.currentTimeMillis() - time;
                    var notification = new Notification("CodeStats", "Code Stats", "Update completed in " + String.format("%.4f", duration / 1_000f) + " sec.", NotificationType.INFORMATION);
                    notification.setIcon(AllIcons.General.Information);
                    Notifications.Bus.notify(notification);
                });
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private void rebuildTabbedPane() {
        Object[][] data = new Object[overView.size()][];
        Object[][] footerData = new Object[][]{{"Total:", 0, 0L, 0L, 0L, 0L, 0, 0, 0, 0, 0}};
        int i = 0;
        for (var pair : overView.entrySet()) {
            var entry = pair.getValue();
            data[i] = new Object[]{pair.getKey(),
                    entry.count + "x",
                    String.format(Locale.GERMAN, "%,d", entry.sizeSum / 1000) + "kb",
                    String.format(Locale.GERMAN, "%,d", entry.sizeMin / 1000) + "kb",
                    String.format(Locale.GERMAN, "%,d", entry.sizeMax / 1000) + "kb",
                    String.format(Locale.GERMAN, "%,d", (entry.sizeSum / entry.count) / 1000) + "kb",
                    entry.lines,
                    entry.linesMin,
                    entry.linesMax,
                    entry.lines / entry.count,
                    entry.linesCode};
            footerData[0][1] = (int) footerData[0][1] + entry.count;
            footerData[0][2] = (long) footerData[0][2] + entry.sizeSum / 1000;
            footerData[0][3] = (long) footerData[0][3] + entry.sizeMin / 1000;
            footerData[0][4] = (long) footerData[0][4] + entry.sizeMax / 1000;
            footerData[0][5] = (long) footerData[0][5] + (entry.sizeSum / entry.count) / 1000;
            footerData[0][6] = (int) footerData[0][6] + entry.lines;
            footerData[0][7] = (int) footerData[0][7] + entry.linesMin;
            footerData[0][8] = (int) footerData[0][8] + entry.linesMax;
            footerData[0][9] = (int) footerData[0][9] + entry.lines / entry.count;
            footerData[0][10] = (int) footerData[0][10] + entry.linesCode;
            i++;
        }

        String[] columnNames = {"Extension", "Count", "Size SUM", "Size MIN", "Size MAX", "Size AVG", "Lines", "Lines MIN", "Lines MAX", "Lines AVG", "Lines CODE"};

        var model = new DefaultTableModel(data, columnNames);
        var table = new JBTable(model);

        //table ui settings
        table.setDefaultEditor(Object.class, null);
        table.setEnableAntialiasing(true);
        table.setStriped(false);
        table.setBorder(null);
        table.getTableHeader().setReorderingAllowed(false);

        //creating the sorting
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(UIHelper.getOverViewTableSorter(sorter));

        //set table renderer
        table.setDefaultRenderer(Object.class, UIHelper.OverViewTableCellRenderer);

        //creating the footer
        model = new DefaultTableModel(footerData, new String[]{"", "", "", "", "", "", "", "", "", "", ""});
        var footer = new JBTable(model);

        //ui settings footer
        footer.setDefaultEditor(Object.class, null);
        footer.setDefaultRenderer(Object.class, UIHelper.getBoldRendere());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Add main table
        panel.add(new JBScrollPane(table), UIHelper.setMainTableConstraint(gbc));

        // Add footer table
        panel.add(footer, UIHelper.setFooterTableConstraint(gbc));
        CodeStatsWindow.tabbedPane.addTab("OverView", AllIcons.Nodes.HomeFolder, panel);

        //build tabs
        for (var pair : tabs.entrySet()) {
            var fileList = pair.getValue();
            if (fileList.isEmpty()) continue; //skip on empty separate tab type

            data = new Object[fileList.size()][];
            footerData = new Object[][]{{"Total:", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
            i = 0;
            for (var entry : fileList) {
                data[i] = new Object[]{entry.name, entry.totalLines, entry.sourceCodeLines, (int) (entry.sourceCodeLines * 100.0f / entry.totalLines + 0.5) + "%", entry.commentLines, entry.docLines, entry.blankLines, (int) (entry.blankLines * 100.0f / entry.totalLines + 0.5) + "%"};
                footerData[0][1] = (int) footerData[0][1] + entry.totalLines;
                footerData[0][2] = (int) footerData[0][2] + entry.sourceCodeLines;
                footerData[0][3] = (int) footerData[0][3] + (int) (entry.sourceCodeLines * 100.0f / entry.totalLines + 0.5);
                footerData[0][4] = (int) footerData[0][4] + entry.commentLines;
                footerData[0][5] = (int) footerData[0][5] + entry.docLines;
                footerData[0][6] = (int) footerData[0][6] + entry.blankLines;
                footerData[0][7] = (int) footerData[0][7] + (int) (entry.blankLines * 100.0f / entry.totalLines + 0.5);
                i++;
            }
            //get average percentage
            footerData[0][3] = (int) footerData[0][3] / fileList.size();
            footerData[0][7] = (int) footerData[0][7] / fileList.size();


            columnNames = new String[]{"Source File", "Total Lines", "Source Code Lines", "Source Code Lines [%]", "Comment Lines", "Documentation Lines", "Blank LInes", "Blank Lines [%]"};

            model = new DefaultTableModel(data, columnNames);
            table = new JBTable(model);

            //main table ui settings
            table.setDefaultEditor(Object.class, null);
            table.setEnableAntialiasing(true);
            table.setStriped(false);
            table.setBorder(null);
            table.getTableHeader().setReorderingAllowed(false);

            //table sorter
            sorter = new TableRowSorter<>(model);
            table.setRowSorter(UIHelper.getSeparateTabTableSorter(sorter));

            table.setDefaultRenderer(Object.class, UIHelper.SeparateTableCellRenderer);

            //create footer
            model = new DefaultTableModel(footerData, new String[]{"", "", "", "", "", "", "", ""});
            footer = new JBTable(model);

            //footer ui settings
            footer.setDefaultEditor(Object.class, null);
            footer.setDefaultRenderer(Object.class, UIHelper.getBoldRendere());


            panel = new JPanel(new GridBagLayout());
            gbc = new GridBagConstraints();

            //Add main table
            panel.add(new JBScrollPane(table), UIHelper.setMainTableConstraint(gbc));

            //Add footer table
            panel.add(footer, UIHelper.setFooterTableConstraint(gbc));
            CodeStatsWindow.tabbedPane.addTab(pair.getKey(), AllIcons.General.ArrowSplitCenterH, panel);
        }
    }

    private void parseFile(Path path, String extension) {
        overView.computeIfAbsent(extension, k -> new OverViewEntry());

        if (separateTabs.contains(extension)) {
            //getting correct entry
            var entry = new StatEntry(path.getFileName().toString());
            long size = 0;
            var bool = new BoolContainer();
            try {
                size = Files.size(path);
                try (Stream<String> linesStream = Files.newBufferedReader(path).lines()) {
                    linesStream.forEach(line -> {
                        line = line.trim();
                        if (line.isEmpty()) {
                            entry.blankLines++;
                        } else if (line.startsWith("//")) {
                            entry.commentLines++;
                        } else if (line.startsWith("/*")) {
                            bool.first = true;
                            entry.commentLines++;
                            if (line.startsWith("/**")) {
                                bool.second = true;
                                bool.first = false;
                                entry.commentLines--;
                                entry.docLines++;
                            }
                        } else if (bool.first || bool.second) {
                            if (bool.second && line.startsWith("*")) {
                                entry.docLines++;
                            }
                            if (line.contains("*/")) {
                                bool.first = false;
                                bool.second = false;
                            }
                        }
                        if (bool.first) {
                            entry.commentLines++;
                        }
                        entry.totalLines++;
                    });
                }
            } catch (UncheckedIOException | IOException e) {
                try {
                    if (size > 50000000) {
                        entry.totalLines = ParsingUtil.parseLargeNonUTFFile(path);
                    } else {
                        entry.totalLines = ParsingUtil.parseSmallNonUTFFile(path);
                    }
                } catch (IOException ignored1) {
                }
            }

            //setting separate tab entry data
            entry.sourceCodeLines = entry.totalLines - entry.blankLines - entry.commentLines;

            //setting over view entry data
            overView.get(extension).setValues(size, entry.totalLines, entry.sourceCodeLines);

            tabs.get(extension).add(entry);
        } else {

            int lines = 0;
            long size = 0;
            try {
                size = Files.size(path);
                if (size > 50000000) {
                    lines = ParsingUtil.parseLargeNonUTFFile(path);
                } else {
                    lines = ParsingUtil.parseSmallNonUTFFile(path);
                }
            } catch (IOException ignored) {
            }


            //setting overview entry data
            overView.get(extension).setValues(size, lines, 0);
        }
    }

    private void iterateFiles() {
        try {
            Files.walkFileTree(projectPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (excludedDirs.contains(dir.toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    String extension = ParsingUtil.getFileExtension(path.getFileName().toString());
                    if (whiteListTypes.isEmpty()) {
                        if (!extension.isEmpty() && !excludedTypes.contains(extension)) {
                            parseFile(path, extension);
                        }
                    } else if (whiteListTypes.contains(extension)) {
                        parseFile(path, extension);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetCache() {
        overView.clear();
        tabs.clear();

        for (final var tab : separateTabs) {
            tabs.put(tab, new ArrayList<>(16));
        }
    }
}
