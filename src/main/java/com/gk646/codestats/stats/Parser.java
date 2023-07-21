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
import com.gk646.codestats.tabs.IconCellRenderer;
import com.gk646.codestats.util.StringParsing;
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

import javax.swing.JTabbedPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

public class Parser {
    public String projectPathString;
    public Path projectPath;
    HashSet<String> excludedTypes = new HashSet<>(16);
    HashSet<String> excludedDirs = new HashSet<>(16);
    HashSet<String> separateTabs = new HashSet<>(16);
    HashMap<String, OverViewEntry> overView = new HashMap<>(10);
    HashMap<String, ArrayList<StatEntry>> tabs = new HashMap<>(6);

    public Parser(String path) {
        this.projectPath = Path.of(path);
        this.projectPathString = projectPath.toString();
        updateState();
    }

    public void updateState() {
        var save = Save.getInstance();
        overView.clear();
        excludedTypes.clear();
        excludedDirs.clear();

        Collections.addAll(excludedTypes, save.excludedFileTypes.split(";"));
        Collections.addAll(separateTabs, save.separateTabsTypes.split(";"));

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

    public void updatePane(JTabbedPane pane) {
        Task.Backgroundable task = new Task.Backgroundable(CodeStatsWindow.project, "Updating Code Stats", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                var time = System.currentTimeMillis();
                resetCache();
                iterateFiles();

                ApplicationManager.getApplication().invokeLater(() -> {
                    rebuildTabbedPane(pane);
                    var duration = System.currentTimeMillis() - time;
                    var notification = new Notification("CodeStats", "Code Stats", "Update completed in " + String.format("%.4f", duration / 1_000f) + " sec.", NotificationType.INFORMATION);
                    notification.setIcon(AllIcons.General.Information);
                    Notifications.Bus.notify(notification);
                });
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private void rebuildTabbedPane(JTabbedPane pane) {
        Object[][] data = new Object[overView.size()][];
        int i = 0;
        for (var pair : overView.entrySet()) {
            var entry = pair.getValue();
            data[i] = new Object[]{pair.getKey(), entry.count + "x", entry.sizeSum + "kb", entry.sizeMin + "kb", entry.sizeMax + "kb", entry.sizeAvg + "kb", entry.lines, entry.linesMin, entry.linesMax, entry.linesAvg, entry.linesCode};
            i++;
        }
        String[] columnNames = {"Extension", "Count", "Size SUM", "Size MIN", "Size MAX", "Size AVG", "Lines", "Lines MIN", "Lines MAX", "Lines AVG", "Lines CODE"};

        DefaultTableModel model = new DefaultTableModel(data, columnNames);
        var table = new JBTable(model);

        table.setEnableAntialiasing(true);
        table.setStriped(false);
        table.setBorder(null);
        table.getTableHeader().setReorderingAllowed(false);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);

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


        table.setRowSorter(sorter);
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            table.getColumnModel().getColumn(columnIndex).setCellRenderer(new IconCellRenderer());
        }

        pane.addTab("OverView", new JBScrollPane(table));

        for (var pair : tabs.entrySet()) {
            var fileList = pair.getValue();
            if (fileList.isEmpty()) continue;

            data = new Object[fileList.size()][];
            i = 0;
            for (var entry : fileList) {
                data[i] = new Object[]{entry.name, entry.totalLines, entry.sourceCodeLines, (int) (entry.sourceCodeLines * 100.0f / entry.totalLines + 0.5) + "%",
                        entry.commentLines, (int) (entry.commentLines * 100.0f / entry.totalLines + 0.5) + "%", entry.blankLines, (int) (entry.blankLines * 100.0f / entry.totalLines + 0.5) + "%"
                };
                i++;
            }
            columnNames = new String[]{"Source File", "Total Lines", "Source Code Lines", "Source Code Lines [%]", "Comment Lines", "Comment Lines[%]", "Blank LInes", "Blank Lines [%]"};


            model = new DefaultTableModel(data, columnNames);
            table = new JBTable(model);

            table.setEnableAntialiasing(true);
            table.setStriped(false);
            table.setBorder(null);
            table.getTableHeader().setReorderingAllowed(false);

            sorter = new TableRowSorter<>(model);

            sorter.setComparator(1, Comparator.comparingInt(a -> (Integer) a));
            sorter.setComparator(2, Comparator.comparingInt(a -> (Integer) a));
            sorter.setComparator(4, Comparator.comparingInt(a -> (Integer) a));
            sorter.setComparator(6, Comparator.comparingInt(a -> (Integer) a));

            sorter.setComparator(3, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("%", ""))));
            sorter.setComparator(5, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("%", ""))));
            sorter.setComparator(7, Comparator.comparingInt(a -> Integer.parseInt(((String) a).replace("%", ""))));

            table.setRowSorter(sorter);
            pane.addTab(pair.getKey(), new JBScrollPane(table));
        }
    }

    private void parseFile(File file, String extension) {
        overView.computeIfAbsent(extension, k -> new OverViewEntry());

        if (separateTabs.contains(extension)) {
            ArrayList<StatEntry> files = tabs.get(extension);
            var entry = new StatEntry(file.getName());
            try {
                String content = Files.readString(file.toPath());

                String[] lines = content.split("\n");
                entry.totalLines += lines.length;
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        entry.blankLines++;
                    } else if (line.startsWith("//") || line.startsWith("/*") || line.endsWith("*/") || line.startsWith("*")) {
                        entry.commentLines++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            entry.sourceCodeLines = entry.totalLines - entry.blankLines - entry.commentLines;

            var overview = overView.get(extension);
            overview.count++;
            int size = (int) (file.length() / 1000);
            overview.sizeSum += size;
            if (size < overview.sizeMin) {
                overview.sizeMin = size;
            }
            if (size > overview.sizeMax) {
                overview.sizeMax = size;
            }

            overview.lines += entry.totalLines;
            if (entry.totalLines < overview.linesMin) {
                overview.linesMin = entry.totalLines;
            }
            if (entry.totalLines > overview.linesMax) {
                overview.linesMax = entry.totalLines;
            }
            overview.linesCode += entry.sourceCodeLines;

            files.add(entry);
        } else {
            try {
                var overview = overView.get(extension);
                overview.count++;

                int lines = 0;
                int size = (int) (file.length() / 1000);

                if (size > 50000) {
                    try (Stream<String> lineStream = Files.lines(file.toPath())) {
                        lines = (int) lineStream.count();
                    } catch (UncheckedIOException | IOException ignored) {
                    }
                } else {
                    try {
                        lines = Files.readString(file.toPath()).split("\n").length;
                    } catch (MalformedInputException ignored) {
                    }
                }

                overview.lines += lines;
                if (lines < overview.linesMin) {
                    overview.linesMin = lines;
                }
                if (lines > overview.linesMax) {
                    overview.linesMax = lines;
                }
                overview.linesCode += lines;


                overview.sizeSum += size;
                if (size < overview.sizeMin) {
                    overview.sizeMin = size;
                }
                if (size > overview.sizeMax) {
                    overview.sizeMax = size;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                    var file = path.toFile();
                    String extension = StringParsing.getFileExtension(file.getName());
                    if (!extension.isEmpty() && !excludedTypes.contains(extension)) {
                        parseFile(file, extension);
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
            tabs.put(tab, new ArrayList<>());
        }
    }
}
