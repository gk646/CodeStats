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

package com.gk646.codestats;

import com.gk646.codestats.settings.Save;
import com.gk646.codestats.settings.Settings;
import com.gk646.codestats.stats.Parser;
import com.gk646.codestats.ui.LineChartPanel;
import com.gk646.codestats.ui.UIHelper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.nio.file.Path;
import java.util.Objects;

public final class CodeStatsWindow implements ToolWindowFactory, ToolWindowManagerListener, DumbAware, StartupActivity.DumbAware {
    public static final JTabbedPane TABBED_PANE = new JBTabbedPane();
    public static final LineChartPanel TIME_LINE = new LineChartPanel(TABBED_PANE);
    public static final Parser PARSER = new Parser();
    public static Project project;

    @Override
    public void runActivity(@NotNull Project project) {
        //Added as safety layer // if ToolWindow isn't opened before the settings there is a null access
        PARSER.projectPath = Path.of(Objects.requireNonNull(project.getBasePath()));
        CodeStatsWindow.project = project;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        PARSER.projectPath = Path.of(Objects.requireNonNull(project.getBasePath()));
        CodeStatsWindow.project = project;
        PARSER.updateState();
        initUI(toolWindow, project);
    }

    private void initUI(ToolWindow toolWindow, Project project) {
        ActionButton refreshButton = UIHelper.createButton("Refresh", "Get CodeStats!", AllIcons.Actions.Refresh, this::update);
        ActionButton settingsButton = UIHelper.createButton("Settings", "Customize CodeStats!", AllIcons.General.GearPlain,
                () -> ShowSettingsUtil.getInstance().showSettingsDialog(project, Settings.class));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(settingsButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(TABBED_PANE, BorderLayout.CENTER);

        //To react to resize events for the TIME_LINE
        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                TIME_LINE.resizeTimer.restart();
            }
        });

        TABBED_PANE.addChangeListener(e -> {
            if (TABBED_PANE.getSelectedIndex() == 1) {
                TIME_LINE.refreshGraphic = true;
            }
        });


        var content = ContentFactory.getInstance().createContent(mainPanel, "CodeStats", true);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public void toolWindowShown(@NotNull ToolWindow toolWindow) {
        if ("CodeStats".equals(toolWindow.getId()) && !Save.getInstance().disableAutoUpdate) {
            update();
        }
    }


    public void update() {
        //TODO potentially no need to remove them / just reassign the tables / could get rid of visual reload delay
        TABBED_PANE.removeAll();
        PARSER.updatePane();
    }
}