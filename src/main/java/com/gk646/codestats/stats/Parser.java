/*
 * MIT License
 *
 * Copyright (c) 2024 gk646
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
import com.gk646.codestats.settings.PersistentSave;
import com.gk646.codestats.settings.SettingsPanel;
import com.gk646.codestats.ui.LineChartPanel;
import com.gk646.codestats.ui.UIHelper;
import com.gk646.codestats.util.BoolContainer;
import com.gk646.codestats.util.IntellijUtil;
import com.gk646.codestats.util.ParsingUtil;
import com.gk646.codestats.util.TimePoint;
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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Handles all the parsing of source and non-source files.
 * Also, responsible currently for rebuilding the tables in {@link #rebuildTabbedPane()}. <br>
 * This class is still quite messy.
 */
@SuppressWarnings("DialogTitleCapitalization")
public final class Parser {
    private static final HashSet<String> excludedTypes = new HashSet<>(16, 1);
    private static final HashSet<String> excludedDirs = new HashSet<>(16, 1);
    public static final HashSet<String> separateTabs = new HashSet<>(16, 1);
    private static final HashSet<String> whiteListTypes = new HashSet<>(16, 1);
    public static final HashMap<String, OverViewEntry> overView = new HashMap<>(10);
    public static final HashMap<String, ArrayList<StatEntry>> tabs = new HashMap<>(6);
    private final FileVisitor<Path> visitor;
    public AtomicBoolean isUpdating = new AtomicBoolean(false);
    public boolean commitHappened = false;
    private boolean countMiscLines = false;
    public String commitText;
    public Path projectPath;
    Charset charset = StandardCharsets.UTF_8;
    private DefaultTableModel overviewModel;
    private DefaultTableModel footerModel;
    private JBTable overviewTable;
    private JBTable footerTable;

    public Parser() {
        visitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) {
                if (excludedDirs.contains(path.toString())) {
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
        };
       initializeOverviewTab(); // Comment this when testing - haha this code is a hot mess ... :(.
    }

    private void initializeOverviewTab() {
        String[] columnNames = {"Extension", "Count", "Size SUM", "Size MIN", "Size MAX", "Size AVG", "Lines", "Lines MIN", "Lines MAX", "Lines AVG", "Lines CODE"};
        overviewModel = new DefaultTableModel(columnNames, 0);
        overviewTable = new JBTable(overviewModel);
        setupTable(overviewTable, UIHelper.OverViewTableCellRenderer, UIHelper.getOverViewTableSorter(new TableRowSorter<>(overviewModel)));

        footerModel = new DefaultTableModel(new String[]{"", "", "", "", "", "", "", "", "", "", ""}, 1);
        footerTable = new JBTable(footerModel);
        setupFooter(footerTable);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panel.add(new JBScrollPane(overviewTable), UIHelper.setMainTableConstraint(gbc));
        panel.add(footerTable, UIHelper.setFooterTableConstraint(gbc));

        CodeStatsWindow.TABBED_PANE.addTab("OverView", AllIcons.Nodes.HomeFolder, panel);
    }

