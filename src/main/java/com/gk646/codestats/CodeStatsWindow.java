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

import com.gk646.codestats.settings.Settings;
import com.gk646.codestats.stats.Parser;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class CodeStatsWindow implements ToolWindowFactory, ProjectManagerListener, ToolWindowManagerListener {
    public static final JTabbedPane tabbedPane = new JBTabbedPane();

    public static Parser parser;
    public static Project project;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull com.intellij.openapi.wm.ToolWindow toolWindow) {
        CodeStatsWindow.project = project;

        //refresh button
        AnAction refreshAction = new AnAction("Refresh", "Get CodeStats!", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                CodeStatsWindow.this.update();
            }
        };
        ActionButton refreshButton = new ActionButton(refreshAction, refreshAction.getTemplatePresentation(), "Refresh", ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);

        //settings button
        AnAction settingsAction = new AnAction("Settings", "Customize CodeStats!", AllIcons.General.GearPlain) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, Settings.class);
            }
        };
        ActionButton settingsButton = new ActionButton(settingsAction, settingsAction.getTemplatePresentation(), "Settings", ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(settingsButton);


        var mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        var contentFactory = ContentFactory.getInstance();
        var content = contentFactory.createContent(mainPanel, "CodeStats", true);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public void toolWindowShown(@NotNull ToolWindow toolWindow) {
        if (toolWindow.getId().equals("CodeStats")) {
            update();
        }
    }

    public void update() {
        tabbedPane.removeAll();
        parser.updatePane();
    }

    @Override
    public void projectOpened(@NotNull Project project) {
        parser = new Parser(project.getBasePath());
    }
}
