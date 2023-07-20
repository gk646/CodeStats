package com.gk646.codestats;

import com.gk646.codestats.stats.Parser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.JTabbedPane;

public class CodeStatsWindow implements ToolWindowFactory, ProjectManagerListener, ToolWindowManagerListener {
    public static final JTabbedPane tabbedPane = new JBTabbedPane();
    public static Parser parser;
    boolean projectOpened = false;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull com.intellij.openapi.wm.ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(tabbedPane, "CodeStats", true);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
        System.out.println("state changed");
        ToolWindow toolWindow = toolWindowManager.getToolWindow("Code Stats");
        if (toolWindow != null && toolWindow.isVisible()) {
            update();
        }
    }

    @Override
    public void toolWindowShown(@NotNull ToolWindow toolWindow) {
        update();
        System.out.println("hey");
    }

    public void update() {
        if (projectOpened) {
            parser.update(tabbedPane);
        }
    }

    @Override
    public void projectOpened(@NotNull Project project) {
        var prm = ProjectRootManager.getInstance(project);
        parser = new Parser(prm.getFileIndex());
        projectOpened = true;
        update();
    }
}