    private void setupTable(@NotNull JBTable table, TableCellRenderer renderer, RowSorter<? extends javax.swing.table.TableModel> sorter) {
        table.setDefaultEditor(Object.class, null);
        table.setEnableAntialiasing(true);
        table.setStriped(false);
        table.setBorder(null);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, renderer);
        table.setRowSorter(sorter);
    }

    private void setupFooter(@NotNull JBTable footer) {
        footer.setStriped(false);
        footer.setDefaultEditor(Object.class, null);
        footer.setDefaultRenderer(Object.class, UIHelper.getBoldRenderer());
    }

    public void updateState() {
        var save = PersistentSave.getInstance();
        excludedTypes.clear();
        excludedDirs.clear();
        separateTabs.clear();
        whiteListTypes.clear();

        Collections.addAll(excludedTypes, save.excludedFileTypes.split(";"));
        Collections.addAll(separateTabs, save.separateTabsTypes.split(";"));

        if (!save.includedFileTypes.isEmpty()) {
            Collections.addAll(whiteListTypes, save.includedFileTypes.split(";"));
        }

        for (var dir : save.excludedDirectories) {
            //to always get correct file separators
            excludedDirs.add(Path.of(dir).toString());
        }

        //checkbox options
        if (save.isExcludeIDE) {
            excludedDirs.add(projectPath + File.separator + ".idea");
            excludedDirs.add(projectPath + File.separator + ".vs");
            excludedDirs.add(projectPath + File.separator + ".settings");
            excludedDirs.add(projectPath + File.separator + ".project");
            excludedDirs.add(projectPath + File.separator + ".classpath");
        }

        if (save.isExcludeNPM) {
            excludedDirs.add(projectPath + File.separator + "node_modules");
            excludedDirs.add(projectPath + File.separator + ".docker");
        }

        if (save.excludeCompiler) {
            excludedDirs.add(projectPath + File.separator + "out");
            excludedDirs.add(projectPath + File.separator + "build");
            excludedDirs.add(projectPath + File.separator + ".gradle");
            excludedDirs.add(projectPath + File.separator + "target");
            excludedDirs.add(projectPath + File.separator + "cmake-build-debug");
            excludedDirs.add(projectPath + File.separator + "cmake-build-release");
            excludedDirs.add(projectPath + File.separator + "cmake-build-Release");
            excludedDirs.add(projectPath + File.separator + "cmake-build-Debug");
            excludedDirs.add(projectPath + File.separator + "dist");
            excludedDirs.add(projectPath + File.separator + "bin");
            excludedDirs.add(projectPath + File.separator + "obj");
        }

        if (save.excludeGit) {
            excludedDirs.add(projectPath + File.separator + ".git");
            excludedDirs.add(projectPath + File.separator + "gitignore");
            excludedDirs.add(projectPath + File.separator + ".svn");
            excludedDirs.add(projectPath + File.separator + ".hg");
        }

        if (save.isExcludeCache) {
            excludedDirs.add(projectPath + File.separator + ".cache");
            excludedDirs.add(projectPath + File.separator + "tmp");
            excludedDirs.add(projectPath + File.separator + "temp");
        }

        if (save.isExcludePython) {
            excludedDirs.add(projectPath + File.separator + "venv");
            excludedDirs.add(projectPath + File.separator + "env");
            excludedDirs.add(projectPath + File.separator + ".env");
        }

        charset = ParsingUtil.getCharsetFallback(save.charSet, StandardCharsets.UTF_8);
        countMiscLines = save.countMiscLines;
    }

    public void updatePane(boolean isSilentUpdate, Path path) {
        Task.Backgroundable task = new Task.Backgroundable(CodeStatsWindow.project, "Updating Code Stats", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                var time = System.currentTimeMillis();
                resetCache();
                iterateFiles(path);
                ApplicationManager.getApplication().invokeLater(() -> {
                    rebuildTabbedPane(path);
                    isUpdating.set(false);
                    if (isSilentUpdate) return;
                    var duration = System.currentTimeMillis() - time;
                    var notification = new Notification("CodeStats", "Code Stats", "Update completed in " + String.format("%d", duration) + " ms.", NotificationType.INFORMATION);
                    notification.setIcon(AllIcons.General.Information);
                    Notifications.Bus.notify(notification);
                });
            }
        };
        ProgressManager.getInstance().run(task);
    }

    private void rebuildTabbedPane(Path path) {
        Locale germanLocale = Locale.GERMAN;
        DecimalFormat df = new DecimalFormat("#,###kb", new DecimalFormatSymbols(germanLocale));

        var data = new Object[overView.size()][];
        var footerData = new Object[][]{{"Total:", 0, 0L, 0L, 0L, 0L, 0, 0, 0, 0, 0}};
        int i = 0;
        for (var pair : overView.entrySet()) {
            var entry = pair.getValue();
            long sizeAvg = entry.sizeSum / entry.count / 1000;
            data[i] = new Object[]{
                    pair.getKey(),
                    entry.count + "x",
                    df.format(entry.sizeSum / 1000),
                    df.format(entry.sizeMin / 1000),
                    df.format(entry.sizeMax / 1000),
                    df.format(sizeAvg),
                    entry.lines,
                    entry.linesMin,
                    entry.linesMax,
                    entry.lines / entry.count,
                    entry.linesCode
            };
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

        overviewModel.setDataVector(data, new String[]{"Extension", "Count", "Size SUM", "Size MIN", "Size MAX", "Size AVG", "Lines", "Lines MIN", "Lines MAX", "Lines AVG", "Lines CODE"});
        setupTable(overviewTable, UIHelper.OverViewTableCellRenderer, UIHelper.getOverViewTableSorter(new TableRowSorter<>(overviewModel)));
        footerModel.setDataVector(footerData, new String[]{"", "", "", "", "", "", "", "", "", "", ""});


        //Adds the new timeline tab as the second tab ONLY IF it's a normal refresh
        if (path.equals(projectPath)) {
            handleTimelineTab((int) footerData[0][10], (int) footerData[0][6]);
        } else {
            CodeStatsWindow.TABBED_PANE.addTab("Refresh to reset view", AllIcons.Actions.QuickfixBulb, new JTabbedPane());
        }
        //Build the separate tabs
        buildSeparateTabs();
    }

    private void handleTimelineTab(int linesCode, int totalLines) {
        if (!SettingsPanel.disableTimeLine.isSelected()) {
            long currentTimeMillis = ZonedDateTime.now().toInstant().toEpochMilli();

            String formattedDate = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()));

            if (commitHappened) {
                PersistentSave.addTimePoint(LineChartPanel.TimePointMode.COMMIT, new TimePoint(currentTimeMillis, linesCode, totalLines, commitText));
                commitHappened = false;
            }
            PersistentSave.addTimePoint(LineChartPanel.TimePointMode.GENERIC, new TimePoint(currentTimeMillis, linesCode, totalLines, formattedDate));
            CodeStatsWindow.TABBED_PANE.addTab("TimeLine", AllIcons.Nodes.PpLib, CodeStatsWindow.TIME_LINE);
        }
    }

    private void buildSeparateTabs() {
        var columnNames = new String[]{"Source File", "Total Lines", "Source Code Lines", "Source Code Lines [%]", "Comment Lines", "Documentation Lines", "Blank Lines", "Blank Lines [%]"};

        for (var pair : tabs.entrySet()) {
            var fileList = pair.getValue();
            if (fileList.isEmpty()) continue;

            var data = new Object[fileList.size()][];
            var footerTotals = new int[]{0, 0, 0, 0, 0}; // Total Lines, Source Code Lines, Comment Lines, Blank Lines
            int i = 0;

            for (var entry : fileList) {
                data[i++] = new Object[]{entry.name,
                        entry.totalLines,
                        entry.sourceCodeLines,
                        (int) (entry.sourceCodeLines * 100.0f / entry.totalLines + 0.5) + "%",
                        entry.commentLines,
                        entry.docLines, entry.blankLines,
                        (int) (entry.blankLines * 100.0f / entry.totalLines + 0.5) + "%"};

                footerTotals[0] += entry.totalLines;
                footerTotals[1] += entry.sourceCodeLines;
                footerTotals[2] += entry.commentLines;
                footerTotals[3] += entry.docLines;
                footerTotals[4] += entry.blankLines;
            }

            var footerData = new Object[][]{
                    {"Total:",
                            footerTotals[0],
                            footerTotals[1],
                            String.format("%d%%", (int) (100 * (footerTotals[1] / (float) footerTotals[0]))),
                            footerTotals[2],
                            footerTotals[3],
                            footerTotals[4],
                            String.format("%d%%", (int) (100 * (footerTotals[4] / (float) footerTotals[0])))
                    }
            };

            createAndAddTab(pair.getKey(), data, footerData, columnNames);
        }
    }

    private void createAndAddTab(String tabName, Object[][] data, Object[][] footerData, String[] columnNames) {
        var tabTableModel = new DefaultTableModel(data, columnNames);
        var table = new JBTable(tabTableModel);
        setupTable(table, UIHelper.SeparateTableCellRenderer, UIHelper.getSeparateTabTableSorter(new TableRowSorter<>(tabTableModel)));

        var tabFooterModel = new DefaultTableModel(footerData, new String[]{"", "", "", "", "", "", "", ""});
        var tabFooterTable = new JBTable(tabFooterModel);
        setupFooter(tabFooterTable);

        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        panel.add(new JBScrollPane(table), UIHelper.setMainTableConstraint(gbc));
        panel.add(tabFooterTable, UIHelper.setFooterTableConstraint(gbc));

        // Mouse listener for opening files - only for separate tabs as these show file stats
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    if (row != -1) {
                        // Assuming the first column contains the file path or name
                        String filePath = (String) table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
                        IntellijUtil.openFileInEditor(filePath, table);
                    }
                }
            }
        });

        CodeStatsWindow.TABBED_PANE.addTab(tabName, AllIcons.General.ArrowSplitCenterH, panel);
    }

    public void parseFile(Path path, String extension) {
        overView.computeIfAbsent(extension, k -> new OverViewEntry());

        if (separateTabs.contains(extension)) {
            var entry = new StatEntry(path.getFileName().toString());
            long size = 0;
            final int[] miscLines = {0}; // New concept - misc lines are deducted from source code lines as well but not shown
            var stateFlags = new BoolContainer(); // first = multiline comment / second = multiline doc
            try {
                size = Files.size(path);
                try (Stream<String> linesStream = Files.newBufferedReader(path, charset).lines()) {
                    linesStream.forEach(line -> {
                        line = line.trim();
                        if (stateFlags.multiLineCommentJava || stateFlags.multiLineDocJava || stateFlags.multiLineLIneDocPython) {
                            if (stateFlags.multiLineDocJava && line.startsWith("*")) {
                                entry.docLines++;
                            }
                            if (stateFlags.multiLineCommentJava) {
                                entry.commentLines++;
                            }
                            if(stateFlags.multiLineLIneDocPython){
                                entry.docLines++;
                            }
                            if (line.contains("*/")) {
                                stateFlags.multiLineCommentJava = false;
                                stateFlags.multiLineDocJava = false;
                            }else if(line.contains("\"\"\"")) {
                                stateFlags.multiLineLIneDocPython = false;
                            }
                        } else {
                            if (line.isEmpty()) {
                                entry.blankLines++;
                            } else if (line.startsWith("//") || line.startsWith("#") || line.startsWith("--")) {
                                entry.commentLines++;
                            } else if (line.startsWith("/*")) {
                                stateFlags.multiLineCommentJava = true;
                                entry.commentLines++;
                                if (line.startsWith("/**")) {
                                    stateFlags.multiLineDocJava = true;
                                    stateFlags.multiLineCommentJava = false;
                                    entry.commentLines--;
                                    entry.docLines++;
                                }
                            }else if(line.startsWith("\"\"\"")){
                                stateFlags.multiLineLIneDocPython= true;
                                if(line.contains("\"\"\"")){
                                    entry.docLines++;
                                    stateFlags.multiLineLIneDocPython = false;
                                }
                            }
                            else if (line.startsWith("import") || line.startsWith("#include") || line.startsWith("package") || line.startsWith("from")) {
                                miscLines[0]++;
                            }
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
                } catch (IOException ignored) {
                }
            }
            //setting separate tab entry data
            entry.sourceCodeLines = entry.totalLines - entry.blankLines - entry.commentLines - entry.docLines;
            if(!countMiscLines) entry.sourceCodeLines-= miscLines[0];

            //setting over view entry data
            overView.get(extension).addValues(size, entry.totalLines, entry.sourceCodeLines);

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
            overView.get(extension).addValues(size, lines, 0);
        }
    }

    private void iterateFiles(Path path) {
        try {
            Files.walkFileTree(path, visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetCache() {
        overView.clear();
        tabs.clear();
        for (var tab : separateTabs) {
            tabs.put(tab, new ArrayList<>());
        }
    }
}
