package com.gk646.codestats;

import com.gk646.codestats.settings.Settings;
import com.gk646.codestats.stats.Parser;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class CodeStatsWindow implements ToolWindowFactory, ProjectManagerListener, ToolWindowManagerListener {
    public static final JTabbedPane tabbedPane = new JBTabbedPane();
    public static final JPanel sumPane = new JBPanel<>();
    public static Parser parser;
    public static Project project;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull com.intellij.openapi.wm.ToolWindow toolWindow) {
        CodeStatsWindow.project = project;

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> update());

        JButton settingsButton = new JButton("Settings");
        settingsButton.addActionListener(e -> ShowSettingsUtil.getInstance().showSettingsDialog(project, Settings.class));

        // Create a panel and add the buttons to it
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(refreshButton);
        buttonPanel.add(settingsButton);

        // Create a new panel, add the buttonPanel and tabbedPane to it
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(sumPane,BorderLayout.EAST);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(mainPanel, "CodeStats", true);
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
        parser.updatePane(tabbedPane);
    }

    @Override
    public void projectOpened(@NotNull Project project) {
        parser = new Parser(project.getBasePath());
    }
}
