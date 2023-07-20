package com.gk646.codestats;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

public class ToolBarAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            VirtualFile baseDir = project.getBaseDir();
            if (baseDir != null) {
                String projectPath = baseDir.getPath();
                Messages.showMessageDialog(project, "Project name: " + project.getName() + " base path : " + projectPath, "Info", Messages.getInformationIcon());
            }
        }
    }
}
